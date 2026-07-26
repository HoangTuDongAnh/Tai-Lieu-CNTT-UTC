from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.dependencies import get_current_user
from app.schemas.transaction_schema import (
    TransactionCreateRequest,
    TransactionResponse,
    TransactionUpdateRequest, TransferResponse, TransferCreateRequest,
)
from app.services.transaction_service import TransactionService

router = APIRouter(prefix="/transactions", tags=["Transactions"])
transaction_service = TransactionService()


@router.get("", response_model=list[TransactionResponse])
def get_transactions(db: Session = Depends(get_db), current_user=Depends(get_current_user)):
    transactions = transaction_service.get_transactions(db, current_user.UserID)
    return [
        TransactionResponse(
            transaction_id=t.TransactionID,
            user_id=t.UserID,
            wallet_id=t.WalletID,
            category_id=t.CategoryID,
            transaction_type=t.TransactionType,
            amount=t.Amount,
            transaction_date=t.TransactionDate,
            note=t.Note,
            is_recurring=t.IsRecurring,
            recur_interval=t.RecurInterval,
        )
        for t in transactions
    ]


@router.post("", response_model=TransactionResponse, status_code=status.HTTP_201_CREATED)
def create_transaction(
    data: TransactionCreateRequest,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    try:
        t = transaction_service.create_transaction(db, current_user.UserID, data)
        return TransactionResponse(
            transaction_id=t.TransactionID,
            user_id=t.UserID,
            wallet_id=t.WalletID,
            category_id=t.CategoryID,
            transaction_type=t.TransactionType,
            amount=t.Amount,
            transaction_date=t.TransactionDate,
            note=t.Note,
            is_recurring=t.IsRecurring,
            recur_interval=t.RecurInterval,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.put("/{transaction_id}", response_model=TransactionResponse)
def update_transaction(
    transaction_id: str,
    data: TransactionUpdateRequest,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    try:
        t = transaction_service.update_transaction(db, transaction_id, current_user.UserID, data)
        return TransactionResponse(
            transaction_id=t.TransactionID,
            user_id=t.UserID,
            wallet_id=t.WalletID,
            category_id=t.CategoryID,
            transaction_type=t.TransactionType,
            amount=t.Amount,
            transaction_date=t.TransactionDate,
            note=t.Note,
            is_recurring=t.IsRecurring,
            recur_interval=t.RecurInterval,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.delete("/{transaction_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_transaction(
    transaction_id: str,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    try:
        transaction_service.delete_transaction(db, transaction_id, current_user.UserID)
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc))


@router.post("/transfer", response_model=TransferResponse, status_code=status.HTTP_201_CREATED)
def create_transfer(
        data: TransferCreateRequest,
        db: Session = Depends(get_db),
        current_user=Depends(get_current_user),
):
    try:
        result = transaction_service.create_transfer(db, current_user.UserID, data)

        def to_resp(t):
            return TransactionResponse(
                transaction_id=t.TransactionID,
                user_id=t.UserID,
                wallet_id=t.WalletID,
                category_id=t.CategoryID,
                transaction_type=t.TransactionType,
                amount=t.Amount,
                transaction_date=t.TransactionDate,
                note=t.Note,
                is_recurring=t.IsRecurring,
                recur_interval=t.RecurInterval,
            )

        return TransferResponse(
            expense=to_resp(result["expense"]),
            income=to_resp(result["income"])
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    except Exception as exc:
        raise HTTPException(status_code=500, detail="Lỗi hệ thống khi chuyển tiền")