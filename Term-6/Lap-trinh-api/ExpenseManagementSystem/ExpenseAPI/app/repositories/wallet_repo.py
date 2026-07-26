from sqlalchemy.orm import Session

from app.models.wallet import Wallet


class WalletRepository:
    def get_all_by_user(self, db: Session, user_id: str) -> list[Wallet]:
        return (
            db.query(Wallet)
            .filter(Wallet.UserID == user_id)
            .order_by(Wallet.CreatedAt.desc())
            .all()
        )

    def get_by_id_and_user(self, db: Session, wallet_id: str, user_id: str) -> Wallet | None:
        return (
            db.query(Wallet)
            .filter(Wallet.WalletID == wallet_id, Wallet.UserID == user_id)
            .first()
        )

    def get_by_name_and_user(self, db: Session, wallet_name: str, user_id: str) -> Wallet | None:
        return (
            db.query(Wallet)
            .filter(Wallet.WalletName == wallet_name, Wallet.UserID == user_id)
            .first()
        )

    def create(self, db: Session, wallet: Wallet) -> Wallet:
        db.add(wallet)
        db.commit()
        db.refresh(wallet)
        return wallet

    def delete(self, db: Session, wallet: Wallet) -> None:
        db.delete(wallet)
        db.commit()