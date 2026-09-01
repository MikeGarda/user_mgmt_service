# TODO
## Aufgabe 1
- [x] Die Docker Images sind in einer für den Kubernetes Cluster zugänglichen Container Registry gespeichert
- [x] Service sowie Deployment für Next Frontend, Spring Boot Backend und PostgreSQL Datenbank vorhanden
- [x] ConfigMap für die nicht sensitive Konfiguration vorhanden 
- [x] Secret für die sensitive Konfiguration vorhanden 
- [x] Die Datenbank verwendet eine persistente Speicherung mittels PersistentVolumeClaim 
- [x] Backend kommuniziert ausschliesslich über den Kubernetes Service mit der Datenbank
- [x] Ingress für den Zugriff auf das Frontend vorhanden und erreichbar

## Aufgabe 2
- [x] Helm Chart erstellt, Kubernetes Manifests templatisiert
- [x] Sämtliche Konfigurationswerte werden zentral über die values.yaml verwaltet, kein Hardcodings
- [x] Wiederverwendbare Template Funktionen werden mittels _helpers.tpl definiert 
- [x] Das Chart lässt sich mittels helm lint ohne Fehler validieren

## Aufgabe 3
- [x] Auf GitHub ist ein Ops Repository angelegt, welches den Helm Chart sowie das ArgodCD Application Manifest enthält
- [x] ArgoCD ist im Kubernetes Cluster in einem dedizierten Namespace installiert und konfiguriert
- [x] Das Deployment des Helm Charts erfolgt in einen eigenen, von ArgoCD getrennten Namespace
- [x] Änderungen am values.yaml oder am Helm Chart im Ops Repository werden von ArgoCD erkannt und in den Cluster übernommen
- [x] Das Argo CD Dashboard ist zugänglich

## Aufgabe 4
- [x] Die Pipeline wird bei einem Push auf den main Branch des Applikations Repositories initiiert
- [x] Das Docker Image wird fehlerfrei gebaut und mit einem eindeutigen, dynamischen Tag (z. B. Git-Commit-Hash) versioniert
- [x] Das versionierte Image wird erfolgreich in eine für den Kubernetes Cluster autorisierte Container Registry publiziert
- [x] Die Pipeline führt einen automatisierten Commit auf das Ops Repository aus, welcher den Image Tag in der values.yaml des Helm Charts aktualisiert (sog. Promotion)
- [x] Sämtliche imperativen Deployment Schritte (bspw. ssh und docker compose) sind aus der Pipeline entfernt
- [x] Alle benötigten Secrets werden über GitHub Secrets verwaltet

## Aufgabe 5
- [x] Der Helm Chart ist umgebungsspezifisch via separater values-staging.yaml und values-prod.yaml parametrisierbar 
- [x] ArgoCD verwaltet zwei eigenständige Application Manifests, welche den Helm Chart automatisiert in separate Namespaces deployen
- [x] Für jeden Namespace sind maximale Ressourcenlimits (CPU und Memory) mittels ResourceQuota verbindlich definiert 
- [x] Die netzwerktechnische Isolation zwischen den Namespaces ist durch NetworkPolicies sichergestellt


## Aufgabe 6
- [x] Ein Horizontal Pod Autoscaler (HPA) skaliert die Backend Replicas dynamisch anhand definierter Schwellenwerte
- [x] Für sämtliche Pods sind requests und limits verbindlich deklariert, um die Funktion des HPA sicherzustellen und Ressourcen Konflikte auf dem Node zu vermeiden
- [x] Konfigurierte livenessProbe und readinessProbe stellen sicher, dass Traffic nur an bereite Instanzen geroutet und fehlerhafte Pods terminiert werden
- [x] Der Ingress Controller verteilt den externen Traffic dynamisch mittels internem Round Robin Load Balancing ausschliesslich auf alle als "ready" validierten Pod Replicas
- [x] Es ist eine RollingUpdate Strategie definiert, um Service Unterbrüche bei Aktualisierungen auszuschliessen
- [x] Ein Pod Disruption Budget (PDB) ist definiert, um bei Wartungsvorgängen oder Re-Schedulings auf dem Node eine minimale Anzahl aktiver Replikate sicherzustellen
