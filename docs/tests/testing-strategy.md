# Testing Strategy — Smart City / UrbanHub

## 1\. Objectif

Ce document décrit la stratégie de tests appliquée dans le projet **Smart City / UrbanHub**.

L’objectif est de garantir que les microservices restent :

* testables ;
* maintenables ;
* découplés de l’infrastructure ;
* fiables dans une architecture distribuée ;
* compatibles avec une démarche XP / TDD.

Les tests couvrent principalement :

* les tests unitaires ;
* les mocks ;
* les stubs ;
* les tests de logique métier ;
* les tests manuels API via Postman ou `curl` ;
* les tests de publication / consommation Kafka ;
* la validation par CI.

\---

## 2\. Services concernés

Les microservices actuellement couverts par cette stratégie sont :

```text
ingestion-service
quality-service
air-quality-service
alerting-service
```

Chaque microservice possède sa propre logique métier, ses tests et sa pipeline CI dédiée.

\---

## 3\. Types de tests utilisés

### 3.1 Tests unitaires

Les tests unitaires vérifient la logique métier sans dépendre de Kafka, d’une base de données ou d’un autre service.

Exemples de classes testées :

* `MeasurementIngestionService` ;
* `MeasurementQualityService` ;
* `AirQualityAlertService` ;
* `AirQualityAnalysisService` ;
* `AlertingService` ;
* `Sensor`.

Objectifs :

* tester rapidement une règle métier isolée ;
* éviter les dépendances réseau ;
* éviter de démarrer Kafka ;
* rendre les tests rapides et déterministes.

### 3.2 Tests avec mocks

Les mocks sont utilisés pour remplacer les dépendances externes.

Exemple :

```java
private final MeasurementReceivedPublisher publisher =
        mock(MeasurementReceivedPublisher.class);
```

Dans cet exemple, le publisher Kafka est remplacé par un mock.

Cela permet de vérifier que le service métier demande bien la publication d’un événement, sans réellement publier dans Kafka.

### 3.3 Tests avec stubs

Les stubs sont utilisés pour forcer une valeur connue dans un test.

Exemple :

```java
when(correlationIdGenerator.generate()).thenReturn("corr-12345");
```

Cela permet de rendre le test déterministe.

Sans stub, le `correlationId` serait généré aléatoirement, ce qui rendrait le test plus difficile à vérifier.

### 3.4 Tests manuels API

Les endpoints REST sont testés manuellement avec Postman ou `curl`.

Exemples :

```text
POST /api/ingestion/measurements
GET /api/quality/status
POST /api/air-quality/measurements
GET /api/alerting/status
```

Ces tests permettent de vérifier :

* le démarrage du service ;
* le routage REST ;
* la sérialisation JSON ;
* la réponse HTTP ;
* l’intégration avec Kafka.

### 3.5 Tests Kafka manuels

Kafka est testé avec `kafka-console-consumer`.

Exemple :

```shell
kafka-console-consumer --bootstrap-server localhost:9092 --topic measurements.received --from-beginning
```

Cela permet de vérifier qu’un événement est réellement publié dans le bon topic.

Topics testés :

* `measurements.received` ;
* `measurements.validated` ;
* `measurements.rejected` ;
* `air-quality.alert.detected` ;
* `air-quality.alert.detected.dlq`.

\---

## 4\. Exemple de test unitaire — `ingestion-service`

Le test suivant vérifie qu’une mesure brute valide est acceptée et qu’un événement `MeasurementReceived` est publié.

```java
@Test
void shouldAcceptRawMeasurementAndPublishMeasurementReceivedEvent() {
    RawMeasurementCommand command = new RawMeasurementCommand(
            "ZFE-1",
            "AIR-STATION-042",
            "NO2",
            220.5,
            Instant.parse("2026-05-06T14:29:58Z")
    );

    when(correlationIdGenerator.generate()).thenReturn("corr-12345");

    IngestionResult result = service.ingest(command);

    assertEquals("ACCEPTED", result.status());
    assertEquals("corr-12345", result.correlationId());

    ArgumentCaptor<MeasurementReceivedEvent> eventCaptor =
            ArgumentCaptor.forClass(MeasurementReceivedEvent.class);

    verify(publisher).publish(eventCaptor.capture());

    MeasurementReceivedEvent event = eventCaptor.getValue();

    assertEquals("MeasurementReceived", event.eventType());
    assertEquals("1.0", event.eventVersion());
    assertEquals("corr-12345", event.correlationId());
    assertEquals("ingestion-service", event.source());
    assertEquals("ZFE-1", event.zoneId());
    assertEquals("AIR-STATION-042", event.stationId());
    assertEquals("NO2", event.indicator());
    assertEquals(220.5, event.value());
}
```

### Ce que ce test valide

* La mesure est acceptée.
* Un `correlationId` est généré.
* Un événement `MeasurementReceived` est créé.
* Le publisher est appelé.
* Kafka n’est pas nécessaire pour le test.

\---

## 5\. Exemple de mock

Dans le test d’ingestion, Kafka n’est pas utilisé directement.

Le port de publication est mocké :

```java
private final MeasurementReceivedPublisher publisher =
        mock(MeasurementReceivedPublisher.class);
```

Puis le test vérifie que la publication a été demandée :

```java
verify(publisher).publish(eventCaptor.capture());
```

Cela montre que la logique métier est testée indépendamment de l’infrastructure.

\---

## 6\. Exemple de stub

Le générateur de `correlationId` est stubbé :

```java
when(correlationIdGenerator.generate()).thenReturn("corr-12345");
```

Cela permet de vérifier précisément la valeur attendue :

```java
assertEquals("corr-12345", result.correlationId());
assertEquals("corr-12345", event.correlationId());
```

Le test devient donc prévisible et reproductible.

\---

## 7\. Exemple de test unitaire — `quality-service`

Le service qualité vérifie si une mesure reçue est valide ou non.

### Cas valide

```java
@Test
void shouldPublishMeasurementValidatedWhenMeasurementIsValid() {
    MeasurementReceivedEvent receivedEvent = validMeasurementReceived();

    service.checkQuality(receivedEvent);

    ArgumentCaptor<MeasurementValidatedEvent> captor =
            ArgumentCaptor.forClass(MeasurementValidatedEvent.class);

    verify(validatedPublisher).publish(captor.capture());
    verifyNoInteractions(rejectedPublisher);

    MeasurementValidatedEvent validatedEvent = captor.getValue();

    assertEquals("MeasurementValidated", validatedEvent.eventType());
    assertEquals("1.0", validatedEvent.eventVersion());
    assertEquals("corr-12345", validatedEvent.correlationId());
    assertEquals("quality-service", validatedEvent.source());
    assertEquals("ZFE-1", validatedEvent.zoneId());
}
```

### Cas invalide

```java
@Test
void shouldPublishMeasurementRejectedWhenZoneIdIsMissing() {
    MeasurementReceivedEvent receivedEvent = new MeasurementReceivedEvent(
            "evt-002",
            "MeasurementReceived",
            "1.0",
            "corr-12345",
            Instant.parse("2026-05-06T14:30:00Z"),
            "ingestion-service",
            "",
            "AIR-STATION-042",
            "NO2",
            220.5,
            Instant.parse("2026-05-06T14:29:58Z")
    );

    service.checkQuality(receivedEvent);

    verifyNoInteractions(validatedPublisher);
    verify(rejectedPublisher).publish(argThat(rejectedEvent ->
            rejectedEvent.eventType().equals("MeasurementRejected")
                    \\\&\\\& rejectedEvent.reason().equals("zoneId is required")
    ));
}
```

### Ce que ces tests valident

* Une mesure valide produit `MeasurementValidated`.
* Une mesure invalide produit `MeasurementRejected`.
* Les publishers Kafka sont mockés.
* La logique de validation est testée indépendamment de Kafka.

\---

## 8\. Exemple de test unitaire — `air-quality-service`

Le service `air-quality-service` calcule un niveau d’alerte selon les seuils.

Exemple :

```java
@Test
void shouldReturnCriticalWhenNo2IsAboveCriticalThreshold() {
    AirQualityMeasurement measurement =
            new AirQualityMeasurement("ZFE-1", "AIR-STATION-042", Pollutant.NO2, 220);

    AlertLevel result = service.calculateAlertLevel(measurement);

    assertEquals(AlertLevel.CRITICAL, result);
}
```

Ce test vérifie la règle métier :

```text
NO2 >= 200 → CRITICAL
```

\---

## 9\. Exemple de test unitaire — `alerting-service`

Le service `alerting-service` vérifie qu’une alerte critique déclenche une notification CSU.

```java
@Test
void shouldNotifyCsuWhenAirQualityAlertIsCritical() {
    AirQualityAlertDetectedEvent event = criticalNo2Event("event-1");

    when(processedEventRepository.hasAlreadyBeenProcessed("event-1"))
            .thenReturn(false);

    alertingService.handle(event);

    verify(notificationPort).notifyCsu(event);
    verify(processedEventRepository).markAsProcessed("event-1");
}
```

Ce test vérifie que :

* une alerte `CRITICAL` déclenche une notification ;
* l’`eventId` est marqué comme traité ;
* la notification est testée via un mock.

\---

## 10\. Test d’idempotence

Le service `alerting-service` doit ignorer un événement déjà traité.

```java
@Test
void shouldIgnoreEventWhenEventIdWasAlreadyProcessed() {
    AirQualityAlertDetectedEvent event = criticalNo2Event("event-1");

    when(processedEventRepository.hasAlreadyBeenProcessed("event-1"))
            .thenReturn(true);

    alertingService.handle(event);

    verifyNoInteractions(notificationPort);
    verify(processedEventRepository, never()).markAsProcessed("event-1");
}
```

Ce test valide une règle importante en architecture asynchrone :

```text
Un événement reçu deux fois ne doit pas déclencher deux notifications.
```

\---

## 11\. Test du Design Pattern State — `Sensor`

La classe `Sensor` est testée indépendamment de Spring et Kafka.

```java
@Test
void activeSensorShouldBeAbleToSendMeasurement() {
    Sensor sensor = new Sensor(
            "SENSOR-001",
            "AIR-STATION-042",
            "ZFE-1",
            new ActiveSensorState()
    );

    assertTrue(sensor.canSendMeasurement());
    assertEquals("ACTIVE", sensor.status());
}
```

Autre exemple :

```java
@Test
void faultySensorShouldNotBeAbleToSendMeasurement() {
    Sensor sensor = new Sensor(
            "SENSOR-001",
            "AIR-STATION-042",
            "ZFE-1",
            new FaultySensorState()
    );

    assertFalse(sensor.canSendMeasurement());
    assertEquals("FAULTY", sensor.status());
}
```

Ces tests vérifient que le comportement du capteur dépend bien de son état.

\---

## 12\. Démarche TDD utilisée

La démarche TDD appliquée est :

```text
Red → Green → Refactor
```

### Red

Écriture d’un test qui échoue.

Exemple :

```text
shouldPublishMeasurementValidatedWhenMeasurementIsValid
```

Le test échoue car la classe ou le comportement n’existe pas encore.

### Green

Implémentation minimale pour faire passer le test.

### Refactor

Amélioration du code sans changer le comportement.

Exemples :

* extraction de `rejectionReason()` ;
* extraction de constantes ;
* séparation des publishers Kafka.

\---

## 13\. Tests manuels avec Postman

Des tests manuels sont réalisés avec Postman pour vérifier les routes REST.

### `ingestion-service`

```http
POST http://localhost:8082/api/ingestion/measurements
```

Body :

```json
{
  "zoneId": "ZFE-1",
  "stationId": "AIR-STATION-042",
  "indicator": "NO2",
  "value": 220.5,
  "timestamp": "2026-05-06T14:29:58Z"
}
```

Réponse attendue :

```json
{
  "status": "ACCEPTED",
  "correlationId": "..."
}
```

### `air-quality-service`

```http
POST http://localhost:8080/api/air-quality/measurements
```

Body :

```json
{
  "zoneId": "ZFE-1",
  "stationId": "AIR-STATION-042",
  "pollutant": "NO2",
  "value": 220.5
}
```

Réponse attendue :

```json
{
  "alertLevel": "CRITICAL"
}
```

### `alerting-service`

```http
GET http://localhost:8081/api/alerting/status
```

Réponse attendue :

```json
{
  "status": "UP",
  "service": "alerting-service"
}
```

### `quality-service`

```http
GET http://localhost:8083/api/quality/status
```

Réponse attendue :

```json
{
  "status": "UP",
  "service": "quality-service"
}
```

\---

## 14\. Tests Kafka manuels

### Vérifier `measurements.received`

```shell
kafka-console-consumer --bootstrap-server localhost:9092 --topic measurements.received --from-beginning
```

### Vérifier `measurements.validated`

```shell
kafka-console-consumer --bootstrap-server localhost:9092 --topic measurements.validated --from-beginning
```

### Vérifier `measurements.rejected`

```shell
kafka-console-consumer --bootstrap-server localhost:9092 --topic measurements.rejected --from-beginning
```

### Vérifier `air-quality.alert.detected`

```shell
kafka-console-consumer --bootstrap-server localhost:9092 --topic air-quality.alert.detected --from-beginning
```

\---

## 15\. Tests end-to-end manuels

Le flux complet attendu est :

```text
ingestion-service
→ measurements.received
→ quality-service
→ measurements.validated
→ air-quality-service
→ air-quality.alert.detected
→ alerting-service
```

Commande de départ :

```shell
curl -X POST http://localhost:8082/api/ingestion/measurements   -H "Content-Type: application/json"   -d '{
    "zoneId": "ZFE-1",
    "stationId": "AIR-STATION-042",
    "indicator": "NO2",
    "value": 220.5,
    "timestamp": "2026-05-06T14:29:58Z"
  }'
```

Résultat attendu :

* `MeasurementReceived` est publié ;
* `MeasurementValidated` est publié ;
* `AirQualityAlertDetected` est publié ;
* `alerting-service` prépare une notification CSU.

\---

## 16\. CI et automatisation

Chaque microservice possède une pipeline CI dédiée.

Exemples :

```text
ingestion-ci.yml
quality-ci.yml
air-quality-ci.yml
alerting-ci.yml
```

Chaque CI exécute :

```text
compile
unit tests
package
```

Objectifs :

* détecter rapidement les régressions ;
* éviter les commits cassés ;
* garantir que les tests passent.

\---

## 17\. Limites actuelles

Les tests Kafka sont encore principalement manuels.

Améliorations possibles :

* ajouter Testcontainers Kafka ;
* ajouter des tests d’intégration automatisés ;
* ajouter des tests de contrat automatisés ;
* ajouter des tests de performance simples ;
* ajouter des tests sur la DLQ.

\---

## 18\. Synthèse

La stratégie de tests repose sur :

* tests unitaires rapides ;
* mocks pour isoler Kafka ;
* stubs pour contrôler les valeurs générées ;
* tests TDD `Red → Green → Refactor` ;
* tests manuels Postman ;
* tests manuels Kafka ;
* CI par microservice.

Cette stratégie permet de garder le projet fiable, évolutif et cohérent avec les pratiques XP.



