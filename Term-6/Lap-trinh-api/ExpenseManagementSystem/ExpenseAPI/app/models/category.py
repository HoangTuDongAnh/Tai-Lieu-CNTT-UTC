from sqlalchemy import Boolean, Column, DateTime, ForeignKey, String
from sqlalchemy.sql import func
from app.core.database import Base


class Category(Base):
    __tablename__ = "categories"

    CategoryID = Column("categoryid", String(15), primary_key=True, index=True)
    UserID = Column("userid", String(15), ForeignKey("users.userid"), nullable=True, index=True)

    CategoryName = Column("categoryname", String(100), nullable=False)
    CategoryType = Column("categorytype", String(10), nullable=False, server_default="expense")

    Icon = Column("icon", String(50), nullable=True)
    Color = Column("color", String(10), nullable=True)
    IsDefault = Column("isdefault", Boolean, nullable=False, default=False)
    IsDeleted = Column("isdeleted", Boolean, nullable=False, default=False)

    CreatedAt = Column("createdat", DateTime, nullable=False, server_default=func.now())
    UpdatedAt = Column("updatedat", DateTime, nullable=False, server_default=func.now(), onupdate=func.now())
