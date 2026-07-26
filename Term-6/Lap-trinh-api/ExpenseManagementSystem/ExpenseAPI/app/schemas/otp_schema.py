from pydantic import BaseModel, EmailStr, Field


class OTPVerifyRequest(BaseModel):
    email: EmailStr
    otp: str = Field(..., min_length=4, max_length=6)


class OTPResendRequest(BaseModel):
    email: EmailStr
