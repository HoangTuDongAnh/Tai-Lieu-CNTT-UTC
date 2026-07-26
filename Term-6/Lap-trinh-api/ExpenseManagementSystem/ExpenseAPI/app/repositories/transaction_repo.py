from sqlalchemy.orm import Session

from app.models.transaction import Transaction


class TransactionRepository:
    def get_all_by_user(self, db: Session, user_id: str) -> list[Transaction]:
        return (
            db.query(Transaction)
            .filter(Transaction.UserID == user_id)
            .order_by(Transaction.TransactionDate.desc(), Transaction.CreatedAt.desc())
            .all()
        )

    def get_by_id_and_user(self, db: Session, transaction_id: str, user_id: str) -> Transaction | None:
        return (
            db.query(Transaction)
            .filter(Transaction.TransactionID == transaction_id, Transaction.UserID == user_id)
            .first()
        )

    def create(self, db: Session, transaction: Transaction) -> Transaction:
        db.add(transaction)
        db.commit()
        db.refresh(transaction)
        return transaction

    def delete(self, db: Session, transaction: Transaction) -> None:
        db.delete(transaction)
        db.commit()