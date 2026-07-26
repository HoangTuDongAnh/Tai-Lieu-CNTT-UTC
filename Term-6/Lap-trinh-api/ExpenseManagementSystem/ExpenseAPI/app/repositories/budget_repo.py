from sqlalchemy.orm import Session

from app.models.budget import Budget


class BudgetRepository:
    def get_all_by_user(
        self,
        db: Session,
        user_id: str,
        period_type: str | None = None,
        period_year: int | None = None,
        period_month: int | None = None,
        period_week: int | None = None,
        category_id: str | None = None,
    ) -> list[Budget]:
        query = db.query(Budget).filter(Budget.UserID == user_id)

        if period_type is not None:
            query = query.filter(Budget.PeriodType == period_type)

        if period_year is not None:
            query = query.filter(Budget.PeriodYear == period_year)

        if period_month is not None:
            query = query.filter(Budget.PeriodMonth == period_month)

        if period_week is not None:
            query = query.filter(Budget.PeriodWeek == period_week)

        if category_id is not None:
            query = query.filter(Budget.CategoryID == category_id)

        return (
            query.order_by(
                Budget.PeriodYear.desc(),
                Budget.PeriodMonth.desc(),
                Budget.PeriodWeek.desc(),
                Budget.CreatedAt.desc(),
            )
            .all()
        )

    def get_by_id_and_user(self, db: Session, budget_id: str, user_id: str) -> Budget | None:
        return (
            db.query(Budget)
            .filter(Budget.BudgetID == budget_id, Budget.UserID == user_id)
            .first()
        )

    def get_existing(
        self,
        db: Session,
        user_id: str,
        category_id: str,
        period_type: str,
        period_year: int,
        period_month: int | None,
        period_week: int | None,
    ) -> Budget | None:
        query = db.query(Budget).filter(
            Budget.UserID == user_id,
            Budget.CategoryID == category_id,
            Budget.PeriodType == period_type,
            Budget.PeriodYear == period_year,
        )

        if period_type == "month":
            query = query.filter(Budget.PeriodMonth == period_month)
        elif period_type == "week":
            query = query.filter(Budget.PeriodWeek == period_week)
        else:
            query = query.filter(Budget.PeriodMonth.is_(None), Budget.PeriodWeek.is_(None))

        return query.first()

    def get_by_date_and_category(
        self,
        db: Session,
        user_id: str,
        category_id: str,
        tx_date,
    ) -> list[Budget]:
        return (
            db.query(Budget)
            .filter(
                Budget.UserID == user_id,
                Budget.CategoryID == category_id,
                Budget.StartDate <= tx_date,
                Budget.EndDate >= tx_date,
            )
            .all()
        )

    def create(self, db: Session, budget: Budget) -> Budget:
        db.add(budget)
        db.commit()
        db.refresh(budget)
        return budget

    def delete(self, db: Session, budget: Budget) -> None:
        db.delete(budget)
        db.commit()