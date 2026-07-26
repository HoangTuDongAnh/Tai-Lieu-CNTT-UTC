from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker
from app.core.config import settings

# Sử dụng DATABASE_URL từ config đã được tự động ghép chuỗi
DATABASE_URL = settings.DATABASE_URL

# Tạo engine cho PostgreSQL (echo=True để bạn debug SQL trong terminal)
engine = create_engine(DATABASE_URL, echo=True)

SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)

Base = declarative_base()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()