from sqlalchemy import Column, Date, DateTime, Numeric, ForeignKey, SmallInteger, String
from sqlalchemy.sql import func
from app.core.database import Base


class Budget(Base):
    __tablename__ = "budgets"

    BudgetID = Column("budgetid", String(13), primary_key=True, index=True)
    UserID = Column("userid", String(15), ForeignKey("users.userid"), nullable=False, index=True)
    CategoryID = Column("categoryid", String(15), ForeignKey("categories.categoryid"), nullable=False, index=True)

    LimitAmount = Column("limitamount", Numeric(15, 2), nullable=False)
    SpentAmount = Column("spentamount", Numeric(15, 2), nullable=False, default=0)

    PeriodType = Column("periodtype", String(10), nullable=False, index=True)
    PeriodYear = Column("periodyear", SmallInteger, nullable=False, index=True)

    PeriodMonth = Column("periodmonth", SmallInteger, nullable=True, index=True)
    PeriodWeek = Column("periodweek", SmallInteger, nullable=True, index=True)

    StartDate = Column("startdate", Date, nullable=False, index=True)
    EndDate = Column("enddate", Date, nullable=False, index=True)

    CreatedAt = Column("createdat", DateTime, nullable=False, server_default=func.now())
    UpdatedAt = Column("updatedat", DateTime, nullable=False, server_default=func.now(), onupdate=func.now())