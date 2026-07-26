from datetime import datetime
from decimal import Decimal

from sqlalchemy.orm import Session

from app.models.budget import Budget
from app.models.category import Category
from app.models.transaction import Transaction
from app.models.wallet import Wallet
from app.repositories.transaction_repo import TransactionRepository
from app.schemas.transaction_schema import (
    TransactionCreateRequest,
    TransactionUpdateRequest,
    TransferCreateRequest,
)


class TransactionService:
    def __init__(self):
        self.transaction_repo = TransactionRepository()

    def _generate_transaction_id(self, db: Session) -> str:
        date_part = datetime.now().strftime("%y%m%d")
        prefix = f"TXN{date_part}"

        last_transaction = (
            db.query(Transaction)
            .filter(Transaction.TransactionID.like(f"{prefix}%"))
            .order_by(Transaction.TransactionID.desc())
            .first()
        )

        last_seq = int(last_transaction.TransactionID[-4:]) if last_transaction else 0
        new_seq = last_seq + 1
        return f"{prefix}{new_seq:04d}"

    def _apply_budget_spent_delta(
        self,
        db: Session,
        user_id: str,
        category_id: str,
        tx_date,
        amount_delta: Decimal,
    ) -> None:
        budgets = (
            db.query(Budget)
            .filter(
                Budget.UserID == user_id,
                Budget.CategoryID == category_id,
                Budget.StartDate <= tx_date,
                Budget.EndDate >= tx_date,
            )
            .all()
        )

        for budget in budgets:
            next_value = Decimal(budget.SpentAmount) + Decimal(amount_delta)
            if next_value < 0:
                next_value = Decimal("0")

            budget.SpentAmount = next_value
            budget.UpdatedAt = datetime.now()

    def _get_accessible_category(self, db: Session, user_id: str, category_id: str) -> Category | None:
        return (
            db.query(Category)
            .filter(
                Category.CategoryID == category_id,
                ((Category.UserID == user_id) | (Category.UserID.is_(None))),
                Category.IsDeleted.is_(False),
            )
            .first()
        )

    @staticmethod
    def _validate_category_matches_transaction(category: Category, transaction_type: str) -> None:
        if category.CategoryType != transaction_type:
            raise ValueError("Loại danh mục phải trùng với loại giao dịch")

    def _get_or_create_transfer_category(self, db: Session, user_id: str, category_type: str) -> Category:
        category_name = "Chuyển tiền đi" if category_type == "expense" else "Chuyển tiền đến"
        icon = "/sneat/img/icons/categories/exchange-alt.svg"
        color = "#696cff"

        category = (
            db.query(Category)
            .filter(
                Category.CategoryName == category_name,
                Category.CategoryType == category_type,
                ((Category.UserID == user_id) | (Category.UserID.is_(None))),
                Category.IsDeleted.is_(False),
            )
            .order_by(Category.IsDefault.desc())
            .first()
        )
        if category:
            return category

        category = Category(
            CategoryID=f"CATTR{category_type[:3].upper()}{user_id[-4:]}",
            UserID=user_id,
            CategoryName=category_name,
            CategoryType=category_type,
            Icon=icon,
            Color=color,
            IsDefault=False,
            IsDeleted=False,
        )
        db.add(category)
        db.flush()
        return category

    def get_transactions(self, db: Session, user_id: str):
        return self.transaction_repo.get_all_by_user(db, user_id)

    def create_transaction(self, db: Session, user_id: str, data: TransactionCreateRequest):
        wallet = db.query(Wallet).filter(Wallet.WalletID == data.wallet_id, Wallet.UserID == user_id).first()
        if not wallet:
            raise ValueError("Wallet not found")

        category = self._get_accessible_category(db, user_id, data.category_id)
        if not category:
            raise ValueError("Category not found")
        self._validate_category_matches_transaction(category, data.transaction_type)

        if data.transaction_type == "expense" and Decimal(wallet.CurrentBalance) < Decimal(data.amount):
            raise ValueError("Insufficient wallet balance")

        transaction = Transaction(
            TransactionID=self._generate_transaction_id(db),
            UserID=user_id,
            WalletID=data.wallet_id,
            CategoryID=data.category_id,
            TransactionType=data.transaction_type,
            Amount=data.amount,
            TransactionDate=data.transaction_date,
            Note=data.note,
            IsRecurring=data.is_recurring,
            RecurInterval=data.recur_interval,
        )

        if data.transaction_type == "income":
            wallet.CurrentBalance = Decimal(wallet.CurrentBalance) + Decimal(data.amount)
        else:
            wallet.CurrentBalance = Decimal(wallet.CurrentBalance) - Decimal(data.amount)
            self._apply_budget_spent_delta(
                db=db,
                user_id=user_id,
                category_id=data.category_id,
                tx_date=data.transaction_date,
                amount_delta=Decimal(data.amount),
            )

        db.add(transaction)
        wallet.UpdatedAt = datetime.now()
        db.commit()
        db.refresh(transaction)
        return transaction

    def update_transaction(self, db: Session, transaction_id: str, user_id: str, data: TransactionUpdateRequest):
        transaction = self.transaction_repo.get_by_id_and_user(db, transaction_id, user_id)
        if not transaction:
            raise ValueError("Transaction not found")

        old_wallet = db.query(Wallet).filter(Wallet.WalletID == transaction.WalletID, Wallet.UserID == user_id).first()
        if not old_wallet:
            raise ValueError("Original wallet not found")

        if transaction.TransactionType == "income":
            old_wallet.CurrentBalance = Decimal(old_wallet.CurrentBalance) - Decimal(transaction.Amount)
        else:
            old_wallet.CurrentBalance = Decimal(old_wallet.CurrentBalance) + Decimal(transaction.Amount)
            self._apply_budget_spent_delta(
                db,
                user_id,
                transaction.CategoryID,
                transaction.TransactionDate,
                -Decimal(transaction.Amount),
            )

        new_wallet_id = data.wallet_id or transaction.WalletID
        new_category_id = data.category_id or transaction.CategoryID
        new_type = data.transaction_type or transaction.TransactionType
        new_amount = Decimal(data.amount) if data.amount is not None else Decimal(transaction.Amount)
        new_date = data.transaction_date or transaction.TransactionDate

        new_wallet = db.query(Wallet).filter(Wallet.WalletID == new_wallet_id, Wallet.UserID == user_id).first()
        if not new_wallet:
            raise ValueError("Wallet not found")

        new_category = self._get_accessible_category(db, user_id, new_category_id)
        if not new_category:
            raise ValueError("Category not found")
        self._validate_category_matches_transaction(new_category, new_type)

        if new_type == "expense" and Decimal(new_wallet.CurrentBalance) < new_amount:
            raise ValueError("Wallet error or insufficient balance")

        if new_type == "income":
            new_wallet.CurrentBalance = Decimal(new_wallet.CurrentBalance) + new_amount
        else:
            new_wallet.CurrentBalance = Decimal(new_wallet.CurrentBalance) - new_amount
            self._apply_budget_spent_delta(db, user_id, new_category_id, new_date, new_amount)

        transaction.WalletID = new_wallet_id
        transaction.CategoryID = new_category_id
        transaction.TransactionType = new_type
        transaction.Amount = new_amount
        transaction.TransactionDate = new_date
        transaction.Note = data.note if data.note is not None else transaction.Note

        if data.is_recurring is not None:
            transaction.IsRecurring = data.is_recurring
            transaction.RecurInterval = data.recur_interval if data.is_recurring else None
        elif data.recur_interval is not None and transaction.IsRecurring:
            transaction.RecurInterval = data.recur_interval

        transaction.UpdatedAt = datetime.now()
        old_wallet.UpdatedAt = datetime.now()
        new_wallet.UpdatedAt = datetime.now()
        db.commit()
        db.refresh(transaction)
        return transaction

    def delete_transaction(self, db: Session, transaction_id: str, user_id: str):
        transaction = self.transaction_repo.get_by_id_and_user(db, transaction_id, user_id)
        if not transaction:
            raise ValueError("Transaction not found")

        wallet = db.query(Wallet).filter(Wallet.WalletID == transaction.WalletID, Wallet.UserID == user_id).first()
        if wallet:
            if transaction.TransactionType == "income":
                wallet.CurrentBalance = Decimal(wallet.CurrentBalance) - Decimal(transaction.Amount)
            else:
                wallet.CurrentBalance = Decimal(wallet.CurrentBalance) + Decimal(transaction.Amount)
                self._apply_budget_spent_delta(
                    db,
                    user_id,
                    transaction.CategoryID,
                    transaction.TransactionDate,
                    -Decimal(transaction.Amount),
                )
            wallet.UpdatedAt = datetime.now()

        self.transaction_repo.delete(db, transaction)
        db.commit()

    def create_transfer(self, db: Session, user_id: str, data: TransferCreateRequest):
        from_wallet = db.query(Wallet).filter(Wallet.WalletID == data.from_wallet_id, Wallet.UserID == user_id).first()
        to_wallet = db.query(Wallet).filter(Wallet.WalletID == data.to_wallet_id, Wallet.UserID == user_id).first()

        if not from_wallet or not to_wallet:
            raise ValueError("Một hoặc cả hai ví không tồn tại.")
        if from_wallet.WalletID == to_wallet.WalletID:
            raise ValueError("Ví nguồn và ví đích phải khác nhau.")
        if Decimal(from_wallet.CurrentBalance) < data.amount:
            raise ValueError("Số dư ví nguồn không đủ.")

        expense_cat = self._get_or_create_transfer_category(db, user_id, "expense")
        income_cat = self._get_or_create_transfer_category(db, user_id, "income")

        base_id = self._generate_transaction_id(db)
        prefix = base_id[:-4]
        seq = int(base_id[-4:])
        next_id = f"{prefix}{(seq + 1):04d}"

        try:
            expense_tx = Transaction(
                TransactionID=base_id,
                UserID=user_id,
                WalletID=data.from_wallet_id,
                CategoryID=expense_cat.CategoryID,
                TransactionType="expense",
                Amount=data.amount,
                TransactionDate=data.transfer_date,
                Note=f"Chuyển đến [{to_wallet.WalletName}]: {data.note or ''}".strip(),
                IsRecurring=False,
            )

            income_tx = Transaction(
                TransactionID=next_id,
                UserID=user_id,
                WalletID=data.to_wallet_id,
                CategoryID=income_cat.CategoryID,
                TransactionType="income",
                Amount=data.amount,
                TransactionDate=data.transfer_date,
                Note=f"Nhận từ [{from_wallet.WalletName}]: {data.note or ''}".strip(),
                IsRecurring=False,
            )

            from_wallet.CurrentBalance = Decimal(from_wallet.CurrentBalance) - data.amount
            to_wallet.CurrentBalance = Decimal(to_wallet.CurrentBalance) + data.amount
            from_wallet.UpdatedAt = datetime.now()
            to_wallet.UpdatedAt = datetime.now()

            db.add_all([expense_tx, income_tx])
            db.commit()
            db.refresh(expense_tx)
            db.refresh(income_tx)

            return {"expense": expense_tx, "income": income_tx}

        except Exception as e:
            db.rollback()
            raise e
