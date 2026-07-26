from fastapi import APIRouter, Body, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_user
from app.schemas.wallet_schema import (
    WalletCreateRequest,
    WalletDeleteRequest,
    WalletResponse,
    WalletUpdateRequest,
)
from app.services.wallet_service import WalletService

router = APIRouter(prefix="/wallets", tags=["Wallets"])
wallet_service = WalletService()


@router.get("", response_model=list[WalletResponse])
def get_wallets(db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    wallets = wallet_service.get_wallets(db, current_user.UserID)
    return [
        WalletResponse(
            wallet_id=w.WalletID,
            user_id=w.UserID,
            wallet_name=w.WalletName,
            initial_balance=w.InitialBalance,
            current_balance=w.CurrentBalance,
            currency=w.Currency,
            is_default=w.IsDefault,
        )
        for w in wallets
    ]


@router.post("", response_model=WalletResponse, status_code=status.HTTP_201_CREATED)
def create_wallet(
    data: WalletCreateRequest,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    try:
        wallet = wallet_service.create_wallet(db, current_user.UserID, data)
        return WalletResponse(
            wallet_id=wallet.WalletID,
            user_id=wallet.UserID,
            wallet_name=wallet.WalletName,
            initial_balance=wallet.InitialBalance,
            current_balance=wallet.CurrentBalance,
            currency=wallet.Currency,
            is_default=wallet.IsDefault,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.put("/{wallet_id}", response_model=WalletResponse)
def update_wallet(
    wallet_id: str,
    data: WalletUpdateRequest,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    try:
        wallet = wallet_service.update_wallet(db, wallet_id, current_user.UserID, data)
        return WalletResponse(
            wallet_id=wallet.WalletID,
            user_id=wallet.UserID,
            wallet_name=wallet.WalletName,
            initial_balance=wallet.InitialBalance,
            current_balance=wallet.CurrentBalance,
            currency=wallet.Currency,
            is_default=wallet.IsDefault,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.delete("/{wallet_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_wallet(
    wallet_id: str,
    data: WalletDeleteRequest = Body(...),
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    try:
        wallet_service.delete_wallet(db, wallet_id, current_user.UserID, data)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))