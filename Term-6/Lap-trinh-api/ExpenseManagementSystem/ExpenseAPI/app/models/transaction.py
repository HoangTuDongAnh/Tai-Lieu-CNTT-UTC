from sqlalchemy import Boolean, Column, Date, DateTime, Numeric, ForeignKey, String
from sqlalchemy.sql import func
from app.core.database import Base


class Transaction(Base):
    __tablename__ = "transactions"

    TransactionID = Column("transactionid", String(17), primary_key=True, index=True)
    UserID = Column("userid", String(15),
                    ForeignKey("users.userid"), nullable=False, index=True)
    WalletID = Column("walletid", String(12),
                      ForeignKey("wallets.walletid"), nullable=False, index=True)
    CategoryID = Column("categoryid", String(15),
                        ForeignKey("categories.categoryid"), nullable=False, index=True)

    TransactionType = Column("transactiontype", String(10), nullable=False)
    Amount = Column("amount", Numeric(15, 2), nullable=False)
    TransactionDate = Column("transactiondate", Date, nullable=False)
    Note = Column("note", String(500), nullable=True)

    IsRecurring = Column("isrecurring", Boolean, nullable=False, default=False)
    RecurInterval = Column("recurinterval", String(20), nullable=True)

    CreatedAt = Column("createdat", DateTime, nullable=False, server_default=func.now())
    UpdatedAt = Column("updatedat", DateTime, nullable=False,
                       server_default=func.now(), onupdate=func.now())