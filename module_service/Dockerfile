########################################
# Runtime        
########################################

ARG PYTHON_VERSION=3.14-trixie-slim

FROM astral/uv:python${PYTHON_VERSION}

# Set working directory
WORKDIR /app

# Copy Application Code
COPY . .

RUN uv sync --frozen

EXPOSE 8080

CMD ["uv", "run", "uvicorn", "app.main:app", "--port", "8080", "--host", "0.0.0.0"]