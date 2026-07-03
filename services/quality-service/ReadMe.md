# Quality Service

## Responsabilité

Le `quality-service` consomme les événements `MeasurementReceived`, vérifie la qualité des mesures et publie soit :

* `MeasurementValidated` ;
* `MeasurementRejected`.

\---

## Port

```text
8083
```

\---

## API REST

```http
GET /api/quality/status
```

\---

## Kafka

### Consomme

```text
measurements.received
```

### Publie

```text
measurements.validated
measurements.rejected
```

\---

## Règles de validation

Une mesure est valide si :

```text
zoneId non vide
stationId non vide
indicator non vide
timestamp non null
value >= 0
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
http://localhost:8083/swagger-ui.html
```

\---

## Contrats

```text
docs/contracts/api/quality-openapi.yaml
docs/contracts/events/measurement-validated.md
docs/contracts/events/measurement-rejected.md
```



