from datetime import datetime

from sqlalchemy.orm import Session

from app.repositories.support_repo import SupportRepository
from app.repositories.user_repo import UserRepository


class AdminService:
    def __init__(self):
        self.user_repo = UserRepository()
        self.support_repo = SupportRepository()

    def get_dashboard_data(self, db: Session):
        now = datetime.now()
        today = now.date()

        total_users = self.user_repo.count_all(db)
        active_users = self.user_repo.count_active(db)
        inactive_users = self.user_repo.count_inactive(db)
        new_users_today = self.user_repo.count_created_on_date(db, today)

        pending_requests = self.support_repo.count_by_status(db, "pending")
        high_priority_open_requests = self.support_repo.count_open_high_priority(db)

        trend_rows = self.user_repo.get_new_users_trend(db, today, days=7)
        new_users_week = [count for _, count in trend_rows]
        new_users_week_labels = [target_date.strftime("%d/%m") for target_date, _ in trend_rows]

        recent_users_db = self.user_repo.get_recent_users(db, limit=12)
        recent_users = [
            {
                "user_id": user.UserID,
                "full_name": user.FullName,
                "email": user.Email,
                "phone_number": user.PhoneNumber,
                "role": user.Role,
                "status": user.Status,
                "avatar": user.Avatar,
                "created_at": user.CreatedAt,
            }
            for user in recent_users_db
        ]

        recent_pending_requests_db = self.support_repo.get_recent_open_requests_with_user(db, limit=6)
        recent_pending_requests = [
            {
                "support_request_id": request.SupportRequestID,
                "subject": request.Subject,
                "support_type": request.SupportType,
                "priority": request.Priority,
                "status": request.Status,
                "created_at": request.CreatedAt,
                "user_id": user.UserID,
                "user_full_name": user.FullName,
                "user_email": user.Email,
                "user_avatar": user.Avatar,
            }
            for request, user in recent_pending_requests_db
        ]

        return {
            "total_users": total_users,
            "active_users": active_users,
            "inactive_users": inactive_users,
            "new_users_today": new_users_today,
            "new_users_week": new_users_week,
            "new_users_week_labels": new_users_week_labels,
            "new_users_week_total": sum(new_users_week),
            "pending_requests": pending_requests,
            "high_priority_open_requests": high_priority_open_requests,
            "recent_users": recent_users,
            "recent_pending_requests": recent_pending_requests,
            "active_today": active_users,
            "new_reports": pending_requests,
            "last_updated_at": now,
        }
