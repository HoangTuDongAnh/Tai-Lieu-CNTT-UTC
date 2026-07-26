from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_user
from app.schemas.support_schema import SupportRequestCreate, SupportRequestDetail, SupportRequestListItem
from app.services.support_service import SupportService

router = APIRouter(prefix="/support-requests", tags=["Support"])
support_service = SupportService()


@router.post("", response_model=SupportRequestDetail)
def create_support_request(
    payload: SupportRequestCreate,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    return support_service.create_request(db, current_user.UserID, payload)


@router.get("/my", response_model=list[SupportRequestListItem])
def get_my_support_requests(
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    return support_service.get_my_requests(db, current_user.UserID)


@router.get("/my/{support_request_id}", response_model=SupportRequestDetail)
def get_my_support_request_detail(
    support_request_id: str,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    return support_service.get_my_request_detail(db, current_user.UserID, support_request_id)
