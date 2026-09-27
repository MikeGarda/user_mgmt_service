"""Prometheus-Telemetrie fuer den module_service (ServiceMonitor -> /metrics).

Eigene, schlanke Middleware auf Basis von prometheus_client statt
prometheus-fastapi-instrumentator: diese Bibliothek greift auf FastAPI-Interna
zu und stuerzt mit aktuellen FastAPI-Versionen ab.

Metriken (Namen kompatibel zum Grafana-Dashboard):
  http_requests_total{method,handler,status}          -> Request Rate, Error Rate
  http_request_duration_seconds{method,handler}       -> Response Time (Histogramm)
status ist gruppiert ("2xx", "4xx", "5xx"), handler ist die Routen-Vorlage
(z. B. /api/v1/modules/{module_id}) statt der konkreten URL -> wenige Zeitreihen.
"""

import time

from fastapi import FastAPI, Request, Response
from prometheus_client import CONTENT_TYPE_LATEST, Counter, Histogram, generate_latest

EXCLUDED_HANDLERS = {"/metrics", "/health/live", "/health/ready"}

REQUESTS = Counter(
    "http_requests_total",
    "Anzahl HTTP-Requests",
    ["method", "handler", "status"],
)
LATENCY = Histogram(
    "http_request_duration_seconds",
    "Dauer der HTTP-Requests in Sekunden",
    ["method", "handler"],
    buckets=(0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0, 10.0),
)


def setup_metrics(app: FastAPI) -> None:
    @app.middleware("http")
    async def record_metrics(request: Request, call_next):
        start = time.perf_counter()
        status_code = 500  # falls call_next mit einer Exception abbricht
        try:
            response = await call_next(request)
            status_code = response.status_code
            return response
        finally:
            route = request.scope.get("route")
            handler = getattr(route, "path", None) or "unmatched"
            if handler not in EXCLUDED_HANDLERS:
                REQUESTS.labels(request.method, handler, f"{status_code // 100}xx").inc()
                LATENCY.labels(request.method, handler).observe(time.perf_counter() - start)

    @app.get("/metrics", include_in_schema=False)
    def metrics() -> Response:
        return Response(generate_latest(), media_type=CONTENT_TYPE_LATEST)
