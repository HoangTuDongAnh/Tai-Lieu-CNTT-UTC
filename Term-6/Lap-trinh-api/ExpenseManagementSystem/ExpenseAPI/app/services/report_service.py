from collections import defaultdict
from datetime import date, datetime, timedelta
from decimal import Decimal, ROUND_HALF_UP

from sqlalchemy import extract, case, func
from sqlalchemy.orm import Session

from app.models.budget import Budget
from app.models.category import Category
from app.models.transaction import Transaction
from app.models.wallet import Wallet


class ReportService:
    def get_monthly_summary(self, db: Session, user_id: str, year: int):
        # Sửa func.month/year thành extract
        month_expr = extract('month', Transaction.TransactionDate)
        year_expr = extract('year', Transaction.TransactionDate)

        results = (
            db.query(
                month_expr.label("month"),
                func.coalesce(
                    func.sum(
                        case((Transaction.TransactionType == "income", Transaction.Amount), else_=0)
                    ), 0
                ).label("total_income"),
                func.coalesce(
                    func.sum(
                        case((Transaction.TransactionType == "expense", Transaction.Amount), else_=0)
                    ), 0
                ).label("total_expense"),
            )
            .filter(Transaction.UserID == user_id, year_expr == year)
            .group_by(month_expr)
            .order_by(month_expr)
            .all()
        )

        return [
            {
                "month": int(r.month),
                "total_income": Decimal(r.total_income),
                "total_expense": Decimal(r.total_expense),
            }
            for r in results
        ]

    def get_category_summary(self, db: Session, user_id: str, month: int, year: int):
        return self.get_category_summary_range(
            db=db,
            user_id=user_id,
            start_date=date(year, month, 1),
            end_date=self._month_end(date(year, month, 1)),
        )

    def get_category_summary_range(self, db: Session, user_id: str, start_date: date, end_date: date):
        start_date, end_date = self._normalize_dates(start_date, end_date)

        results = (
            db.query(
                Category.CategoryID.label("category_id"),
                Category.CategoryName.label("category_name"),
                Category.Icon.label("icon"),
                Category.Color.label("color"),
                func.coalesce(func.sum(Transaction.Amount), 0).label("total_amount"),
            )
            .join(Category, Transaction.CategoryID == Category.CategoryID)
            .filter(
                Transaction.UserID == user_id,
                Transaction.TransactionType == "expense",
                Transaction.TransactionDate >= start_date,
                Transaction.TransactionDate <= end_date,
            )
            .group_by(Category.CategoryID, Category.CategoryName, Category.Icon, Category.Color)
            .order_by(func.sum(Transaction.Amount).desc())
            .all()
        )

        total = sum(Decimal(r.total_amount) for r in results) if results else Decimal("0")
        output = []
        for r in results:
            amount = Decimal(r.total_amount)
            percentage = Decimal("0.00")
            if total > 0:
                percentage = ((amount * Decimal("100")) / total).quantize(Decimal("0.01"))
            output.append(
                {
                    "category_id": r.category_id,
                    "category_name": r.category_name,
                    "icon": r.icon,
                    "color": r.color,
                    "total_amount": amount,
                    "percentage": percentage,
                }
            )
        return output

    def get_dashboard_overview(self, db: Session, user_id: str):
        now = datetime.now().date()
        return self.get_dashboard_overview_range(
            db=db,
            user_id=user_id,
            start_date=date(now.year, now.month, 1),
            end_date=self._month_end(date(now.year, now.month, 1)),
        )

    def get_dashboard_overview_range(self, db: Session, user_id: str, start_date: date, end_date: date):
        start_date, end_date = self._normalize_dates(start_date, end_date)

        total_balance = (
            db.query(func.coalesce(func.sum(Wallet.CurrentBalance), 0))
            .filter(Wallet.UserID == user_id)
            .scalar()
        )

        monthly_income = (
            db.query(func.coalesce(func.sum(Transaction.Amount), 0))
            .filter(
                Transaction.UserID == user_id,
                Transaction.TransactionType == "income",
                Transaction.TransactionDate >= start_date,  # ✅ dùng start_date/end_date
                Transaction.TransactionDate <= end_date,
            )
            .scalar()
        )

        monthly_expense = (
            db.query(func.coalesce(func.sum(Transaction.Amount), 0))
            .filter(
                Transaction.UserID == user_id,
                Transaction.TransactionType == "expense",
                Transaction.TransactionDate >= start_date,  # ✅
                Transaction.TransactionDate <= end_date,
            )
            .scalar()
        )

        transaction_count = (
            db.query(func.count(Transaction.TransactionID))
            .filter(
                Transaction.UserID == user_id,
                Transaction.TransactionDate >= start_date,  # ✅
                Transaction.TransactionDate <= end_date,
            )
            .scalar()
        )

        return {
            "total_balance": Decimal(total_balance or 0),
            "monthly_income": Decimal(monthly_income or 0),
            "monthly_expense": Decimal(monthly_expense or 0),
            "transaction_count": int(transaction_count or 0),
        }

    def get_top_expenses(self, db: Session, user_id: str, month: int, year: int, limit: int = 5):
        results = (
            db.query(
                Transaction.TransactionID.label("transaction_id"),
                Transaction.TransactionDate.label("transaction_date"),
                Transaction.Amount.label("amount"),
                Transaction.Note.label("note"),
                Wallet.WalletID.label("wallet_id"),
                Wallet.WalletName.label("wallet_name"),
                Category.CategoryID.label("category_id"),
                Category.CategoryName.label("category_name"),
                Category.Icon.label("category_icon"),
                Category.Color.label("category_color"),
            )
            .join(Wallet, Transaction.WalletID == Wallet.WalletID)
            .join(Category, Transaction.CategoryID == Category.CategoryID)
            .filter(
                Transaction.UserID == user_id,
                Transaction.TransactionType == "expense",
                extract('month', Transaction.TransactionDate) == month,
                extract('year', Transaction.TransactionDate) == year,
            )
            .order_by(Transaction.Amount.desc(), Transaction.TransactionDate.desc())
            .limit(limit)
            .all()
        )

        return [
            {
                "transaction_id": r.transaction_id,
                "transaction_date": r.transaction_date,
                "amount": Decimal(r.amount),
                "note": r.note,
                "wallet_id": r.wallet_id,
                "wallet_name": r.wallet_name,
                "category_id": r.category_id,
                "category_name": r.category_name,
                "category_icon": r.category_icon,
                "category_color": r.category_color,
            }
            for r in results
        ]

    def get_top_expenses_range(self, db: Session, user_id: str, start_date: date, end_date: date, limit: int = 5):
        start_date, end_date = self._normalize_dates(start_date, end_date)

        results = (
            db.query(
                Transaction.TransactionID.label("transaction_id"),
                Transaction.TransactionDate.label("transaction_date"),
                Transaction.Amount.label("amount"),
                Transaction.Note.label("note"),
                Wallet.WalletID.label("wallet_id"),
                Wallet.WalletName.label("wallet_name"),
                Category.CategoryID.label("category_id"),
                Category.CategoryName.label("category_name"),
                Category.Icon.label("category_icon"),
                Category.Color.label("category_color"),
            )
            .join(Wallet, Transaction.WalletID == Wallet.WalletID)
            .join(Category, Transaction.CategoryID == Category.CategoryID)
            .filter(
                Transaction.UserID == user_id,
                Transaction.TransactionType == "expense",
                Transaction.TransactionDate >= start_date,
                Transaction.TransactionDate <= end_date,
            )
            .order_by(Transaction.Amount.desc(), Transaction.TransactionDate.desc())
            .limit(limit)
            .all()
        )

        return [
            {
                "transaction_id": r.transaction_id,
                "transaction_date": r.transaction_date,
                "amount": Decimal(r.amount),
                "note": r.note,
                "wallet_id": r.wallet_id,
                "wallet_name": r.wallet_name,
                "category_id": r.category_id,
                "category_name": r.category_name,
                "category_icon": r.category_icon,
                "category_color": r.category_color,
            }
            for r in results
        ]

    def get_budget_progress(self, db: Session, user_id: str, month: int, year: int):
        budgets = (
            db.query(
                Budget.BudgetID.label("budget_id"),
                Budget.CategoryID.label("category_id"),
                Budget.LimitAmount.label("limit_amount"),
                Budget.SpentAmount.label("spent_amount"),
                Category.CategoryName.label("category_name"),
                Category.Icon.label("category_icon"),
                Category.Color.label("category_color"),
            )
            .join(Category, Budget.CategoryID == Category.CategoryID)
            .filter(
                Budget.UserID == user_id,
                Budget.PeriodType == "month",
                Budget.PeriodYear == year,
                Budget.PeriodMonth == month,
            )
            .order_by(Budget.SpentAmount.desc())
            .all()
        )

        output = []
        for item in budgets:
            limit_amount = Decimal(item.limit_amount)
            spent_amount = Decimal(item.spent_amount)
            remaining_amount = limit_amount - spent_amount

            if limit_amount > 0:
                percentage_used = ((spent_amount * Decimal("100")) / limit_amount).quantize(
                    Decimal("0.01"), rounding=ROUND_HALF_UP
                )
            else:
                percentage_used = Decimal("0.00")

            if spent_amount > limit_amount:
                status = "over"
            elif spent_amount == limit_amount:
                status = "reached"
            else:
                status = "normal"

            output.append(
                {
                    "budget_id": item.budget_id,
                    "category_id": item.category_id,
                    "category_name": item.category_name,
                    "category_icon": item.category_icon,
                    "category_color": item.category_color,
                    "limit_amount": limit_amount,
                    "spent_amount": spent_amount,
                    "remaining_amount": remaining_amount,
                    "percentage_used": percentage_used,
                    "status": status,
                }
            )

        return output

    def get_cashflow_analytics(self, db: Session, user_id: str, start_date: date, end_date: date, granularity: str):
        start_date, end_date = self._normalize_dates(start_date, end_date)

        granularity = (granularity or "day").lower()
        if granularity not in {"day", "week", "month", "year"}:
            granularity = "day"

        transactions = (
            db.query(
                Transaction.TransactionDate.label("transaction_date"),
                Transaction.TransactionType.label("transaction_type"),
                Transaction.Amount.label("amount"),
            )
            .filter(
                Transaction.UserID == user_id,
                Transaction.TransactionDate >= start_date,
                Transaction.TransactionDate <= end_date,
            )
            .order_by(Transaction.TransactionDate.asc())
            .all()
        )

        current_total_balance = Decimal(
            db.query(func.coalesce(func.sum(Wallet.CurrentBalance), 0))
            .filter(Wallet.UserID == user_id)
            .scalar()
            or 0
        )

        period_income = Decimal("0")
        period_expense = Decimal("0")
        grouped = defaultdict(lambda: {"income": Decimal("0"), "expense": Decimal("0")})

        for tx in transactions:
            amount = Decimal(tx.amount or 0)
            if tx.transaction_type == "income":
                period_income += amount
            elif tx.transaction_type == "expense":
                period_expense += amount

            key, label = self._bucket_for_date(tx.transaction_date, granularity)
            grouped[(key, label)][tx.transaction_type] += amount

        series_items = []
        running_balance = current_total_balance - (period_income - period_expense)
        for key, label in self._generate_buckets(start_date, end_date, granularity):
            income = grouped[(key, label)]["income"]
            expense = grouped[(key, label)]["expense"]
            net = income - expense
            running_balance += net
            series_items.append(
                {
                    "key": key,
                    "label": label,
                    "income": income,
                    "expense": expense,
                    "net": net,
                    "running_balance": running_balance,
                }
            )

        transaction_count = len(transactions)
        expense_transactions = sum(1 for tx in transactions if tx.transaction_type == "expense")
        average_expense = (
            (period_expense / Decimal(expense_transactions)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
            if expense_transactions > 0 else Decimal("0.00")
        )

        busiest_label = None
        if series_items:
            busiest = max(series_items, key=lambda item: (item["income"] + item["expense"]))
            if (busiest["income"] + busiest["expense"]) > 0:
                busiest_label = busiest["label"]

        return {
            "start_date": start_date,
            "end_date": end_date,
            "granularity": granularity,
            "total_income": period_income,
            "total_expense": period_expense,
            "net_change": period_income - period_expense,
            "transaction_count": transaction_count,
            "average_expense": average_expense,
            "busiest_label": busiest_label,
            "series": series_items,
        }

    def _month_end(self, value: date) -> date:
        next_month = (value.replace(day=28) + timedelta(days=4)).replace(day=1)
        return next_month - timedelta(days=1)

    def _normalize_dates(self, start_date: date, end_date: date) -> tuple[date, date]:
        if end_date < start_date:
            return end_date, start_date
        return start_date, end_date

    def _bucket_for_date(self, value: date, granularity: str):
        if granularity == "year":
            return str(value.year), str(value.year)
        if granularity == "month":
            return value.strftime("%Y-%m"), value.strftime("%m/%Y")
        if granularity == "week":
            iso_year, iso_week, _ = value.isocalendar()
            return f"{iso_year}-W{iso_week:02d}", f"W{iso_week:02d}/{iso_year}"
        return value.isoformat(), value.strftime("%d/%m")

    def _generate_buckets(self, start_date: date, end_date: date, granularity: str):
        buckets = []
        current = start_date

        if granularity == "year":
            for year in range(start_date.year, end_date.year + 1):
                buckets.append((str(year), str(year)))
            return buckets

        if granularity == "month":
            current = date(start_date.year, start_date.month, 1)
            while current <= end_date:
                buckets.append((current.strftime("%Y-%m"), current.strftime("%m/%Y")))
                if current.month == 12:
                    current = date(current.year + 1, 1, 1)
                else:
                    current = date(current.year, current.month + 1, 1)
            return buckets

        if granularity == "week":
            current = start_date - timedelta(days=start_date.weekday())
            while current <= end_date:
                iso_year, iso_week, _ = current.isocalendar()
                buckets.append((f"{iso_year}-W{iso_week:02d}", f"W{iso_week:02d}/{iso_year}"))
                current += timedelta(days=7)
            return buckets

        while current <= end_date:
            buckets.append((current.isoformat(), current.strftime("%d/%m")))
            current += timedelta(days=1)
        return buckets
