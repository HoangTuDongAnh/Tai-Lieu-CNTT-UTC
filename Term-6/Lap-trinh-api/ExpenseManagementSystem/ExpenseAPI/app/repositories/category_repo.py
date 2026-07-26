from sqlalchemy import or_
from sqlalchemy.orm import Session

from app.models.category import Category


class CategoryRepository:
    def get_all_by_user(
        self,
        db: Session,
        user_id: str,
        include_deleted: bool = False,
        category_type: str | None = None,
    ) -> list[Category]:
        query = db.query(Category).filter(
            or_(Category.UserID == user_id, Category.UserID.is_(None))
        )

        if not include_deleted:
            query = query.filter(Category.IsDeleted == False)

        if category_type:
            query = query.filter(Category.CategoryType == category_type)

        return query.order_by(Category.IsDefault.desc(), Category.CreatedAt.asc()).all()

    def get_by_id_and_user(self, db: Session, category_id: str, user_id: str) -> Category | None:
        return (
            db.query(Category)
            .filter(
                Category.CategoryID == category_id,
                or_(Category.UserID == user_id, Category.UserID.is_(None))
            )
            .first()
        )

    def get_custom_by_id_and_user(self, db: Session, category_id: str, user_id: str) -> Category | None:
        return (
            db.query(Category)
            .filter(
                Category.CategoryID == category_id,
                Category.UserID == user_id
            )
            .first()
        )

    def get_by_name_and_user(self, db: Session, category_name: str, user_id: str) -> Category | None:
        return (
            db.query(Category)
            .filter(
                Category.CategoryName == category_name,
                Category.UserID == user_id,
                Category.IsDeleted == False
            )
            .first()
        )

    def get_by_name_and_user_any_status(
        self,
        db: Session,
        category_name: str,
        user_id: str,
        category_type: str | None = None,
    ) -> Category | None:
        query = (
            db.query(Category)
            .filter(
                Category.CategoryName == category_name,
                Category.UserID == user_id,
            )
        )

        if category_type:
            query = query.filter(Category.CategoryType == category_type)

        return query.order_by(Category.IsDeleted.asc(), Category.UpdatedAt.desc(), Category.CreatedAt.desc()).first()

    def create(self, db: Session, category: Category) -> Category:
        db.add(category)
        db.commit()
        db.refresh(category)
        return category

    def delete(self, db: Session, category: Category) -> None:
        db.delete(category)
        db.commit()
