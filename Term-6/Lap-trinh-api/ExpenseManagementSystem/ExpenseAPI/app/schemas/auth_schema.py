from typing import Optional

from pydantic import BaseModel, ConfigDict, EmailStr, Field, field_validator


class RegisterRequest(BaseModel):
    full_name: str = Field(..., min_length=2, max_length=100)
    email: EmailStr
    password: str = Field(..., min_length=6, max_length=100)
    phone_number: Optional[str] = Field(default=None, max_length=15)
    avatar: Optional[str] = Field(default=None, max_length=255)
    agree_terms: bool = Field(...)

    @field_validator("agree_terms")
    @classmethod
    def must_agree(cls, v):
        if not v:
            raise ValueError("Bạn phải đồng ý với điều khoản sử dụng")
        return v


class LoginRequest(BaseModel):
    email: EmailStr
    password: str = Field(..., min_length=6, max_length=100)


class UpdateProfileRequest(BaseModel):
    full_name: str = Field(..., min_length=2, max_length=100)
    email: EmailStr
    phone_number: Optional[str] = Field(default=None, max_length=15)
    avatar: Optional[str] = Field(default=None, max_length=255)


class UserResponse(BaseModel):
    user_id: str
    full_name: str
    email: EmailStr
    phone_number: Optional[str] = None
    avatar: Optional[str] = None
    role: str
    status: str

    model_config = ConfigDict(from_attributes=True)


class RegisterResponse(BaseModel):
    user: UserResponse
    email_sent: bool
    message: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: UserResponse