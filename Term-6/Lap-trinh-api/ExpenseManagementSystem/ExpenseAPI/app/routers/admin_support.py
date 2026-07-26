from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.role_dependencies import require_admin
from app.schemas.support_schema import SupportRequestDetail, SupportRequestListItem, SupportRequestReply
from app.services.support_service import SupportService

router = APIRouter(prefix="/admin/support-requests", tags=["Admin Support"])
support_service = SupportService()


@router.get("", response_model=list[SupportRequestListItem])
def get_support_requests(
    status: str | None = Query(default=None, pattern="^(pending|viewed|replied|closed)$"),
    support_type: str | None = Query(default=None, pattern="^(bug|transaction|account|feature|other)$"),
    priority: str | None = Query(default=None, pattern="^(low|medium|high|urgent)$"),
    keyword: str | None = Query(default=None),
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=20, ge=1, le=100),
    db: Session = Depends(get_db),
    current_user=Depends(require_admin),
):
    return support_service.admin_list_requests(db, status, support_type, priority, keyword, page, page_size)


@router.get("/{support_request_id}", response_model=SupportRequestDetail)
def get_support_request_detail(
    support_request_id: str,
    db: Session = Depends(get_db),
    current_user=Depends(require_admin),
):
    return support_service.admin_get_detail(db, support_request_id)


@router.patch("/{support_request_id}/view", response_model=SupportRequestDetail)
def mark_support_request_viewed(
    support_request_id: str,
    db: Session = Depends(get_db),
    current_user=Depends(require_admin),
):
    return support_service.admin_mark_viewed(db, support_request_id)


@router.patch("/{support_request_id}/reply", response_model=SupportRequestDetail)
def reply_support_request(
    support_request_id: str,
    payload: SupportRequestReply,
    db: Session = Depends(get_db),
    current_user=Depends(require_admin),
):
    return support_service.admin_reply(db, support_request_id, payload.admin_reply, payload.status)


@router.patch("/{support_request_id}/close", response_model=SupportRequestDetail)
def close_support_request(
    support_request_id: str,
    db: Session = Depends(get_db),
    current_user=Depends(require_admin),
):
    return support_service.admin_close(db, support_request_id)
