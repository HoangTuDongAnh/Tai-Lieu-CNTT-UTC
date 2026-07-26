from datetime import datetime

from sqlalchemy.orm import Session

from app.core.security import create_access_token, hash_password, verify_password
from app.models.user import User
from app.repositories.user_repo import UserRepository
from app.schemas.auth_schema import LoginRequest, RegisterRequest, UpdateProfileRequest
from app.services.otp_service import OTPService


class AuthService:
    def __init__(self):
        self.user_repo = UserRepository()
        self.otp_service = OTPService()

    def _generate_user_id(self, db: Session) -> str:
        date_part = datetime.now().strftime("%y%m%d")
        prefix = f"U{date_part}"

        last_user = (
            db.query(User)
            .filter(User.UserID.like(f"{prefix}%"))
            .order_by(User.UserID.desc())
            .first()
        )

        last_seq = int(last_user.UserID[-4:]) if last_user else 0
        return f"{prefix}{last_seq + 1:04d}"

    def register(self, db: Session, data: RegisterRequest):
        existing_user = self.user_repo.get_by_email(db, data.email)
        if existing_user:
            if existing_user.Status == "active":
                raise ValueError("Email already exists")

            otp_result = self.otp_service.create_otp(db, data.email)
            return {
                "user": existing_user,
                "email_sent": otp_result["email_sent"],
                "message": (
                    "Tài khoản đã tồn tại nhưng chưa xác thực. Một mã OTP mới đã được tạo. "
                    + otp_result["message"]
                ),
            }

        user = User(
            UserID=self._generate_user_id(db),
            FullName=data.full_name,
            Email=data.email,
            PasswordHash=hash_password(data.password),
            PhoneNumber=data.phone_number,
            Avatar=data.avatar,
            Role="user",
            Status="inactive",
        )
        self.user_repo.create(db, user)

        otp_result = self.otp_service.create_otp(db, data.email)

        return {
            "user": user,
            "email_sent": otp_result["email_sent"],
            "message": otp_result["message"],
        }

    def login(self, db: Session, data: LoginRequest):
        user = self.user_repo.get_by_email(db, data.email)
        if not user or user.Status != "active" or not verify_password(data.password, user.PasswordHash):
            raise ValueError("Invalid email or password")

        token = create_access_token({"sub": user.UserID, "email": user.Email, "role": user.Role})
        return {"access_token": token, "user": user}

    def update_profile(self, db: Session, current_user: User, data: UpdateProfileRequest):
        existing_user = self.user_repo.get_by_email(db, data.email)
        if existing_user and existing_user.UserID != current_user.UserID:
            raise ValueError("Email already exists")

        current_user.FullName = data.full_name.strip()
        current_user.Email = data.email
        current_user.PhoneNumber = data.phone_number.strip() if data.phone_number else None
        current_user.Avatar = data.avatar.strip() if data.avatar else None
        current_user.UpdatedAt = datetime.now()
        return self.user_repo.update(db, current_user)

    def delete_current_user(self, db: Session, current_user: User):
        self.user_repo.delete(db, current_user)