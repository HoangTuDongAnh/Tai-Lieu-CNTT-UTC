from fastapi import FastAPI
from app.core.database import Base, engine
from fastapi.middleware.cors import CORSMiddleware

from app.models import user
from app.models import wallet
from app.models import category
from app.models import transaction
from app.models import budget
from app.models import support_request
from app.models import support_attachment

Base.metadata.create_all(bind=engine)

from app.routers.admin import router as admin_router
from app.routers.admin_support import router as admin_support_router
from app.routers.auth import router as auth_router
from app.routers.budgets import router as budget_router
from app.routers.categories import router as category_router
from app.routers.reports import router as report_router
from app.routers.support import router as support_router
from app.routers.transactions import router as transaction_router
from app.routers.wallets import router as wallet_router

app = FastAPI(
    title="ExpenseAPI",
    version="1.0.0",
    description="API for Personal Expense Management System",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Cho phép tất cả các nguồn trong lúc phát triển
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth_router)
app.include_router(wallet_router)
app.include_router(category_router)
app.include_router(transaction_router)
app.include_router(budget_router)
app.include_router(report_router)
app.include_router(admin_router)
app.include_router(support_router)
app.include_router(admin_support_router)


@app.get("/")
def root():
    return {"message": "ExpenseAPI is running"}