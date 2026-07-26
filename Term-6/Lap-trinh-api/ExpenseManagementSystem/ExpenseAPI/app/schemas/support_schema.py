from datetime import datetime

from pydantic import BaseModel, Field


class SupportAttachmentCreate(BaseModel):
    file_name: str = Field(min_length=1, max_length=255)
    file_url: str = Field(min_length=1, max_length=500)
    file_type: str | None = Field(default=None, max_length=50)
    file_size: int | None = Field(default=None, ge=0)


class SupportAttachmentResponse(BaseModel):
    attachment_id: int
    file_name: str
    file_url: str
    file_type: str | None = None
    file_size: int | None = None
    created_at: datetime


class SupportRequestCreate(BaseModel):
    subject: str = Field(min_length=1, max_length=200)
    message: str = Field(min_length=1, max_length=2000)
    support_type: str = Field(pattern="^(bug|transaction|account|feature|other)$")
    priority: str = Field(default="medium", pattern="^(low|medium|high|urgent)$")
    attachments: list[SupportAttachmentCreate] = []


class SupportRequestReply(BaseModel):
    admin_reply: str | None = Field(default=None, max_length=2000)
    status: str = Field(default="replied", pattern="^(viewed|replied|closed)$")


class SupportRequestListItem(BaseModel):
    support_request_id: str
    user_id: str
    user_full_name: str = ""
    user_email: str | None = None
    subject: str
    support_type: str
    priority: str
    status: str
    created_at: datetime
    viewed_at: datetime | None = None
    replied_at: datetime | None = None
    closed_at: datetime | None = None
    has_attachments: bool = False
    message: str | None = None
    admin_reply: str | None = None


class SupportRequestDetail(BaseModel):
    support_request_id: str
    user_id: str
    user_full_name: str = ""
    user_email: str | None = None
    user_avatar: str | None = None
    subject: str
    message: str
    support_type: str
    priority: str
    status: str
    admin_reply: str | None = None
    created_at: datetime
    updated_at: datetime
    viewed_at: datetime | None = None
    replied_at: datetime | None = None
    closed_at: datetime | None = None
    attachments: list[SupportAttachmentResponse] = []
