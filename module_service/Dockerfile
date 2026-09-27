########################################
# module_service - Runtime-Image
# Alle Abhaengigkeiten werden beim BAUEN installiert. Zur Laufzeit wird nichts
# nachgeladen: die NetworkPolicy erlaubt dem Pod keinen Internetzugriff, und
# ein Container soll reproduzierbar ohne Downloads starten.
########################################
ARG PYTHON_VERSION=3.14-trixie-slim
FROM astral/uv:python${PYTHON_VERSION}

WORKDIR /app
ENV UV_COMPILE_BYTECODE=1 \
    UV_LINK_MODE=copy \
    PYTHONUNBUFFERED=1

# 1) Nur Abhaengigkeiten (gut cachebar, aendert sich selten)
COPY pyproject.toml uv.lock ./
RUN uv sync --frozen --no-dev --no-install-project

# 2) Code + Projekt selbst (nicht editierbar -> keine Laufzeit-Builds)
COPY . .
RUN uv sync --frozen --no-dev --no-editable

# Ohne Root-Rechte laufen
RUN useradd --system --uid 10001 app && chown -R app /app
USER 10001

ENV PATH="/app/.venv/bin:$PATH"
EXPOSE 8080

# Direkt uvicorn aus der venv starten - NICHT "uv run" (das wuerde beim Start
# die Umgebung pruefen und ggf. Pakete aus dem Internet laden)
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8080"]
