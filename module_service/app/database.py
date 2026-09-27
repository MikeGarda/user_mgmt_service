from collections.abc import Generator

from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, Session, sessionmaker

from app.config import get_settings


class Base(DeclarativeBase):
    pass


def _connect_args() -> dict[str, object]:
    settings = get_settings()
    if settings.database_url.startswith("mysql"):
        if settings.mysql_ssl_disabled:
            # nur fuer lokale Entwicklung ohne TLS
            return {"ssl_disabled": True}
        # DigitalOcean Managed MySQL erzwingt TLS ("require_secure_transport").
        # PyMySQL aktiviert TLS nur, wenn ein ssl-Dict uebergeben wird. Ohne
        # CA-Datei: verschluesselt, aber ohne Zertifikatspruefung (entspricht
        # sslmode=require bei PostgreSQL).
        return {"ssl": {"check_hostname": False}}
    if settings.database_url.startswith("sqlite"):
        return {"check_same_thread": False}
    return {}


settings = get_settings()
engine = create_engine(
    settings.database_url,
    connect_args=_connect_args(),
    pool_pre_ping=True,
    pool_recycle=300,
)
SessionLocal = sessionmaker(bind=engine, autoflush=False, expire_on_commit=False)


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
