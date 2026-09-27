import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import JSONResponse

from app.api import module_router, user_module_router
from app.bootstrap import init_database
from app.config import get_settings
from app.health import health_router
from app.metrics import setup_metrics

settings = get_settings()
logging.basicConfig(
    level=settings.log_level, format="%(asctime)s %(levelname)s %(name)s %(message)s"
)


@asynccontextmanager
async def lifespan(_: FastAPI):
    init_database()
    yield


app = FastAPI(
    title="Module Service",
    description="Manages modules.",
    version=settings.app_version,
    lifespan=lifespan,
)
app.include_router(module_router)
app.include_router(user_module_router)
app.include_router(health_router)

# Telemetrie fuer Prometheus (ServiceMonitor -> /metrics): Request Rate,
# Response Time, Error Rate - siehe app/metrics.py
setup_metrics(app)


@app.exception_handler(HTTPException)
async def http_exception_handler(_: Request, exc: HTTPException) -> JSONResponse:
    detail = exc.detail
    if not isinstance(detail, dict) or "code" not in detail:
        detail = {"code": "HTTP_ERROR", "message": str(detail)}
    return JSONResponse(status_code=exc.status_code, content=detail, headers=exc.headers)
