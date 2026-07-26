from datetime import date
from decimal import Decimal
from pydantic import BaseModel


class MonthlySummaryItem(BaseModel):
    month: int
    total_income: Decimal
    total_expense: Decimal


class CategorySummaryItem(BaseModel):
    category_id: str
    category_name: str
    icon: str | None = None
    color: str | None = None
    total_amount: Decimal
    percentage: Decimal


class DashboardOverviewResponse(BaseModel):
    total_balance: Decimal
    monthly_income: Decimal
    monthly_expense: Decimal
    transaction_count: int


class TopExpenseItem(BaseModel):
    transaction_id: str
    transaction_date: date
    amount: Decimal
    note: str | None = None
    wallet_id: str
    wallet_name: str
    category_id: str
    category_name: str
    category_icon: str | None = None
    category_color: str | None = None


class BudgetProgressItem(BaseModel):
    budget_id: str
    category_id: str
    category_name: str
    category_icon: str | None = None
    category_color: str | None = None
    limit_amount: Decimal
    spent_amount: Decimal
    remaining_amount: Decimal
    percentage_used: Decimal
    status: str


class CashflowSeriesItem(BaseModel):
    key: str
    label: str
    income: Decimal
    expense: Decimal
    net: Decimal
    running_balance: Decimal


class CashflowAnalyticsResponse(BaseModel):
    start_date: date
    end_date: date
    granularity: str
    total_income: Decimal
    total_expense: Decimal
    net_change: Decimal
    transaction_count: int
    average_expense: Decimal
    busiest_label: str | None = None
    series: list[CashflowSeriesItem]
