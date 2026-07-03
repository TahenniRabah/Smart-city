# Alerting Service

## Responsabilité

Le `alerting-service` consomme les événements `AirQualityAlertDetected`.

Il applique une logique d’idempotence et prépare une notification CSU si l’alerte est critique.

\---

## Port

```text
8081
```

\---

## API REST

```http
GET /api/alerting/status
```

\---

## Kafka

### Consomme

```text
air-quality.alert.detected
```

### DLQ prévue

```text
air-quality.alert.detected.dlq
```

\---

## Robustesse

```text
Idempotence par eventId
Retry contrôlé prévu
DLQ prévue
```

\---

## Lancer

```shell
mvn spring-boot:run
```

\---

## Tests

```shell
mvn test
```

\---

## Swagger

```text
http://localhost:8081/swagger-ui.html
```

\---

## Contrats

```text
docs/contracts/api/alerting-openapi.yaml
docs/contracts/events/air-quality-alert-detected.md
```



