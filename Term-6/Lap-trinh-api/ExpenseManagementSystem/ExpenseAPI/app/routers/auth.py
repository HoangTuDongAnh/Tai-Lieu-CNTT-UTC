from fastapi import APIRouter, Depends, HTTPException, Response, status
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_user
from app.schemas.auth_schema import (
    LoginRequest,
    RegisterRequest,
    RegisterResponse,
    TokenResponse,
    UpdateProfileRequest,
    UserResponse,
)
from app.schemas.forgot_password_schema import ForgotPasswordRequest, ResetPasswordRequest
from app.schemas.otp_schema import OTPResendRequest, OTPVerifyRequest
from app.services.auth_service import AuthService
from app.services.forgot_password_service import ForgotPasswordService
from app.services.otp_service import OTPService

auth_service = AuthService()
otp_service = OTPService()
forgot_service = ForgotPasswordService()
router = APIRouter(prefix="/auth", tags=["Auth"])


@router.post("/register", response_model=RegisterResponse, status_code=status.HTTP_201_CREATED)
def register(data: RegisterRequest, db: Session = Depends(get_db)):
    try:
        result = auth_service.register(db, data)
        user = result["user"]

        return RegisterResponse(
            user=UserResponse(
                user_id=user.UserID,
                full_name=user.FullName,
                email=user.Email,
                phone_number=user.PhoneNumber,
                avatar=user.Avatar,
                role=user.Role,
                status=user.Status,
            ),
            email_sent=result["email_sent"],
            message=result["message"],
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.post("/verify-otp")
def verify_otp(data: OTPVerifyRequest, db: Session = Depends(get_db)):
    try:
        otp_service.verify_otp(db, data.email, data.otp)
        return {"message": "OTP verified successfully"}
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.post("/resend-otp")
def resend_otp(data: OTPResendRequest, db: Session = Depends(get_db)):
    try:
        result = otp_service.create_otp(db, data.email)
        return {
            "message": result["message"],
            "email_sent": result["email_sent"]
        }
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.post("/login", response_model=TokenResponse)
def login(data: LoginRequest, db: Session = Depends(get_db)):
    try:
        result = auth_service.login(db, data)
        user = result["user"]
        return TokenResponse(
            access_token=result["access_token"],
            user=UserResponse(
                user_id=user.UserID,
                full_name=user.FullName,
                email=user.Email,
                phone_number=user.PhoneNumber,
                avatar=user.Avatar,
                role=user.Role,
                status=user.Status,
            ),
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.post("/forgot-password")
def forgot_password(data: ForgotPasswordRequest, db: Session = Depends(get_db)):
    forgot_service.forgot_password(db, data.email)
    return {"message": "If the email exists, a reset link has been sent"}


@router.post("/reset-password")
def reset_password(data: ResetPasswordRequest, db: Session = Depends(get_db)):
    try:
        forgot_service.reset_password(db, data.email, data.new_password)
        return {"message": "Password reset successfully"}
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.get("/me", response_model=UserResponse)
def get_me(current_user=Depends(get_current_user)):
    return UserResponse(
        user_id=current_user.UserID,
        full_name=current_user.FullName,
        email=current_user.Email,
        phone_number=current_user.PhoneNumber,
        avatar=current_user.Avatar,
        role=current_user.Role,
        status=current_user.Status,
    )


@router.put("/me", response_model=UserResponse)
def update_me(
    data: UpdateProfileRequest,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    try:
        user = auth_service.update_profile(db, current_user, data)
        return UserResponse(
            user_id=user.UserID,
            full_name=user.FullName,
            email=user.Email,
            phone_number=user.PhoneNumber,
            avatar=user.Avatar,
            role=user.Role,
            status=user.Status,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.delete("/me", status_code=status.HTTP_204_NO_CONTENT)
def delete_me(
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    auth_service.delete_current_user(db, current_user)
    return Response(status_code=status.HTTP_204_NO_CONTENT)