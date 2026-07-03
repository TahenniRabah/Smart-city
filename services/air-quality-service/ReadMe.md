# Air Quality Service

## Responsabilité

Le `air-quality-service` analyse les mesures validées de qualité de l’air et calcule un niveau d’alerte.

Il publie un événement `AirQualityAlertDetected` si le niveau est `WARNING` ou `CRITICAL`.

\---

## Port

```text
8080
```

\---

## API REST

Endpoint conservé pour les tests manuels :

```http
POST /api/air-quality/measurements
```

\---

## Kafka

### Consomme

```text
measurements.validated
```

### Publie

```text
air-quality.alert.detected
```

\---

## Règles métier

### NO2

```text
value < 100         → NORMAL
100 <= value < 200  → WARNING
value >= 200        → CRITICAL
```

### PM10

```text
value < 50          → NORMAL
50 <= value < 80    → WARNING
value >= 80         → CRITICAL
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
http://localhost:8080/swagger-ui.html
```

\---

## Contrats

```text
docs/contracts/api/air-quality-openapi.yaml
docs/contracts/events/air-quality-alert-detected.md
```



