# TODO
## Aufgabe 1
- [ ] gekube-prometheus-stack ist mittels Helm in einem dedizierten Namespace namens monitoring installiert
- [ ] Für Kubernetes werden mindestens CPU- und Memory Auslastung pro Pod durch Prometheus überwacht
- [ ] Der Spring Boot user_mgmt_service stellt kompatible Applikationsmetriken bereit. Mittels ServiceMonitor werden mindestens Request Rate, Response Time und Error Rate durch Prometheus erfasst
- [ ] In Grafana sind zwei passende Dashboards zur Visualisierung der Telemetriedaten vorhanden
- [ ] Für den user_mgmt_service ist eine eigene PrometheusRule definiert, welche einen fachlich sinnvollen Fehlerzustand erkennt. Der ausgelöste Alert wird über Alertmanager an einen konfigurierten Benachrichtigungskanal weitergeleitet
- [ ] Die Konfiguration des Monitoring Stacks erfolgt deklarativ über eine eigene values.yaml und befindet sich im Ops Repository

## Aufgabe 2
- [ ] k6 ist im Kubernetes Cluster ausführbar und es ist mindestens 1 Testskript für den user_mgmt_service vorhanden
- [ ] Das Testskript erzeugt kontrolliert steigende Last auf mindestens einen relevanten API Endpoint
- [ ] Während des Lasttests werden die aufgezeichneten Telemetriedaten erfasst und die Auswirkungen sind in Prometheus resp. Grafana nachvollziehbar dargestellt
- [ ] Es wird überprüft, ob der konfigurierte HPA bei steigender Last zusätzliche Service Replicas erzeugt und nach Reduktion der Last die Anzahl wieder reduziert
- [ ] Während des Skalierungsvorgangs bleibt der user_mgmt_service verfügbar und eingehende Requests werden via vordefinierter Strategie auf die verfügbaren Replicas verteilt

## Aufgabe 3
- [ ] Der DigitalOcean Provider ist in Terraform konfiguriert
- [ ] Der bestehende Kubernetes Cluster wird via Terraform import Blocks referenziert und mittels terraform plan -generate-config-out=generated.tf aus der bestehenden Infrastruktur generiert
- [ ] Die automatisch erzeugte generated.tf ist analysiert und bereinigt
- [ ] Wiederverwendbare Konfigurationswerte werden über Terraform Variablen parametrisiert
- [ ] Sensible Werte, insbesondere der DigitalOcean API Token, befinden sich nicht im Repository
- [ ] Terraform fmt und terraform validate laufen fehlerfrei und terraform plan zeigt für den bestehenden Cluster keine unbeabsichtigten Infrastrukturänderungen

## Aufgabe 4
- [ ] Die bisher im Kubernetes Cluster betriebene PostgreSQL Datenbank wird durch eine DigitalOcean Managed PostgreSQL Database ersetzt
- [ ] Der user_mgmt_service verbindet sich ausschliesslich über die bereitgestellten Verbindungsdaten mit der Managed Database
- [ ] Zugangsdaten zur Datenbank werden weiterhin über ein Kubernetes Secret bereitgestellt und nicht hardcodiert
- [ ] Der bisherige PostgreSQL Pod, Service und PersistentVolumeClaim werden aus dem Deployment entfernt
- [ ] Die Managed PostgreSQL Datenbank wird mittels Terraform definiert und via Digital Ocean Provider provisioniert

## Aufgabe 5
- [ ] Kyverno ist mittels Helm in einem dedizierten Namespace namens policy installiert
- [ ] Es sind mindestens 3 passende ClusterPolicies implementiert
- [ ] Ein Deployment, welches gegen eine der Policies verstösst, wird von Kyverno abgelehnt. Mittels eines absichtlich ungültigen Kubernetes Manifests wird nachgewiesen, dass das Policy Enforcement funktioniert
- [ ] Die ClusterPolicies befinden sich deklarativ im Ops Repository

## Aufgabe 6
- [ ] Der user_mgmt_service stellt einen neuen Endpoint zur Verfügung, über welchen einem User ein Module zugewiesen werden kann
- [ ] Vor der Zuweisung prüft der user_mgmt_service über die API des module_service, ob das angegebene Module verfügbar ist
- [ ] Die Kommunikation zwischen dem user_mgmt_service und dem module_service erfolgt synchron via REST Client über den jeweiligen Kubernetes Service und wird durch Timeout, Retry und Circuit Breaker gegen temporäre Ausfälle abgesichert
- [ ] Der user_mgmt_service hat keinen direkten Zugriff auf die seitens Digital Ocean verwaltete MySQL Datenbank
- [ ] Die vollständige End-to-End-Kommunikation vom Client über den user_mgmt_service bis zum module_service funktioniert fehlerfrei. Erfolgreiche sowie fehlerhafte Modulzuweisungen werden korrekt verarbeitet und mit geeigneten HTTP Statuscodes beantwortet
- [ ] Die seitens module_service exponierten Telemetriedaten werden mittels ServiceMonitor durch Prometheus erfasst und in einem zusätzlichen Grafana Dashboard visualisiert. Das Dashboard zeigt mindestens Request Rate, Response Time und Error Rate
- [ ] Für den module_service sind CPU- und Memory Limits so dimensioniert, dass die Anwendung unter Last stabil betrieben werden kann (vertikale Skalierung)
- [ ] Der module_service erfüllt die bestehenden ClusterPolicies
- [ ] Das Deployment erfolgt über den bestehenden GitOps Prozess. Die existierende Pipeline wird erweitert, sodass auch das Image es module_service automatisch gebaut, versioniert und publiziert wird