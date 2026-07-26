from sqlalchemy.orm import Session
from fastapi import HTTPException

from app.repositories.user_repo import UserRepository
from app.services.email_service import EmailService
from app.core.security import create_reset_token, decode_reset_token, hash_password


class ForgotPasswordService:
    def __init__(self):
        self.user_repo = UserRepository()
        self.email_service = EmailService()

    # ================= FORGOT PASSWORD =================
    def forgot_password(self, db: Session, email: str):
        user = self.user_repo.get_by_email(db, email)

        # Không báo lỗi để tránh lộ email
        if not user:
            return

        token = create_reset_token(email)

        self.email_service.send_reset_password_email(email, token)

    # ================= RESET PASSWORD =================
    def reset_password(self, db: Session, token: str, new_password: str):
        try:
            email = decode_reset_token(token)
        except Exception:
            raise HTTPException(400, "Invalid or expired token")

        user = self.user_repo.get_by_email(db, email)
        if not user:
            raise HTTPException(404, "User not found")

        # validate password (optional)
        if len(new_password) < 6:
            raise HTTPException(400, "Password must be at least 6 characters")

        user.PasswordHash = hash_password(new_password)

        db.commit()