from datetime import date

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_user
from app.schemas.report_schema import (
    BudgetProgressItem,
    CashflowAnalyticsResponse,
    CategorySummaryItem,
    DashboardOverviewResponse,
    MonthlySummaryItem,
    TopExpenseItem,
)
from app.services.report_service import ReportService

router = APIRouter(prefix="/reports", tags=["Reports"])
report_service = ReportService()


@router.get("/monthly", response_model=list[MonthlySummaryItem])
def get_monthly_summary(year: int, db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    return report_service.get_monthly_summary(db, current_user.UserID, year)


@router.get("/by-category", response_model=list[CategorySummaryItem])
def get_category_summary(
    month: int,
    year: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    return report_service.get_category_summary(db, current_user.UserID, month, year)


@router.get("/by-category-range", response_model=list[CategorySummaryItem])
def get_category_summary_range(
    start_date: date,
    end_date: date,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    return report_service.get_category_summary_range(db, current_user.UserID, start_date, end_date)


@router.get("/dashboard", response_model=DashboardOverviewResponse)
def get_dashboard_overview(db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    return report_service.get_dashboard_overview(db, current_user.UserID)


@router.get("/dashboard-range", response_model=DashboardOverviewResponse)
def get_dashboard_overview_range(
    start_date: date,
    end_date: date,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    return report_service.get_dashboard_overview_range(db, current_user.UserID, start_date, end_date)


@router.get("/cashflow-analytics", response_model=CashflowAnalyticsResponse)
def get_cashflow_analytics(
    start_date: date,
    end_date: date,
    granularity: str = Query(default="day", pattern="^(day|week|month|year)$"),
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    return report_service.get_cashflow_analytics(db, current_user.UserID, start_date, end_date, granularity)


@router.get("/top-expenses", response_model=list[TopExpenseItem])
def get_top_expenses(
    month: int,
    year: int,
    limit: int = Query(default=5, ge=1, le=50),
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    return report_service.get_top_expenses(db, current_user.UserID, month, year, limit)


@router.get("/top-expenses-range", response_model=list[TopExpenseItem])
def get_top_expenses_range(
    start_date: date,
    end_date: date,
    limit: int = Query(default=5, ge=1, le=50),
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    return report_service.get_top_expenses_range(db, current_user.UserID, start_date, end_date, limit)


@router.get("/budget-progress", response_model=list[BudgetProgressItem])
def get_budget_progress(
    month: int,
    year: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    return report_service.get_budget_progress(db, current_user.UserID, month, year)
