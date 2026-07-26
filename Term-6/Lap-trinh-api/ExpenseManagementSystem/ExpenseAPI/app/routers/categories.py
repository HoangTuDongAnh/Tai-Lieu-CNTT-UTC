from fastapi import APIRouter, Body, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_user
from app.schemas.category_schema import (
    CategoryCreateRequest,
    CategoryDeleteRequest,
    CategoryOverviewResponse,
    CategoryResponse,
    CategoryUpdateRequest,
)
from app.services.category_service import CategoryService

router = APIRouter(prefix="/categories", tags=["Categories"])
category_service = CategoryService()


@router.get("", response_model=list[CategoryResponse])
def get_categories(
    include_deleted: bool = Query(False, description="Bao gồm cả danh mục đã xóa mềm"),
    category_type: str | None = Query(default=None, pattern="^(income|expense)$"),
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user)
):
    return category_service.get_categories_response(
        db,
        current_user.UserID,
        include_deleted,
        category_type,
    )


@router.get("/overview", response_model=list[CategoryOverviewResponse])
def get_categories_overview(
    period_type: str = Query(..., pattern="^(week|month|year)$"),
    period_year: int = Query(..., ge=2000, le=2100),
    period_month: int | None = Query(default=None, ge=1, le=12),
    period_week: int | None = Query(default=None, ge=1, le=53),
    category_type: str | None = Query(default=None, pattern="^(income|expense)$"),
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    try:
        return category_service.get_categories_overview(
            db=db,
            user_id=current_user.UserID,
            period_type=period_type,
            period_year=period_year,
            period_month=period_month,
            period_week=period_week,
            category_type=category_type,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.post("", response_model=CategoryResponse, status_code=status.HTTP_201_CREATED)
def create_category(
    data: CategoryCreateRequest,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    try:
        category = category_service.create_category(db, current_user.UserID, data)
        return CategoryResponse(
            category_id=category.CategoryID,
            user_id=category.UserID,
            category_name=category.CategoryName,
            category_type=category.CategoryType,
            icon=category.Icon,
            color=category.Color,
            is_default=category.IsDefault,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.put("/{category_id}", response_model=CategoryResponse)
def update_category(
    category_id: str,
    data: CategoryUpdateRequest,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    try:
        category = category_service.update_category(db, category_id, current_user.UserID, data)
        return CategoryResponse(
            category_id=category.CategoryID,
            user_id=category.UserID,
            category_name=category.CategoryName,
            category_type=category.CategoryType,
            icon=category.Icon,
            color=category.Color,
            is_default=category.IsDefault,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.delete("/{category_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_category(
    category_id: str,
    data: CategoryDeleteRequest = Body(...),
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    try:
        category_service.delete_category(db, category_id, current_user.UserID, data)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
