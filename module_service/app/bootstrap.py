"""Legt beim Start die Tabellen an und fuegt die Stamm-Module ein.

Idempotent: bestehende Tabellen und Module bleiben unveraendert. Ersetzt das
manuelle Einspielen von schema.sql, damit eine frische Managed-MySQL-Datenbank
ohne Handarbeit funktioniert (GitOps: nichts wird von Hand am System gemacht).
"""

import logging
import time

from sqlalchemy.exc import OperationalError

from app.database import Base, SessionLocal, engine
from app.models import Module

logger = logging.getLogger(__name__)

# Dieselben festen IDs wie in schema.sql -> bekannte IDs fuer End-to-End-Tests
SEED_MODULES = [
    ("c02f58f2-3aca-4f1e-8076-bacf6f1999e6", "CLOUD-ARCH", "Cloud Architecture",
     "Designing reliable and scalable cloud systems"),
    ("6d5889ee-f4c7-44d7-a887-da92d2a51ac4", "DATABASES", "Database Systems",
     "Relational data modeling and SQL fundamentals"),
    ("674ca4e0-6334-4b12-aa83-d97895049b8a", "SECURITY", "Application Security",
     "Secure software design and common vulnerabilities"),
    ("4b9ff45a-d90f-42b0-8b72-20f0b92b6027", "WEB-DEV", "Web Development",
     "Building modern web applications and APIs"),
]


def init_database(attempts: int = 10, delay_seconds: float = 3.0) -> None:
    for attempt in range(1, attempts + 1):
        try:
            Base.metadata.create_all(engine)
            _seed_modules()
            logger.info("Datenbank initialisiert")
            return
        except OperationalError:
            if attempt == attempts:
                raise
            logger.warning("Datenbank nicht erreichbar (Versuch %s/%s)", attempt, attempts)
            time.sleep(delay_seconds)


def _seed_modules() -> None:
    with SessionLocal() as db:
        for module_id, code, name, description in SEED_MODULES:
            if db.get(Module, module_id) is None:
                db.add(Module(id=module_id, code=code, name=name, description=description))
        db.commit()
