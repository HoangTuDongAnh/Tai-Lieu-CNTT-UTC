from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.role_dependencies import require_admin
from app.schemas.admin_schema import AdminDashboardResponse
from app.services.admin_service import AdminService

router = APIRouter(prefix="/admin", tags=["Admin"])

admin_service = AdminService()


@router.get("/dashboard", response_model=AdminDashboardResponse)
def get_admin_dashboard(
    db: Session = Depends(get_db),
    current_user=Depends(require_admin)
):
    return admin_service.get_dashboard_data(db)