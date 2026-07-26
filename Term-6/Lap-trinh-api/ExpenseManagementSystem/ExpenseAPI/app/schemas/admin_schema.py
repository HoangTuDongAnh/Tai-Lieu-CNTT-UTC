from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


class AdminDashboardUserItem(BaseModel):
    user_id: str
    full_name: str
    email: str
    phone_number: str | None = None
    role: str
    status: str
    avatar: str | None = None
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)


class AdminDashboardSupportRequestItem(BaseModel):
    support_request_id: str
    subject: str
    support_type: str
    priority: str
    status: str
    created_at: datetime
    user_id: str
    user_full_name: str
    user_email: str | None = None
    user_avatar: str | None = None


class AdminDashboardResponse(BaseModel):
    total_users: int
    active_users: int
    inactive_users: int
    new_users_today: int
    new_users_week: list[int] = Field(default_factory=list)
    new_users_week_labels: list[str] = Field(default_factory=list)
    new_users_week_total: int
    pending_requests: int
    high_priority_open_requests: int
    recent_users: list[AdminDashboardUserItem] = Field(default_factory=list)
    recent_pending_requests: list[AdminDashboardSupportRequestItem] = Field(default_factory=list)
    active_today: int
    new_reports: int
    last_updated_at: datetime
