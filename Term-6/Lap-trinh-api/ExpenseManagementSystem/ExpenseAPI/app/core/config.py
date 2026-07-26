import os
from pathlib import Path
from dotenv import load_dotenv

BASE_DIR = Path(__file__).resolve().parent.parent.parent

# Ưu tiên .env ở thư mục gốc, nếu không có thì fallback sang app/.env
ENV_CANDIDATES = [
    BASE_DIR / ".env",
    BASE_DIR / "app" / ".env",
]

for env_path in ENV_CANDIDATES:
    if env_path.exists():
        load_dotenv(dotenv_path=env_path)
        break


class Settings:
    # ================= DATABASE =================
    DB_SERVER: str | None = os.getenv("DB_SERVER")
    DB_NAME: str | None = os.getenv("DB_NAME")
    DB_USER: str | None = os.getenv("DB_USER")
    DB_PASSWORD: str | None = os.getenv("DB_PASSWORD")
    DB_PORT: str = os.getenv("DB_PORT", "5432")

    @property
    def DATABASE_URL(self) -> str:
        missing = [
            key for key, value in {
                "DB_SERVER": self.DB_SERVER,
                "DB_NAME": self.DB_NAME,
                "DB_USER": self.DB_USER,
                "DB_PASSWORD": self.DB_PASSWORD,
            }.items() if not value
        ]

        if missing:
            raise ValueError(
                f"Missing required environment variables: {', '.join(missing)}. "
                f"Please check your .env file location and values."
            )

        return (
            f"postgresql://{self.DB_USER}:{self.DB_PASSWORD}"
            f"@{self.DB_SERVER}:{self.DB_PORT}/{self.DB_NAME}"
        )

    # ================= JWT =================
    SECRET_KEY: str = os.getenv("SECRET_KEY")
    ALGORITHM: str = os.getenv("ALGORITHM", "HS256")
    ACCESS_TOKEN_EXPIRE_MINUTES: int = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", 60))

    # ================= EMAIL =================
    EMAIL_HOST: str = os.getenv("EMAIL_HOST", "smtp.gmail.com")
    EMAIL_PORT: int = int(os.getenv("EMAIL_PORT", 587))
    EMAIL_USER: str = os.getenv("EMAIL_USER", "").strip()
    EMAIL_PASSWORD: str = os.getenv("EMAIL_PASSWORD", "").strip().replace(" ", "")


settings = Settings()