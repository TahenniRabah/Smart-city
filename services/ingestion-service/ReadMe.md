# Ingestion Service

## Responsabilité

Le `ingestion-service` reçoit les mesures brutes envoyées par les passerelles IoT simulées.

Il génère un `correlationId`, puis publie un événement `MeasurementReceived` dans Kafka.

\---

## Port

```text
8082
```

\---

## API REST

```http
POST /api/ingestion/measurements
```

### Exemple de requête

```json
{
  "zoneId": "ZFE-1",
  "stationId": "AIR-STATION-042",
  "indicator": "NO2",
  "value": 220.5,
  "timestamp": "2026-05-06T14:29:58Z"
}
```

### Exemple de réponse

```json
{
  "status": "ACCEPTED",
  "correlationId": "..."
}
```

\---

## Kafka

### Publie

```text
measurements.received
```

### Événement

```text
MeasurementReceived
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
http://localhost:8082/swagger-ui.html
```

\---

## Contrats

```text
docs/contracts/api/ingestion-openapi.yaml
docs/contracts/events/measurement-received.md
```



