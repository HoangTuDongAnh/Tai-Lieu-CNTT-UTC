from fastapi import HTTPException, status
from sqlalchemy.orm import Session

from app.repositories.support_repo import SupportRepository


class SupportService:
    def __init__(self):
        self.repo = SupportRepository()

    def _to_attachment_response(self, item):
        return {
            "attachment_id": item.AttachmentID,
            "file_name": item.FileName,
            "file_url": item.FileUrl,
            "file_type": item.FileType,
            "file_size": item.FileSize,
            "created_at": item.CreatedAt,
        }

    def _to_list_item(self, db: Session, item):
        user = self.repo.get_user_brief(db, item.UserID)
        return {
            "support_request_id": item.SupportRequestID,
            "user_id": item.UserID,
            "user_full_name": user.FullName if user else "",
            "user_email": user.Email if user else None,
            "subject": item.Subject,
            "message": item.Message,
            "admin_reply": item.AdminReply,
            "support_type": item.SupportType,
            "priority": item.Priority,
            "status": item.Status,
            "created_at": item.CreatedAt,
            "viewed_at": item.ViewedAt,
            "replied_at": item.RepliedAt,
            "closed_at": item.ClosedAt,
            "has_attachments": self.repo.has_attachments(db, item.SupportRequestID),
        }

    def _to_detail(self, db: Session, item, attachments):
        user = self.repo.get_user_brief(db, item.UserID)
        return {
            "support_request_id": item.SupportRequestID,
            "user_id": item.UserID,
            "user_full_name": user.FullName if user else "",
            "user_email": user.Email if user else None,
            "user_avatar": user.Avatar if user else None,
            "subject": item.Subject,
            "message": item.Message,
            "support_type": item.SupportType,
            "priority": item.Priority,
            "status": item.Status,
            "admin_reply": item.AdminReply,
            "created_at": item.CreatedAt,
            "updated_at": item.UpdatedAt,
            "viewed_at": item.ViewedAt,
            "replied_at": item.RepliedAt,
            "closed_at": item.ClosedAt,
            "attachments": [self._to_attachment_response(x) for x in attachments],
        }

    def create_request(self, db: Session, user_id: str, payload):
        item = self.repo.create_request(db, payload.model_dump(), user_id)
        if payload.attachments:
            self.repo.add_attachments(
                db,
                item.SupportRequestID,
                [x.model_dump() for x in payload.attachments],
            )
        attachments = self.repo.list_attachments(db, item.SupportRequestID)
        return self._to_detail(db, item, attachments)

    def get_my_requests(self, db: Session, user_id: str):
        items = self.repo.list_by_user(db, user_id)
        return [self._to_list_item(db, x) for x in items]

    def get_my_request_detail(self, db: Session, user_id: str, support_request_id: str):
        item = self.repo.get_user_request(db, support_request_id, user_id)
        if not item:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Support request not found")
        attachments = self.repo.list_attachments(db, item.SupportRequestID)
        return self._to_detail(db, item, attachments)

    def admin_list_requests(self, db: Session, status: str | None, support_type: str | None, priority: str | None, keyword: str | None = None, page: int = 1, page_size: int = 20):
        items = self.repo.list_for_admin(
            db,
            status=status,
            support_type=support_type,
            priority=priority,
            keyword=keyword,
            page=page,
            page_size=page_size,
        )
        return [self._to_list_item(db, x) for x in items]

    def admin_get_detail(self, db: Session, support_request_id: str):
        item = self.repo.get_by_id(db, support_request_id)
        if not item:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Support request not found")
        attachments = self.repo.list_attachments(db, item.SupportRequestID)
        return self._to_detail(db, item, attachments)

    def admin_mark_viewed(self, db: Session, support_request_id: str):
        item = self.repo.get_by_id(db, support_request_id)
        if not item:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Support request not found")
        item = self.repo.mark_viewed(db, item)
        attachments = self.repo.list_attachments(db, item.SupportRequestID)
        return self._to_detail(db, item, attachments)

    def admin_reply(self, db: Session, support_request_id: str, reply_text: str | None, status_value: str = "replied"):
        item = self.repo.get_by_id(db, support_request_id)
        if not item:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Support request not found")
        item = self.repo.reply(db, item, reply_text, status_value)
        attachments = self.repo.list_attachments(db, item.SupportRequestID)
        return self._to_detail(db, item, attachments)

    def admin_close(self, db: Session, support_request_id: str):
        item = self.repo.get_by_id(db, support_request_id)
        if not item:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Support request not found")
        item = self.repo.close(db, item)
        attachments = self.repo.list_attachments(db, item.SupportRequestID)
        return self._to_detail(db, item, attachments)
