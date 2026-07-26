from datetime import datetime
from decimal import Decimal

from sqlalchemy import func
from sqlalchemy.orm import Session

from app.models.budget import Budget
from app.models.category import Category
from app.models.transaction import Transaction
from app.repositories.budget_repo import BudgetRepository
from app.schemas.budget_schema import BudgetCreateRequest, BudgetResponse, BudgetUpdateRequest
from app.utils.budget_period import calculate_budget_metrics, normalize_period


class BudgetService:
    def __init__(self):
        self.budget_repo = BudgetRepository()

    def _generate_budget_id(self, db: Session) -> str:
        date_part = datetime.now().strftime("%y%m%d")
        prefix = f"BDG{date_part}"

        last_budget = (
            db.query(Budget)
            .filter(Budget.BudgetID.like(f"{prefix}%"))
            .order_by(Budget.BudgetID.desc())
            .first()
        )

        last_seq = int(last_budget.BudgetID[-4:]) if last_budget else 0
        new_seq = last_seq + 1
        return f"{prefix}{new_seq:04d}"

    def _to_response(self, budget: Budget) -> BudgetResponse:
        metrics = calculate_budget_metrics(budget.LimitAmount, budget.SpentAmount)

        return BudgetResponse(
            budget_id=budget.BudgetID,
            user_id=budget.UserID,
            category_id=budget.CategoryID,
            limit_amount=budget.LimitAmount,
            spent_amount=budget.SpentAmount,
            period_type=budget.PeriodType,
            period_year=budget.PeriodYear,
            period_month=budget.PeriodMonth,
            period_week=budget.PeriodWeek,
            start_date=budget.StartDate,
            end_date=budget.EndDate,
            remaining_amount=Decimal(str(metrics["remaining_amount"])),
            percentage_used=metrics["percentage_used"],
            status=metrics["status"],
        )

    def get_budgets(
        self,
        db: Session,
        user_id: str,
        period_type: str | None = None,
        period_year: int | None = None,
        period_month: int | None = None,
        period_week: int | None = None,
        category_id: str | None = None,
    ) -> list[BudgetResponse]:
        budgets = self.budget_repo.get_all_by_user(
            db=db,
            user_id=user_id,
            period_type=period_type,
            period_year=period_year,
            period_month=period_month,
            period_week=period_week,
            category_id=category_id,
        )
        return [self._to_response(b) for b in budgets]

    def create_budget(self, db: Session, user_id: str, data: BudgetCreateRequest) -> BudgetResponse:
        category = (
            db.query(Category)
            .filter(
                Category.CategoryID == data.category_id,
                Category.IsDeleted.is_(False),
                ((Category.UserID == user_id) | (Category.UserID.is_(None)))
            )
            .first()
        )
        if not category:
            raise ValueError("Category not found")
        if category.CategoryType != "expense":
            raise ValueError("Chỉ có thể tạo ngân sách cho danh mục chi tiêu")

        period = normalize_period(
            period_type=data.period_type,
            period_year=data.period_year,
            period_month=data.period_month,
            period_week=data.period_week,
        )

        existing_budget = self.budget_repo.get_existing(
            db=db,
            user_id=user_id,
            category_id=data.category_id,
            period_type=period["period_type"],
            period_year=period["period_year"],
            period_month=period["period_month"],
            period_week=period["period_week"],
        )
        if existing_budget:
            raise ValueError("Budget already exists for this category and selected period")

        spent_query = (
            db.query(func.sum(Transaction.Amount))
            .filter(
                Transaction.UserID == user_id,
                Transaction.CategoryID == data.category_id,
                Transaction.TransactionType == "expense",
                Transaction.TransactionDate >= period["start_date"],
                Transaction.TransactionDate <= period["end_date"]
            )
            .scalar()
        )
        retroactive_spent = Decimal(spent_query) if spent_query else Decimal(0)

        budget = Budget(
            BudgetID=self._generate_budget_id(db),
            UserID=user_id,
            CategoryID=data.category_id,
            LimitAmount=data.limit_amount,
            SpentAmount=retroactive_spent,
            PeriodType=period["period_type"],
            PeriodYear=period["period_year"],
            PeriodMonth=period["period_month"],
            PeriodWeek=period["period_week"],
            StartDate=period["start_date"],
            EndDate=period["end_date"],
        )

        saved = self.budget_repo.create(db, budget)
        return self._to_response(saved)

    def update_budget(
        self,
        db: Session,
        budget_id: str,
        user_id: str,
        data: BudgetUpdateRequest,
    ) -> BudgetResponse:
        budget = self.budget_repo.get_by_id_and_user(db, budget_id, user_id)
        if not budget:
            raise ValueError("Budget not found")

        category = (
            db.query(Category)
            .filter(
                Category.CategoryID == budget.CategoryID,
                Category.IsDeleted.is_(False),
                ((Category.UserID == user_id) | (Category.UserID.is_(None)))
            )
            .first()
        )
        if not category or category.CategoryType != "expense":
            raise ValueError("Ngân sách chỉ áp dụng cho danh mục chi tiêu")

        if data.limit_amount is not None:
            budget.LimitAmount = data.limit_amount

        if data.period_type is not None:
            period = normalize_period(
                period_type=data.period_type,
                period_year=data.period_year or budget.PeriodYear,
                period_month=data.period_month,
                period_week=data.period_week,
            )
            budget.PeriodType = period["period_type"]
            budget.PeriodYear = period["period_year"]
            budget.PeriodMonth = period["period_month"]
            budget.PeriodWeek = period["period_week"]
            budget.StartDate = period["start_date"]
            budget.EndDate = period["end_date"]

            spent_query = (
                db.query(func.sum(Transaction.Amount))
                .filter(
                    Transaction.UserID == user_id,
                    Transaction.CategoryID == budget.CategoryID,
                    Transaction.TransactionType == "expense",
                    Transaction.TransactionDate >= budget.StartDate,
                    Transaction.TransactionDate <= budget.EndDate
                )
                .scalar()
            )
            budget.SpentAmount = Decimal(spent_query) if spent_query else Decimal(0)

        budget.UpdatedAt = datetime.now()
        db.commit()
        db.refresh(budget)

        return self._to_response(budget)

    def delete_budget(self, db: Session, budget_id: str, user_id: str):
        budget = self.budget_repo.get_by_id_and_user(db, budget_id, user_id)
        if not budget:
            raise ValueError("Budget not found")

        self.budget_repo.delete(db, budget)
