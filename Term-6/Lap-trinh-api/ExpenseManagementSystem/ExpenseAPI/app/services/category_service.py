from datetime import datetime
from decimal import Decimal

from sqlalchemy import func, or_
from sqlalchemy.orm import Session

from app.models.budget import Budget
from app.models.category import Category
from app.models.transaction import Transaction
from app.repositories.category_repo import CategoryRepository
from app.schemas.category_schema import (
    CategoryBudgetSummary,
    CategoryCreateRequest,
    CategoryDeleteRequest,
    CategoryOverviewResponse,
    CategoryResponse,
    CategoryUpdateRequest,
)
from app.utils.budget_period import calculate_budget_metrics, normalize_period


class CategoryService:
    def __init__(self):
        self.category_repo = CategoryRepository()

    def _generate_category_id(self, db: Session) -> str:
        date_part = datetime.now().strftime("%y%m%d")
        prefix = f"CAT{date_part}"

        last_category = (
            db.query(Category)
            .filter(Category.CategoryID.like(f"{prefix}%"))
            .order_by(Category.CategoryID.desc())
            .first()
        )

        last_seq = int(last_category.CategoryID[-3:]) if last_category else 0
        new_seq = last_seq + 1
        return f"{prefix}{new_seq:03d}"

    @staticmethod
    def _normalize_name(value: str | None) -> str:
        return " ".join((value or "").split()).strip()

    def _find_name_conflict(
        self,
        db: Session,
        user_id: str,
        category_name: str,
        exclude_category_id: str | None = None,
    ) -> Category | None:
        normalized = self._normalize_name(category_name)
        if not normalized:
            return None

        query = db.query(Category).filter(
            Category.IsDeleted == False,
            or_(Category.UserID == user_id, Category.UserID.is_(None)),
            func.lower(func.ltrim(func.rtrim(Category.CategoryName))) == normalized.lower(),
        )

        if exclude_category_id:
            query = query.filter(Category.CategoryID != exclude_category_id)

        return query.first()

    def _resolve_replacement_category(
        self,
        db: Session,
        user_id: str,
        replacement_category_id: str | None,
        deleting_category_id: str,
        deleting_category_type: str,
    ) -> Category:
        replacement: Category | None = None

        if replacement_category_id:
            replacement = (
                db.query(Category)
                .filter(
                    Category.CategoryID == replacement_category_id,
                    Category.IsDeleted == False,
                    Category.CategoryType == deleting_category_type,
                    or_(Category.UserID == user_id, Category.UserID.is_(None)),
                )
                .first()
            )

            if not replacement:
                raise ValueError("Danh mục thay thế không hợp lệ, không tồn tại, hoặc khác loại.")

            if replacement.CategoryID == deleting_category_id:
                raise ValueError("Danh mục thay thế phải khác danh mục đang xóa.")

            return replacement

        fallback_name = "Khác" if deleting_category_type == "expense" else "Thu nhập khác"
        replacement = (
            db.query(Category)
            .filter(
                Category.CategoryName == fallback_name,
                Category.CategoryType == deleting_category_type,
                Category.IsDeleted == False,
                or_(Category.UserID == user_id, Category.UserID.is_(None)),
            )
            .order_by(Category.IsDefault.desc())
            .first()
        )

        if not replacement:
            raise ValueError(
                f"Không tìm thấy danh mục thay thế mặc định cho loại '{deleting_category_type}'. Hãy truyền replacement_category_id."
            )

        if replacement.CategoryID == deleting_category_id:
            raise ValueError("Không thể dùng chính danh mục đang xóa làm danh mục thay thế.")

        return replacement

    def get_categories(
        self,
        db: Session,
        user_id: str,
        include_deleted: bool = False,
        category_type: str | None = None,
    ):
        return self.category_repo.get_all_by_user(db, user_id, include_deleted, category_type)

    def get_categories_response(
        self,
        db: Session,
        user_id: str,
        include_deleted: bool = False,
        category_type: str | None = None,
    ) -> list[CategoryResponse]:
        categories = self.get_categories(db, user_id, include_deleted, category_type)
        return [
            CategoryResponse(
                category_id=c.CategoryID,
                user_id=c.UserID,
                category_name=c.CategoryName,
                category_type=c.CategoryType,
                icon=c.Icon,
                color=c.Color,
                is_default=c.IsDefault,
            )
            for c in categories
        ]

    def get_categories_overview(
        self,
        db: Session,
        user_id: str,
        period_type: str,
        period_year: int,
        period_month: int | None = None,
        period_week: int | None = None,
        category_type: str | None = None,
    ) -> list[CategoryOverviewResponse]:
        period = normalize_period(
            period_type=period_type,
            period_year=period_year,
            period_month=period_month,
            period_week=period_week,
        )

        categories = self.category_repo.get_all_by_user(
            db, user_id, include_deleted=False, category_type=category_type
        )

        tx_counts = (
            db.query(Transaction.CategoryID, func.count(Transaction.TransactionID).label("count"))
            .filter(Transaction.UserID == user_id)
            .group_by(Transaction.CategoryID)
            .all()
        )
        tx_count_map = {cid: count for cid, count in tx_counts}

        budgets = (
            db.query(Budget)
            .filter(
                Budget.UserID == user_id,
                Budget.StartDate <= period["end_date"],
                Budget.EndDate >= period["start_date"],
            )
            .all()
        )

        priority = {"week": 1, "month": 2, "year": 3}
        budget_map = {}
        for b in budgets:
            cid = b.CategoryID
            if cid not in budget_map:
                budget_map[cid] = b
            elif priority.get(b.PeriodType, 99) < priority.get(budget_map[cid].PeriodType, 99):
                budget_map[cid] = b

        result: list[CategoryOverviewResponse] = []

        for c in categories:
            budget = budget_map.get(c.CategoryID)

            if budget:
                metrics = calculate_budget_metrics(budget.LimitAmount, budget.SpentAmount)
                budget_summary = CategoryBudgetSummary(
                    budget_id=budget.BudgetID,
                    limit_amount=budget.LimitAmount,
                    spent_amount=budget.SpentAmount,
                    remaining_amount=Decimal(str(metrics["remaining_amount"])),
                    percentage_used=metrics["percentage_used"],
                    status=metrics["status"],
                    period_type=budget.PeriodType,
                    period_year=budget.PeriodYear,
                    period_month=budget.PeriodMonth,
                    period_week=budget.PeriodWeek,
                    start_date=budget.StartDate,
                    end_date=budget.EndDate,
                )
            else:
                budget_summary = CategoryBudgetSummary(
                    status="none",
                    period_type=period["period_type"],
                    period_year=period["period_year"],
                    period_month=period["period_month"],
                    period_week=period["period_week"],
                    start_date=period["start_date"],
                    end_date=period["end_date"],
                )

            result.append(
                CategoryOverviewResponse(
                    category_id=c.CategoryID,
                    user_id=c.UserID,
                    category_name=c.CategoryName,
                    category_type=c.CategoryType,
                    icon=c.Icon,
                    color=c.Color,
                    is_default=c.IsDefault,
                    can_edit=(c.UserID == user_id),
                    can_delete=(c.UserID == user_id),
                    budget=budget_summary,
                    transaction_count=tx_count_map.get(c.CategoryID, 0),
                )
            )

        return result

    def create_category(self, db: Session, user_id: str, data: CategoryCreateRequest):
        normalized_name = self._normalize_name(data.category_name)
        if not normalized_name:
            raise ValueError("Tên danh mục không được để trống")

        existing_category = self._find_name_conflict(db, user_id, normalized_name)
        if existing_category:
            raise ValueError("Tên danh mục đã tồn tại hoặc trùng với danh mục mặc định")

        soft_deleted_category = self.category_repo.get_by_name_and_user_any_status(
            db=db,
            category_name=normalized_name,
            user_id=user_id,
        )

        if soft_deleted_category and soft_deleted_category.IsDeleted:
            soft_deleted_category.CategoryType = data.category_type
            soft_deleted_category.Icon = data.icon
            soft_deleted_category.Color = data.color
            soft_deleted_category.IsDefault = False
            soft_deleted_category.IsDeleted = False
            soft_deleted_category.UpdatedAt = datetime.now()
            db.commit()
            db.refresh(soft_deleted_category)
            return soft_deleted_category

        category = Category(
            CategoryID=self._generate_category_id(db),
            UserID=user_id,
            CategoryName=normalized_name,
            CategoryType=data.category_type,
            Icon=data.icon,
            Color=data.color,
            IsDefault=False,
            IsDeleted=False,
        )

        return self.category_repo.create(db, category)

    def update_category(self, db: Session, category_id: str, user_id: str, data: CategoryUpdateRequest):
        category = self.category_repo.get_custom_by_id_and_user(db, category_id, user_id)
        if not category:
            raise ValueError("Category not found or cannot edit default category")

        if data.category_name is not None:
            normalized_name = self._normalize_name(data.category_name)
            if not normalized_name:
                raise ValueError("Tên danh mục không được để trống")

            if normalized_name != category.CategoryName:
                duplicate = self._find_name_conflict(db, user_id, normalized_name, exclude_category_id=category_id)
                if duplicate:
                    raise ValueError("Tên danh mục đã tồn tại hoặc trùng với danh mục mặc định")
                category.CategoryName = normalized_name

        if data.category_type is not None and data.category_type != category.CategoryType:
            linked_tx_count = (
                db.query(func.count(Transaction.TransactionID))
                .filter(Transaction.CategoryID == category_id, Transaction.UserID == user_id)
                .scalar()
            )
            linked_budget_count = (
                db.query(func.count(Budget.BudgetID))
                .filter(Budget.CategoryID == category_id, Budget.UserID == user_id)
                .scalar()
            )
            if (linked_tx_count or 0) > 0 or (linked_budget_count or 0) > 0:
                raise ValueError("Không thể đổi loại danh mục khi đã có giao dịch hoặc ngân sách liên kết")
            category.CategoryType = data.category_type

        if data.icon is not None:
            category.Icon = data.icon

        if data.color is not None:
            category.Color = data.color

        category.UpdatedAt = datetime.now()
        db.commit()
        db.refresh(category)
        return category

    def delete_category(self, db: Session, category_id: str, user_id: str, data: CategoryDeleteRequest | None):
        category = self.category_repo.get_custom_by_id_and_user(db, category_id, user_id)
        if not category:
            raise ValueError("Category not found hoặc không thể xóa danh mục mặc định.")

        replacement = self._resolve_replacement_category(
            db=db,
            user_id=user_id,
            replacement_category_id=(data.replacement_category_id if data else None),
            deleting_category_id=category_id,
            deleting_category_type=category.CategoryType,
        )

        transactions = db.query(Transaction).filter(
            Transaction.CategoryID == category_id,
            Transaction.UserID == user_id,
        ).all()

        for tx in transactions:
            tx.CategoryID = replacement.CategoryID
            tx.UpdatedAt = datetime.now()

        db.flush()

        db.query(Budget).filter(
            Budget.CategoryID == category_id,
            Budget.UserID == user_id,
        ).delete()

        category.IsDeleted = True
        category.UpdatedAt = datetime.now()
        db.flush()

        replacement_budgets = db.query(Budget).filter(
            Budget.CategoryID == replacement.CategoryID,
            Budget.UserID == user_id,
        ).all()

        for budget in replacement_budgets:
            spent_query = (
                db.query(func.sum(Transaction.Amount))
                .filter(
                    Transaction.UserID == user_id,
                    Transaction.CategoryID == replacement.CategoryID,
                    Transaction.TransactionType == "expense",
                    Transaction.TransactionDate >= budget.StartDate,
                    Transaction.TransactionDate <= budget.EndDate,
                )
                .scalar()
            )
            budget.SpentAmount = Decimal(spent_query) if spent_query else Decimal(0)
            budget.UpdatedAt = datetime.now()

        db.commit()
