from datetime import datetime
from sqlalchemy.orm import Session

from app.models.otp import UserOTP


class OTPRepository:
    def invalidate_active_by_email(self, db: Session, email: str) -> None:
        db.query(UserOTP).filter(
            UserOTP.Email == email,
            UserOTP.IsUsed == False,
            UserOTP.ExpiresAt > datetime.utcnow(),
        ).update({UserOTP.IsUsed: True}, synchronize_session=False)
        db.commit()

    def create(self, db: Session, otp: UserOTP) -> UserOTP:
        db.add(otp)
        db.commit()
        db.refresh(otp)
        return otp

    def get_valid_otp(self, db: Session, email: str, code: str) -> UserOTP | None:
        return (
            db.query(UserOTP)
            .filter(
                UserOTP.Email == email,
                UserOTP.OTPCode == code,
                UserOTP.IsUsed == False,
                UserOTP.ExpiresAt > datetime.utcnow(),
            )
            .order_by(UserOTP.CreatedAt.desc())
            .first()
        )

    def mark_used(self, db: Session, otp: UserOTP) -> None:
        otp.IsUsed = True
        db.commit()
