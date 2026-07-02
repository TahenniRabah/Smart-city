# Clean Code — Exemples SOLID, DRY, KISS et Refactoring

## 1\. Objectif

Ce document présente plusieurs exemples de Clean Code appliqués dans le projet **Smart City / UrbanHub**.

Les exemples couvrent :

* SOLID ;
* DRY ;
* KISS ;
* séparation des responsabilités ;
* encapsulation ;
* refactoring sécurisé par les tests ;
* usage de mocks et ports applicatifs.

\---

## 2\. Exemple SOLID — Single Responsibility Principle

### Contexte

Dans le service `ingestion-service`, la logique métier d’ingestion est séparée de la publication Kafka.

La classe `MeasurementIngestionService` ne connaît pas directement Kafka. Elle dépend d’un port applicatif :

```java
public interface MeasurementReceivedPublisher {

    void publish(MeasurementReceivedEvent event);

}
```

L’implémentation Kafka est isolée dans une classe dédiée :

```java
@Component
public class KafkaMeasurementReceivedPublisher implements MeasurementReceivedPublisher {

    @Override
    public void publish(MeasurementReceivedEvent event) {
        kafkaTemplate.send(topicName, event.zoneId(), event);
    }
}
```

Chaque classe a une seule responsabilité :

* `MeasurementIngestionService` → logique métier d’ingestion ;
* `KafkaMeasurementReceivedPublisher` → publication Kafka ;
* `MeasurementReceivedEvent` → contrat événementiel.

\---

## 3\. SOLID — Dependency Inversion Principle

La classe métier dépend d’une abstraction et non d’une implémentation technique Kafka.

```java
public class MeasurementIngestionService {

    private final MeasurementReceivedPublisher publisher;
    private final CorrelationIdGenerator correlationIdGenerator;
}
```

En test unitaire, le publisher peut être mocké :

```java
private final MeasurementReceivedPublisher publisher =
        mock(MeasurementReceivedPublisher.class);
```

Cela permet de tester la logique métier sans démarrer Kafka.

\---

## 4\. SOLID — Open/Closed Principle avec le State Pattern

La classe `Sensor` utilise le design pattern **State**.

Un capteur peut être dans plusieurs états :

* `ACTIVE` ;
* `INACTIVE` ;
* `FAULTY`.

Interface commune :

```java
public interface SensorState {

    boolean canSendMeasurement();

    String name();
}
```

États implémentés :

* `ActiveSensorState` ;
* `InactiveSensorState` ;
* `FaultySensorState`.

La classe `Sensor` est ouverte à l’extension : on peut ajouter un nouvel état sans modifier fortement le code existant.

\---

## 5\. Encapsulation dans la classe `Sensor`

```java
public class Sensor {

    private final String sensorId;
    private final String stationId;
    private final String zoneId;
    private SensorState state;

    public boolean canSendMeasurement() {
        return state.canSendMeasurement();
    }

    public void activate() {
        this.state = new ActiveSensorState();
    }

    public void deactivate() {
        this.state = new InactiveSensorState();
    }

    public void markAsFaulty() {
        this.state = new FaultySensorState();
    }
}
```

Les attributs sont privés et les transitions d’état passent par des méthodes métier.

\---

## 6\. DRY — Don’t Repeat Yourself

Les valeurs répétées sont centralisées dans des constantes.

```java
private static final String EVENT\\\_TYPE = "MeasurementReceived";
private static final String EVENT\\\_VERSION = "1.0";
private static final String SOURCE = "ingestion-service";
private static final String ACCEPTED = "ACCEPTED";
```

Avantages :

* moins de duplication ;
* moins d’erreurs de saisie ;
* meilleure lisibilité ;
* évolution plus simple.

\---

## 7\. KISS — Keep It Simple

Dans `quality-service`, les règles restent simples et lisibles.

```java
private String rejectionReason(MeasurementReceivedEvent event) {
    if (isBlank(event.zoneId())) return "zoneId is required";
    if (isBlank(event.stationId())) return "stationId is required";
    if (isBlank(event.indicator())) return "indicator is required";
    if (event.timestamp() == null) return "timestamp is required";
    if (event.value() < 0) return "value must be positive or zero";

    return null;
}
```

La règle est compréhensible, testable et adaptée à une première small release.

\---

## 8\. Exemple de refactoring sécurisé par les tests

Avant refactor, la validation pouvait être directement écrite dans la méthode principale.

Après refactor, elle est isolée dans une méthode dédiée :

```java
private String rejectionReason(MeasurementReceivedEvent event) {
    if (isBlank(event.zoneId())) {
        return "zoneId is required";
    }

    if (isBlank(event.stationId())) {
        return "stationId is required";
    }

    if (isBlank(event.indicator())) {
        return "indicator is required";
    }

    if (event.timestamp() == null) {
        return "timestamp is required";
    }

    if (event.value() < 0) {
        return "value must be positive or zero";
    }

    return null;
}
```

Bénéfices :

* méthode principale plus courte ;
* règles métier isolées ;
* lisibilité améliorée ;
* tests unitaires conservés ;
* refactor sécurisé.

\---

## 9\. Exemple de mock

Dans `MeasurementIngestionServiceTest`, le publisher Kafka est remplacé par un mock.

```java
private final MeasurementReceivedPublisher publisher =
        mock(MeasurementReceivedPublisher.class);
```

Cela permet de tester la logique métier sans dépendre de Kafka.

\---

## 10\. Exemple de stub

Le générateur de `correlationId` est stubbé pour retourner une valeur connue.

```java
when(correlationIdGenerator.generate()).thenReturn("corr-12345");
```

Cela rend le test déterministe.

\---

## 11\. Exemple de test unitaire

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
    assertEquals("ZFE-1", event.zoneId());
}
```

Ce test démontre que :

* la mesure est acceptée ;
* un `correlationId` est généré ;
* un événement est publié ;
* Kafka est mocké ;
* le test reste unitaire.

\---

## 12\. Séparation métier / infrastructure

### Logique métier

* `MeasurementIngestionService` ;
* `MeasurementQualityService` ;
* `AirQualityAnalysisService` ;
* `AlertingService`.

### Infrastructure

* `KafkaMeasurementReceivedPublisher` ;
* `MeasurementReceivedConsumer` ;
* `AirQualityAlertProducer` ;
* `AirQualityAlertConsumer` ;
* `KafkaProducerConfig` ;
* `KafkaConsumerConfig`.

Cette séparation permet :

* des tests plus simples ;
* moins de couplage technique ;
* une meilleure maintenabilité ;
* une architecture plus proche des principes hexagonaux.

\---

## 13\. Synthèse

Le projet applique plusieurs pratiques Clean Code :

* **SOLID** : séparation des responsabilités et dépendance aux abstractions ;
* **DRY** : constantes pour éviter les duplications ;
* **KISS** : règles métier simples et lisibles ;
* **Refactor** : extraction des règles de validation ;
* **POO** : encapsulation dans `Sensor` ;
* **Design Pattern** : State Pattern pour gérer les états du capteur ;
* **Tests** : mocks, stubs et tests unitaires rapides.

Ces pratiques rendent le projet plus maintenable, testable et évolutif.



