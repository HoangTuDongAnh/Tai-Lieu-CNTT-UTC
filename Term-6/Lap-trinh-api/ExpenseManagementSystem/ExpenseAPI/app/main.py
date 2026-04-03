from fastapi import FastAPI

from app.routers.auth import router as auth_router

app = FastAPI(
    title="ExpenseAPI",
    version="1.0.0",
    description="API for Personal Expense Management System",
)

app.include_router(auth_router)


@app.get("/")
def root():
    return {"message": "ExpenseAPI is running"}

import uvicorn

if __name__ == "__main__":
    uvicorn.run("app.main:app", host="127.0.0.1", port=8000, reload=True)