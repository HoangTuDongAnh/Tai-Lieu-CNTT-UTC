from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_user
from app.schemas.budget_schema import BudgetCreateRequest, BudgetResponse, BudgetUpdateRequest
from app.services.budget_service import BudgetService

router = APIRouter(prefix="/budgets", tags=["Budgets"])
budget_service = BudgetService()


@router.get("", response_model=list[BudgetResponse])
def get_budgets(
    period_type: str | None = Query(default=None, pattern="^(week|month|year)$"),
    period_year: int | None = Query(default=None, ge=2000, le=2100),
    period_month: int | None = Query(default=None, ge=1, le=12),
    period_week: int | None = Query(default=None, ge=1, le=53),
    category_id: str | None = Query(default=None),
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    try:
        return budget_service.get_budgets(
            db=db,
            user_id=current_user.UserID,
            period_type=period_type,
            period_year=period_year,
            period_month=period_month,
            period_week=period_week,
            category_id=category_id,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.post("", response_model=BudgetResponse, status_code=status.HTTP_201_CREATED)
def create_budget(
    data: BudgetCreateRequest,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    try:
        return budget_service.create_budget(db, current_user.UserID, data)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.put("/{budget_id}", response_model=BudgetResponse)
def update_budget(
    budget_id: str,
    data: BudgetUpdateRequest,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    try:
        return budget_service.update_budget(db, budget_id, current_user.UserID, data)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.delete("/{budget_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_budget(
    budget_id: str,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    try:
        budget_service.delete_budget(db, budget_id, current_user.UserID)
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc))