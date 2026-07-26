from decimal import Decimal
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field


class WalletCreateRequest(BaseModel):
    wallet_name: str = Field(..., min_length=1, max_length=100)
    initial_balance: Decimal = Field(default=0, ge=0)
    currency: str = Field(default="VND", max_length=10)
    is_default: bool = False


class WalletUpdateRequest(BaseModel):
    wallet_name: Optional[str] = Field(default=None, min_length=1, max_length=100)
    currency: Optional[str] = Field(default=None, max_length=10)
    is_default: Optional[bool] = None


class WalletDeleteRequest(BaseModel):
    mode: str = Field(..., pattern="^(delete_all|move_transactions)$")
    replacement_wallet_id: Optional[str] = Field(default=None, max_length=12)


class WalletResponse(BaseModel):
    wallet_id: str
    user_id: str
    wallet_name: str
    initial_balance: Decimal
    current_balance: Decimal
    currency: str
    is_default: bool

    model_config = ConfigDict(from_attributes=True)