from datetime import date, timedelta

from sqlalchemy import func
from sqlalchemy.orm import Session

from app.models.user import User


class UserRepository:
    def get_by_email(self, db: Session, email: str) -> User | None:
        return db.query(User).filter(User.Email == email).first()

    def get_by_id(self, db: Session, user_id: str) -> User | None:
        return db.query(User).filter(User.UserID == user_id).first()

    def create(self, db: Session, user: User) -> User:
        db.add(user)
        db.commit()
        db.refresh(user)
        return user

    def update(self, db: Session, user: User) -> User:
        db.commit()
        db.refresh(user)
        return user

    def delete(self, db: Session, user: User) -> None:
        db.delete(user)
        db.commit()

    def count_all(self, db: Session) -> int:
        return db.query(func.count(User.UserID)).scalar() or 0

    def count_active(self, db: Session) -> int:
        return db.query(func.count(User.UserID)).filter(User.Status == "active").scalar() or 0

    def count_inactive(self, db: Session) -> int:
        return db.query(func.count(User.UserID)).filter(User.Status != "active").scalar() or 0

    def count_created_on_date(self, db: Session, target_date: date) -> int:
        return (
            db.query(func.count(User.UserID))
            .filter(func.date(User.CreatedAt) == target_date)
            .scalar()
            or 0
        )

    def get_recent_users(self, db: Session, limit: int = 5):
        return (
            db.query(User)
            .order_by(User.CreatedAt.desc(), User.UserID.desc())
            .limit(limit)
            .all()
        )

    def get_new_users_trend(self, db: Session, end_date: date, days: int = 7) -> list[tuple[date, int]]:
        safe_days = max(days, 1)
        output: list[tuple[date, int]] = []
        for offset in range(safe_days - 1, -1, -1):
            target_date = end_date - timedelta(days=offset)
            output.append((target_date, self.count_created_on_date(db, target_date)))
        return output
