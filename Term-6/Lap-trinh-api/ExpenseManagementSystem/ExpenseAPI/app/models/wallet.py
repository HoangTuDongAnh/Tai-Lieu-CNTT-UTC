from sqlalchemy import Boolean, Column, DateTime, Numeric, ForeignKey, String
from sqlalchemy.sql import func
from app.core.database import Base


class Wallet(Base):
    __tablename__ = "wallets"

    WalletID = Column("walletid", String(12), primary_key=True, index=True)
    UserID = Column("userid", String(15),
                    ForeignKey("users.userid"), nullable=False, index=True)

    WalletName = Column("walletname", String(100), nullable=False)

    InitialBalance = Column("initialbalance", Numeric(15, 2), nullable=False, default=0)
    CurrentBalance = Column("currentbalance", Numeric(15, 2), nullable=False, default=0)

    Currency = Column("currency", String(10), nullable=False, default="VND")
    IsDefault = Column("isdefault", Boolean, nullable=False, default=False)

    CreatedAt = Column("createdat", DateTime, nullable=False, server_default=func.now())
    UpdatedAt = Column("updatedat", DateTime, nullable=False,
                       server_default=func.now(), onupdate=func.now())