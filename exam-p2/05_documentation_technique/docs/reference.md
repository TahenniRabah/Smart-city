# Référence technique

## Versions

| Composant | Version de référence |
|---|---|
| Java | 25 |
| Spring Boot | 3.5.16 pour ingestion-service |
| Maven | 3.9 ou compatible |
| JaCoCo | 0.8.15 |
| Maven Checkstyle Plugin | 3.6.0 |
| Checkstyle | 13.9.0 |
| SpotBugs Maven Plugin | 4.10.3.0 |
| Find Security Bugs | 1.14.0 |
| Gitleaks | 8.30.1 |
| Trivy | 0.72.0 |

## Ports

| Composant | Port local |
|---|---:|
| ingestion-service | 8082 |
| Kafka externe | 9092 |
| Redpanda Console | 8090 |

Les ports administratifs sont limités à localhost dans l'environnement local.

## Variables d'environnement

| Variable | Défaut | Description |
|---|---|---|
| `INGESTION_API_KEY` | Aucun en production | Clé API |
| `SERVER_PORT` | 8082 | Port HTTP |
| `KAFKA_BOOTSTRAP_SERVERS` | localhost:9092 hors Docker | Brokers Kafka |
| `OPENAPI_ENABLED` | Selon profil | Active OpenAPI |
| `SWAGGER_ENABLED` | Selon profil | Active Swagger UI |
| `INGESTION_RATE_LIMIT_CAPACITY` | 100 | Capacité du bucket |
| `INGESTION_RATE_LIMIT_REFILL_TOKENS` | 100 | Jetons rechargés |
| `INGESTION_RATE_LIMIT_DURATION_SECONDS` | 60 | Période de recharge |

## Routes

| Méthode | Route | Authentification | Résultat nominal |
|---|---|---|---|
| POST | `/api/ingestion/measurements` | `X-API-Key` | HTTP 202 |
| GET | `/actuator/health/readiness` | Publique selon configuration | HTTP 200 |
| GET | `/actuator/info` | Publique selon configuration | HTTP 200 |
| GET | `/v3/api-docs` | Démonstration | OpenAPI JSON |
| GET | `/swagger-ui.html` | Démonstration | Interface Swagger |

## Topics Kafka

| Topic | Producteur | Consommateur principal |
|---|---|---|
| `measurements.received` | ingestion-service | quality-service |
| `measurements.validated` | quality-service | air-quality-service |
| `measurements.rejected` | quality-service | Supervision |
| `air-quality.alert.detected` | air-quality-service | alerting-service |

## Événement MeasurementReceived

| Champ | Type | Description |
|---|---|---|
| `eventId` | chaîne | Identifiant unique |
| `eventType` | chaîne | `MeasurementReceived` |
| `eventVersion` | chaîne | Version du contrat |
| `correlationId` | chaîne | Corrélation distribuée |
| `occurredAt` | instant | Date de publication |
| `source` | chaîne | `ingestion-service` |
| `zoneId` | chaîne | Zone urbaine |
| `stationId` | chaîne | Station émettrice |
| `indicator` | chaîne | NO2, PM10 ou PM25 |
| `value` | nombre | Valeur mesurée |
| `timestamp` | instant | Date de mesure |

## Commandes Maven

```bash
mvn --file services/ingestion-service/pom.xml dependency:go-offline
mvn --file services/ingestion-service/pom.xml clean test
mvn --file services/ingestion-service/pom.xml clean verify
mvn --file services/ingestion-service/pom.xml checkstyle:check
mvn --file services/ingestion-service/pom.xml spotbugs:spotbugs
```

## Commandes Docker

```bash
docker build -t urbanhub-ingestion:local services/ingestion-service
docker compose up --build -d kafka ingestion-service
docker compose ps
docker compose logs -f ingestion-service
docker compose down --volumes --remove-orphans
```

## Rapports générés

| Rapport | Chemin Maven ou artefact |
|---|---|
| Surefire | `target/surefire-reports/` |
| JaCoCo HTML | `target/site/jacoco/index.html` |
| JaCoCo XML | `target/site/jacoco/jacoco.xml` |
| Checkstyle | `target/checkstyle-result.xml` |
| SpotBugs | `target/spotbugsXml.xml` |
| Trivy | Artefact `ingestion-security-reports` |
| SBOM | `urbanhub-ingestion-sbom.cdx.json` |

## Niveaux de qualité

```text
Lignes couvertes >= 60 %
Branches couvertes >= 60 %
Checkstyle : 0 violation
Tests : 0 échec et 0 erreur
Gitleaks : 0 secret non qualifié
```

## Codes HTTP

| Code | Signification |
|---:|---|
| 202 | Mesure acceptée |
| 400 | Requête invalide |
| 401 | Authentification invalide |
| 429 | Quota dépassé |
| 500 | Erreur interne |
