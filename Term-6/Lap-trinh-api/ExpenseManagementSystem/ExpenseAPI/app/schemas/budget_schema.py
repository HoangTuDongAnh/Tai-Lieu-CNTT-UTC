from datetime import date
from decimal import Decimal

from pydantic import BaseModel, ConfigDict, Field


class BudgetCreateRequest(BaseModel):
    category_id: str = Field(..., min_length=1, max_length=15)
    limit_amount: Decimal = Field(..., gt=0)

    period_type: str = Field(..., pattern="^(week|month|year)$")
    period_year: int = Field(..., ge=2000, le=2100)
    period_month: int | None = Field(default=None, ge=1, le=12)
    period_week: int | None = Field(default=None, ge=1, le=53)


class BudgetUpdateRequest(BaseModel):
    limit_amount: Decimal | None = Field(default=None, gt=0)
    period_type: str | None = Field(default=None, pattern="^(week|month|year)$")
    period_year: int | None = Field(default=None, ge=2000, le=2100)
    period_month: int | None = Field(default=None, ge=1, le=12)
    period_week: int | None = Field(default=None, ge=1, le=53)


class BudgetResponse(BaseModel):
    budget_id: str
    user_id: str
    category_id: str

    limit_amount: Decimal
    spent_amount: Decimal

    period_type: str
    period_year: int
    period_month: int | None = None
    period_week: int | None = None

    start_date: date
    end_date: date

    remaining_amount: Decimal
    percentage_used: float
    status: str

    model_config = ConfigDict(from_attributes=True)