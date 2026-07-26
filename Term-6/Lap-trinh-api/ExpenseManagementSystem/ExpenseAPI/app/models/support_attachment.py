from sqlalchemy import BigInteger, Column, DateTime, ForeignKey, Integer, String
from sqlalchemy.sql import func
from app.core.database import Base


class SupportAttachment(Base):
    __tablename__ = "supportattachments"

    AttachmentID = Column("attachmentid", Integer, primary_key=True, index=True, autoincrement=True)
    SupportRequestID = Column("supportrequestid", String(17),
                              ForeignKey("supportrequests.supportrequestid", ondelete="CASCADE"),
                              nullable=False, index=True)

    FileName = Column("filename", String(255), nullable=False)
    FileUrl = Column("fileurl", String(500), nullable=False)
    FileType = Column("filetype", String(50), nullable=True)
    FileSize = Column("filesize", BigInteger, nullable=True)

    CreatedAt = Column("createdat", DateTime, nullable=False, server_default=func.now())