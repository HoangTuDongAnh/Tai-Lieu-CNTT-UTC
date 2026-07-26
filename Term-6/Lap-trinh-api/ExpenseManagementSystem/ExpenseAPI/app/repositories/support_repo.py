from datetime import datetime

from sqlalchemy import func, or_, text
from sqlalchemy.orm import Session

from app.models.support_attachment import SupportAttachment
from app.models.support_request import SupportRequest
from app.models.user import User


class SupportRepository:
    def _generate_support_request_id(self, db: Session) -> str:
        sql = text("SELECT fn_GenerateSupportRequestID() AS NewID")
        row = db.execute(sql).mappings().first()
        return row["newid"]

    def create_request(self, db: Session, payload: dict, user_id: str) -> SupportRequest:
        new_id = self._generate_support_request_id(db)
        item = SupportRequest(
            SupportRequestID=new_id,
            UserID=user_id,
            Subject=payload["subject"].strip(),
            Message=payload["message"].strip(),
            SupportType=payload["support_type"],
            Priority=payload.get("priority", "medium"),
            Status="pending",
        )
        db.add(item)
        db.commit()
        db.refresh(item)
        return item

    def add_attachments(self, db: Session, support_request_id: str, attachments: list[dict]) -> list[SupportAttachment]:
        output = []
        for attachment in attachments:
            entity = SupportAttachment(
                SupportRequestID=support_request_id,
                FileName=attachment["file_name"],
                FileUrl=attachment["file_url"],
                FileType=attachment.get("file_type"),
                FileSize=attachment.get("file_size"),
            )
            db.add(entity)
            output.append(entity)
        db.commit()
        return output

    def get_by_id(self, db: Session, support_request_id: str) -> SupportRequest | None:
        return db.query(SupportRequest).filter(SupportRequest.SupportRequestID == support_request_id).first()

    def get_user_request(self, db: Session, support_request_id: str, user_id: str) -> SupportRequest | None:
        return (
            db.query(SupportRequest)
            .filter(
                SupportRequest.SupportRequestID == support_request_id,
                SupportRequest.UserID == user_id,
            )
            .first()
        )

    def get_user_brief(self, db: Session, user_id: str) -> User | None:
        return db.query(User).filter(User.UserID == user_id).first()

    def list_by_user(self, db: Session, user_id: str) -> list[SupportRequest]:
        return (
            db.query(SupportRequest)
            .filter(SupportRequest.UserID == user_id)
            .order_by(SupportRequest.CreatedAt.desc())
            .all()
        )

    def list_for_admin(
        self,
        db: Session,
        status: str | None = None,
        support_type: str | None = None,
        priority: str | None = None,
        keyword: str | None = None,
        page: int = 1,
        page_size: int = 20,
    ) -> list[SupportRequest]:
        query = db.query(SupportRequest)

        if status:
            query = query.filter(SupportRequest.Status == status)
        if support_type:
            query = query.filter(SupportRequest.SupportType == support_type)
        if priority:
            query = query.filter(SupportRequest.Priority == priority)
        if keyword:
            keyword_value = f"%{keyword.strip()}%"
            query = query.join(User, User.UserID == SupportRequest.UserID).filter(
                or_(
                    SupportRequest.Subject.ilike(keyword_value),
                    User.FullName.ilike(keyword_value),
                    User.Email.ilike(keyword_value),
                )
            )

        page = max(page, 1)
        page_size = min(max(page_size, 1), 100)
        return (
            query.order_by(SupportRequest.CreatedAt.desc())
            .offset((page - 1) * page_size)
            .limit(page_size)
            .all()
        )

    def list_attachments(self, db: Session, support_request_id: str) -> list[SupportAttachment]:
        return (
            db.query(SupportAttachment)
            .filter(SupportAttachment.SupportRequestID == support_request_id)
            .order_by(SupportAttachment.CreatedAt.asc(), SupportAttachment.AttachmentID.asc())
            .all()
        )

    def has_attachments(self, db: Session, support_request_id: str) -> bool:
        count = (
            db.query(func.count(SupportAttachment.AttachmentID))
            .filter(SupportAttachment.SupportRequestID == support_request_id)
            .scalar()
        )
        return bool(count)

    def mark_viewed(self, db: Session, item: SupportRequest) -> SupportRequest:
        if item.Status == "pending":
            item.Status = "viewed"
        if item.ViewedAt is None:
            item.ViewedAt = datetime.now()
        item.UpdatedAt = datetime.now()
        db.commit()
        db.refresh(item)
        return item

    def reply(self, db: Session, item: SupportRequest, reply_text: str | None, status_value: str = "replied") -> SupportRequest:
        now = datetime.now()

        if reply_text is not None:
            item.AdminReply = reply_text.strip()

        if item.ViewedAt is None:
            item.ViewedAt = now

        if status_value == "viewed":
            item.Status = "viewed"
        elif status_value == "closed":
            item.Status = "closed"
            item.ClosedAt = now
            if reply_text:
                item.RepliedAt = now
        else:
            item.Status = "replied"
            item.RepliedAt = now

        item.UpdatedAt = now
        db.commit()
        db.refresh(item)
        return item

    def close(self, db: Session, item: SupportRequest) -> SupportRequest:
        now = datetime.now()
        item.Status = "closed"
        if item.ViewedAt is None:
            item.ViewedAt = now
        item.ClosedAt = now
        item.UpdatedAt = now
        db.commit()
        db.refresh(item)
        return item

    def count_by_status(self, db: Session, status: str) -> int:
        return (
            db.query(func.count(SupportRequest.SupportRequestID))
            .filter(SupportRequest.Status == status)
            .scalar()
            or 0
        )

    def count_open_high_priority(self, db: Session) -> int:
        return (
            db.query(func.count(SupportRequest.SupportRequestID))
            .filter(
                SupportRequest.Status.in_(["pending", "viewed"]),
                SupportRequest.Priority.in_(["high", "urgent"]),
            )
            .scalar()
            or 0
        )

    def get_recent_open_requests_with_user(self, db: Session, limit: int = 6):
        return (
            db.query(SupportRequest, User)
            .join(User, User.UserID == SupportRequest.UserID)
            .filter(SupportRequest.Status.in_(["pending", "viewed"]))
            .order_by(SupportRequest.CreatedAt.desc(), SupportRequest.SupportRequestID.desc())
            .limit(limit)
            .all()
        )

