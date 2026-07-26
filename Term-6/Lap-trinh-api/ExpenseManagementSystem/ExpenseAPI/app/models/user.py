from sqlalchemy import Column, DateTime, String
from sqlalchemy.sql import func
from app.core.database import Base


class User(Base):
    __tablename__ = "users"

    UserID = Column("userid", String(15), primary_key=True, index=True)
    FullName = Column("fullname", String(100), nullable=False)
    Email = Column("email", String(150), unique=True, nullable=False, index=True)
    PasswordHash = Column("passwordhash", String(255), nullable=False)
    PhoneNumber = Column("phonenumber", String(15), nullable=True)
    Avatar = Column("avatar", String(255), nullable=True)

    Role = Column("role", String(20), nullable=False, default="user")
    Status = Column("status", String(10), nullable=False, default="active")

    CreatedAt = Column("createdat", DateTime, nullable=False, server_default=func.now())
    UpdatedAt = Column("updatedat", DateTime, nullable=False,
                       server_default=func.now(), onupdate=func.now())