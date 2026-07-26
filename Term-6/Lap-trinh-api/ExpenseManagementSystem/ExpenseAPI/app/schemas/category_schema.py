from datetime import date
from decimal import Decimal
from typing import Literal, Optional

from pydantic import BaseModel, ConfigDict, Field

CategoryType = Literal["income", "expense"]


class CategoryCreateRequest(BaseModel):
    category_name: str = Field(..., min_length=1, max_length=100)
    category_type: CategoryType = Field(default="expense")
    icon: Optional[str] = Field(default=None, max_length=50)
    color: Optional[str] = Field(default=None, max_length=10)


class CategoryUpdateRequest(BaseModel):
    category_name: Optional[str] = Field(default=None, min_length=1, max_length=100)
    category_type: Optional[CategoryType] = None
    icon: Optional[str] = Field(default=None, max_length=50)
    color: Optional[str] = Field(default=None, max_length=10)


class CategoryDeleteRequest(BaseModel):
    replacement_category_id: str | None = Field(default=None, min_length=1, max_length=15)


class CategoryResponse(BaseModel):
    category_id: str
    user_id: Optional[str] = None
    category_name: str
    category_type: CategoryType
    icon: Optional[str] = None
    color: Optional[str] = None
    is_default: bool

    model_config = ConfigDict(from_attributes=True)


class CategoryBudgetSummary(BaseModel):
    budget_id: str | None = None
    limit_amount: Decimal | None = None
    spent_amount: Decimal | None = None
    remaining_amount: Decimal | None = None
    percentage_used: float | None = None
    status: str = "none"

    period_type: str | None = None
    period_year: int | None = None
    period_month: int | None = None
    period_week: int | None = None
    start_date: date | None = None
    end_date: date | None = None


class CategoryOverviewResponse(BaseModel):
    category_id: str
    user_id: Optional[str] = None
    category_name: str
    category_type: CategoryType
    icon: Optional[str] = None
    color: Optional[str] = None
    is_default: bool
    can_edit: bool
    can_delete: bool
    budget: CategoryBudgetSummary
    transaction_count: int = 0
