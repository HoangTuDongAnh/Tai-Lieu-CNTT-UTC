from sqlalchemy import Column, DateTime, ForeignKey, String
from sqlalchemy.sql import func
from app.core.database import Base


class SupportRequest(Base):
    __tablename__ = "supportrequests"

    SupportRequestID = Column("supportrequestid", String(17), primary_key=True, index=True)
    UserID = Column("userid", String(15),
                    ForeignKey("users.userid", ondelete="CASCADE"),
                    nullable=False, index=True)

    Subject = Column("subject", String(200), nullable=False)
    Message = Column("message", String(2000), nullable=False)
    SupportType = Column("supporttype", String(20), nullable=False)

    Priority = Column("priority", String(10), nullable=False, default="medium")
    Status = Column("status", String(20), nullable=False, default="pending")
    AdminReply = Column("adminreply", String(2000), nullable=True)

    CreatedAt = Column("createdat", DateTime, nullable=False, server_default=func.now())
    UpdatedAt = Column("updatedat", DateTime, nullable=False,
                       server_default=func.now(), onupdate=func.now())

    ViewedAt = Column("viewedat", DateTime, nullable=True)
    RepliedAt = Column("repliedat", DateTime, nullable=True)
    ClosedAt = Column("closedat", DateTime, nullable=True)