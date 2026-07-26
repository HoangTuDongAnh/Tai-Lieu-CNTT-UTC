import random
from datetime import datetime, timedelta
from sqlalchemy.orm import Session

from app.models.otp import UserOTP
from app.repositories.otp_repo import OTPRepository
from app.repositories.user_repo import UserRepository
from app.services.email_service import EmailDeliveryError, EmailService


class OTPService:
    def __init__(self):
        self.otp_repo = OTPRepository()
        self.user_repo = UserRepository()
        self.email_service = EmailService()

    def _generate_code(self) -> str:
        return f"{random.randint(0, 999999):06d}"

    def create_otp(self, db: Session, email: str) -> dict:
        user = self.user_repo.get_by_email(db, email)
        if not user:
            raise ValueError("User not found")

        self.otp_repo.invalidate_active_by_email(db, email)

        code = self._generate_code()
        otp = UserOTP(
            Email=email,
            OTPCode=code,
            IsUsed=False,
            ExpiresAt=datetime.utcnow() + timedelta(minutes=5),
        )
        self.otp_repo.create(db, otp)

        try:
            self.email_service.send_otp_email(email, code)
            return {
                "email_sent": True,
                "message": "OTP has been sent successfully."
            }
        except EmailDeliveryError as exc:
            return {
                "email_sent": False,
                "message": str(exc)
            }

    def verify_otp(self, db: Session, email: str, code: str) -> None:
        otp = self.otp_repo.get_valid_otp(db, email, code)
        if not otp:
            raise ValueError("Invalid or expired OTP")

        self.otp_repo.mark_used(db, otp)
        user = self.user_repo.get_by_email(db, email)
        if user:
            user.Status = "active"
            self.user_repo.update(db, user)