from datetime import date
from decimal import Decimal
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field


class TransactionCreateRequest(BaseModel):
    wallet_id: str = Field(..., min_length=1, max_length=12)
    category_id: str = Field(..., min_length=1, max_length=15)
    transaction_type: str = Field(..., pattern="^(income|expense)$")
    amount: Decimal = Field(..., gt=0)
    transaction_date: date
    note: Optional[str] = Field(default=None, max_length=500)
    is_recurring: bool = False
    recur_interval: Optional[str] = Field(default=None, max_length=20)


class TransactionUpdateRequest(BaseModel):
    wallet_id: Optional[str] = Field(default=None, min_length=1, max_length=12)
    category_id: Optional[str] = Field(default=None, min_length=1, max_length=15)
    transaction_type: Optional[str] = Field(default=None, pattern="^(income|expense)$")
    amount: Optional[Decimal] = Field(default=None, gt=0)
    transaction_date: Optional[date] = None
    note: Optional[str] = Field(default=None, max_length=500)
    is_recurring: Optional[bool] = None
    recur_interval: Optional[str] = Field(default=None, max_length=20)


class TransactionResponse(BaseModel):
    transaction_id: str
    user_id: str
    wallet_id: str
    category_id: str
    transaction_type: str
    amount: Decimal
    transaction_date: date
    note: Optional[str] = None
    is_recurring: bool
    recur_interval: Optional[str] = None

    model_config = ConfigDict(from_attributes=True)

class TransferCreateRequest(BaseModel):
    from_wallet_id: str = Field(..., min_length=1, max_length=12)
    to_wallet_id: str = Field(..., min_length=1, max_length=12)
    amount: Decimal = Field(..., gt=0)
    transfer_date: date
    note: Optional[str] = Field(default=None, max_length=500)

class TransferResponse(BaseModel):
    expense: TransactionResponse
    income: TransactionResponse