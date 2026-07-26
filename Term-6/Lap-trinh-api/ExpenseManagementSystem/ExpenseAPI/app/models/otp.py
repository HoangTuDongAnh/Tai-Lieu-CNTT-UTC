from sqlalchemy import Boolean, Column, DateTime, Integer, String
from sqlalchemy.sql import func
from app.core.database import Base


class UserOTP(Base):
    __tablename__ = "userotps"

    OtpID = Column("otpid", Integer, primary_key=True, autoincrement=True)
    Email = Column("email", String(150), nullable=False, index=True)
    OTPCode = Column("otpcode", String(6), nullable=False)

    IsUsed = Column("isused", Boolean, nullable=False, default=False, server_default="false")

    ExpiresAt = Column("expiresat", DateTime, nullable=False)
    CreatedAt = Column("createdat", DateTime, nullable=False, server_default=func.now())