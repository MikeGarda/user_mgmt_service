"""Health-Endpunkte fuer die Kubernetes-Probes (ohne Datenbank-Session-Abhaengigkeit)."""

from fastapi import APIRouter, Response, status
from sqlalchemy import text

from app.database import engine

health_router = APIRouter(prefix="/health", tags=["health"])


@health_router.get("/live")
def liveness() -> dict[str, str]:
    # Nur der Prozess: ein DB-Ausfall soll KEINEN Neustart ausloesen
    return {"status": "UP"}


@health_router.get("/ready")
def readiness(response: Response) -> dict[str, str]:
    # Bereit fuer Traffic nur, wenn die Datenbank antwortet
    try:
        with engine.connect() as connection:
            connection.execute(text("SELECT 1"))
    except Exception:
        response.status_code = status.HTTP_503_SERVICE_UNAVAILABLE
        return {"status": "DOWN", "database": "unreachable"}
    return {"status": "UP", "database": "reachable"}
