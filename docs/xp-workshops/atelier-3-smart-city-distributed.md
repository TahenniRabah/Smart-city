# Atelier 3 — Adapter XP à la Smart City distribuée

## 1. Contexte

Dans le cadre du projet **Smart City / UrbanHub**, l’architecture évolue vers une approche **multi-services** avec des interactions asynchrones et des API.

L’objectif de cet atelier est d’adapter les pratiques XP à un contexte distribué en sécurisant une interaction critique entre microservices.

L’atelier couvre :

- la cartographie d’une interaction critique ;
- la définition d’un contrat d’événement ;
- l’écriture de tests d’acceptation ;
- l’application d’une démarche TDD sur le microservice consommateur ;
- la mise en place de mesures de robustesse ;
- l’observabilité minimale ;
- une stratégie de small releases.

---

## 2. Interaction critique retenue

L’interaction critique choisie est :

```text
air-quality-service
→ Kafka topic: air-quality.alert.detected
→ alerting-service
```

### Justification

Cette interaction est critique car elle permet de transmettre une alerte de pollution depuis le microservice de qualité de l’air vers un service chargé de préparer les notifications.

Elle répond à plusieurs enjeux Smart City :

- détecter rapidement un dépassement de seuil de pollution ;
- transmettre l’information de manière asynchrone ;
- éviter les couplages forts entre microservices ;
- préparer la notification du CSU ou des citoyens ;
- assurer la traçabilité avec `eventId` et `correlationId` ;
- rendre l’intégration robuste avec idempotence, retry et DLQ.

---

## 3. Vue d’ensemble de l’architecture de l’atelier

```text
┌─────────────────────────┐
│ Client / Test curl       │
└────────────┬────────────┘
             │ POST /api/air-quality/measurements
             v
┌─────────────────────────┐
│ air-quality-service      │
│ - reçoit la mesure       │
│ - calcule le seuil       │
│ - publie un événement    │
└────────────┬────────────┘
             │ Kafka publish
             │ key = zoneId
             v
┌─────────────────────────────────────┐
│ Kafka topic                          │
│ air-quality.alert.detected           │
└────────────┬────────────────────────┘
             │ Kafka consume
             v
┌─────────────────────────┐
│ alerting-service         │
│ - consomme l’événement   │
│ - vérifie l’idempotence  │
│ - prépare notification   │
└─────────────────────────┘
```

---

## 4. État d’implémentation

### 4.1 Partie producteur — `air-quality-service`

La partie producteur est implémentée.

Flux validé :

```text
API REST
→ calcul du niveau d’alerte
→ publication Kafka
→ événement visible dans le topic air-quality.alert.detected
```

Éléments implémentés :

```text
AirQualityAlertDetectedEvent
AirQualityAlertProducer
AirQualityAnalysisService
AirQualityController
KafkaProducerConfig
```

### 4.2 Partie consommateur — `alerting-service`

La partie consommateur est réalisée selon une démarche TDD.

Le principe retenu est :

```text
Kafka Listener = adaptateur technique mince
AlertingService = logique métier testée en TDD
```

Cela permet de tester la logique métier sans dépendre de Kafka.

---

## 5. API exposée par `air-quality-service`

### Endpoint

```http
POST /api/air-quality/measurements
```

### Exemple de requête critique

```json
{
  "zoneId": "ZFE-1",
  "stationId": "AIR-STATION-042",
  "pollutant": "NO2",
  "value": 220.5
}
```

### Exemple de réponse

```json
{
  "alertLevel": "CRITICAL"
}
```

---

## 6. Règles métier de calcul des seuils

### NO2

```text
value < 100              → NORMAL
100 <= value < 200       → WARNING
value >= 200             → CRITICAL
```

### PM10

```text
value < 50               → NORMAL
50 <= value < 80         → WARNING
value >= 80              → CRITICAL
```

### Règle de publication

Un événement Kafka est publié uniquement si le niveau calculé est :

```text
WARNING
CRITICAL
```

Aucun événement n’est publié si le niveau est :

```text
NORMAL
```

---

## 7. Contrat d’événement

### Nom de l’événement

```text
AirQualityAlertDetected
```

### Topic Kafka

```text
air-quality.alert.detected
```

### Producteur

```text
air-quality-service
```

### Consommateur

```text
alerting-service
```

### Clé Kafka

```text
zoneId
```

Le choix de `zoneId` permet de conserver une cohérence de traitement par zone urbaine et facilite le partitionnement des événements.

---

## 8. Exemple d’événement publié

```json
{
  "eventId": "7d7f2af9-5a71-44d8-88d7-f3c8b2c58c11",
  "eventType": "AirQualityAlertDetected",
  "eventVersion": "1.0",
  "occurredAt": "2026-05-06T14:30:00Z",
  "correlationId": "29f5ff30-8a1a-4470-8d73-d7c8f09a92f1",
  "source": "air-quality-service",
  "zoneId": "ZFE-1",
  "stationId": "AIR-STATION-042",
  "pollutant": "NO2",
  "measuredValue": 220.5,
  "unit": "µg/m3",
  "alertLevel": "CRITICAL",
  "threshold": 200.0
}
```

---

## 9. Champs obligatoires

```text
eventId
eventType
eventVersion
occurredAt
correlationId
source
zoneId
stationId
pollutant
measuredValue
unit
alertLevel
threshold
```

---

## 10. Règles de compatibilité du contrat

- `eventVersion` est obligatoire.
- Les consommateurs doivent ignorer les champs inconnus.
- Les champs obligatoires ne doivent pas être supprimés en version `1.x`.
- L’ajout d’un champ optionnel est autorisé sans changement majeur de version.
- Toute modification incompatible du contrat devra produire une version `2.0`.

---

## 11. Tests d’acceptation

### Scénario 1 — Publication et consommation d’une alerte critique

```gherkin
Feature: Transmission d'une alerte qualité de l'air critique

Scenario: Une mesure NO2 critique est publiée puis consommée
  Given une mesure NO2 de valeur 220.5 dans la zone ZFE-1
  And le seuil critique NO2 est fixé à 200
  When le microservice air-quality-service analyse la mesure
  Then le niveau d'alerte retourné est CRITICAL
  And un événement AirQualityAlertDetected est publié sur le topic air-quality.alert.detected
  And l'événement contient zoneId "ZFE-1"
  And l'événement contient alertLevel "CRITICAL"
  When le microservice alerting-service consomme cet événement
  Then une notification prioritaire CSU est préparée
```

### Scénario 2 — Idempotence lors d’un doublon

```gherkin
Feature: Idempotence des événements d'alerte

Scenario: Un événement déjà traité ne déclenche pas une deuxième notification
  Given un événement AirQualityAlertDetected avec eventId "event-1" a déjà été traité
  When le microservice alerting-service reçoit à nouveau le même événement
  Then aucune nouvelle notification CSU n'est préparée
  And l'événement dupliqué est ignoré
```

---

## 12. Démarche TDD sur `alerting-service`

La logique métier du microservice `alerting-service` a été développée en TDD.

Le choix de conception est de séparer :

```text
AlertingService          → logique métier
NotificationPort         → port de notification
ProcessedEventRepository → port d’idempotence
KafkaListener            → adaptateur technique
```

---

## 13. Boucle TDD 1 — Notification CSU pour alerte critique

### Red

Premier test ajouté :

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

Résultat initial :

```text
Échec de compilation ou test rouge car AlertingService n'existe pas encore.
```

### Green

Implémentation minimale :

```java
public void handle(AirQualityAlertDetectedEvent event) {
    if ("CRITICAL".equals(event.alertLevel())) {
        notificationPort.notifyCsu(event);
    }

    processedEventRepository.markAsProcessed(event.eventId());
}
```

Résultat :

```text
Le test passe.
```

### Refactor

La condition de criticité est extraite dans une méthode dédiée :

```java
private boolean isCritical(AirQualityAlertDetectedEvent event) {
    return "CRITICAL".equals(event.alertLevel());
}
```

---

## 14. Boucle TDD 2 — Idempotence par `eventId`

### Red

Deuxième test ajouté :

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

Résultat initial :

```text
Le test échoue car le service notifie encore les événements déjà traités.
```

### Green

Ajout de la vérification d’idempotence :

```java
public void handle(AirQualityAlertDetectedEvent event) {
    if (processedEventRepository.hasAlreadyBeenProcessed(event.eventId())) {
        return;
    }

    if (isCritical(event)) {
        notificationPort.notifyCsu(event);
    }

    processedEventRepository.markAsProcessed(event.eventId());
}
```

Résultat :

```text
Le test passe.
```

### Refactor

La logique métier reste isolée dans `AlertingService`, tandis que Kafka reste dans un adaptateur séparé.

---

## 15. Test complémentaire — Alerte WARNING

Un test complémentaire vérifie qu’une alerte `WARNING` est traitée mais ne déclenche pas de notification prioritaire CSU.

```java
@Test
void shouldNotNotifyCsuWhenAlertLevelIsWarning() {
    AirQualityAlertDetectedEvent event = warningNo2Event("event-2");

    when(processedEventRepository.hasAlreadyBeenProcessed("event-2"))
            .thenReturn(false);

    alertingService.handle(event);

    verifyNoInteractions(notificationPort);
    verify(processedEventRepository).markAsProcessed("event-2");
}
```

---

## 16. Implémentation technique du consommateur Kafka

Le listener Kafka reste volontairement mince.

```java
@KafkaListener(
        topics = "${urbanhub.kafka.topics.air-quality-alert-detected}",
        containerFactory = "airQualityAlertKafkaListenerContainerFactory"
)
public void consume(AirQualityAlertDetectedEvent event) {
    log.info(
            "Air quality alert consumed: eventId={}, correlationId={}, zoneId={}, pollutant={}, level={}",
            event.eventId(),
            event.correlationId(),
            event.zoneId(),
            event.pollutant(),
            event.alertLevel()
    );

    alertingService.handle(event);
}
```

Le listener ne contient pas de logique métier complexe. Il délègue au service testé :

```text
AirQualityAlertConsumer
→ AlertingService.handle(event)
```

---

## 17. Configuration Kafka consumer

Configuration attendue côté `alerting-service` :

```yaml
server:
  port: 8081

spring:
  application:
    name: alerting-service

  kafka:
    bootstrap-servers: localhost:9092

urbanhub:
  kafka:
    topics:
      air-quality-alert-detected: air-quality.alert.detected
```

La désérialisation utilise `JacksonJsonDeserializer` afin de rester compatible avec Spring Kafka 4.

---

## 18. Mesures de robustesse

### 18.1 Idempotence

Le champ `eventId` sert de clé technique d’idempotence.

Règle :

```text
Si eventId a déjà été traité, l’événement est ignoré.
```

Implémentation actuelle :

```text
InMemoryProcessedEventRepository
```

Limite :

```text
Cette implémentation est suffisante pour l’atelier, mais en production elle devrait être remplacée par un stockage persistant.
```

### 18.2 Retry contrôlé

En cas d’erreur temporaire côté consommateur, le traitement doit être rejoué un nombre limité de fois.

Stratégie proposée :

```text
Tentative 1 : immédiate
Tentative 2 : après 1 seconde
Tentative 3 : après 5 secondes
```

### 18.3 Dead Letter Queue

Topic DLQ prévu :

```text
air-quality.alert.detected.dlq
```

La DLQ reçoit les événements impossibles à traiter après les retries.

Exemples :

```text
payload invalide
zoneId manquant
alertLevel inconnu
pollutant non supporté
erreur de désérialisation
```

---

## 19. Schéma de robustesse

```text
┌─────────────────────┐
│ air-quality-service  │
└──────────┬──────────┘
           │ publish AirQualityAlertDetected
           v
┌───────────────────────────────────┐
│ topic: air-quality.alert.detected │
└──────────┬────────────────────────┘
           │ consume
           v
┌─────────────────────┐
│ alerting-service     │
└──────────┬──────────┘
           │
           ├── Vérification eventId déjà traité
           ├── Notification CSU si CRITICAL
           ├── Marquage eventId traité
           ├── Retry contrôlé en cas d’erreur
           └── DLQ si échecs répétés
```

---

## 20. Observabilité minimale

### Logs structurés

Chaque événement publié ou consommé doit être traçable avec les champs suivants :

```text
eventId
correlationId
zoneId
stationId
pollutant
alertLevel
processingStatus
timestamp
```

Exemple côté producteur :

```text
eventPublished=true eventType=AirQualityAlertDetected zoneId=ZFE-1 pollutant=NO2 alertLevel=CRITICAL
```

Exemple côté consommateur :

```text
eventConsumed=true eventId=... zoneId=ZFE-1 alertLevel=CRITICAL processingStatus=PROCESSED
```

Exemple côté notification :

```text
Priority CSU notification prepared: eventId=..., zoneId=ZFE-1, stationId=AIR-STATION-042, pollutant=NO2, level=CRITICAL
```

### Métriques minimales proposées

```text
Nombre d’événements publiés
Nombre d’événements consommés
Nombre d’événements dupliqués
Nombre d’erreurs de traitement
Nombre d’événements envoyés en DLQ
Latence entre occurredAt et traitement
Consumer lag Kafka
```

### Tracing

Le champ `correlationId` permet de suivre le flux complet :

```text
API REST
→ air-quality-service
→ Kafka
→ alerting-service
→ notification CSU
```

---

## 21. Stratégie de small releases

### Release 1 — Contrat événementiel

Objectif : documenter le contrat `AirQualityAlertDetected`.

Livrables :

```text
Contrat JSON
Topic Kafka
Règles de compatibilité
```

### Release 2 — Producteur Kafka

Objectif : publier un événement depuis `air-quality-service` lorsque le niveau d’alerte est `WARNING` ou `CRITICAL`.

Livrables réalisés :

```text
AirQualityAlertDetectedEvent
AirQualityAlertProducer
Endpoint REST de test
Publication Kafka validée manuellement
```

### Release 3 — Consommateur minimal

Objectif : créer `alerting-service` et consommer les événements `AirQualityAlertDetected`.

Livrables réalisés ou en cours :

```text
AirQualityAlertConsumer
AlertingService
ConsoleNotificationAdapter
InMemoryProcessedEventRepository
```

### Release 4 — Robustesse

Objectif : ajouter idempotence, retry et DLQ.

Livrables :

```text
Idempotence par eventId
Retry contrôlé
Topic DLQ
```

### Release 5 — Observabilité

Objectif : ajouter des métriques et des logs structurés exploitables.

Livrables :

```text
Logs eventId / correlationId
Métriques Kafka
Suivi DLQ
```

---

## 22. Tests réalisés manuellement

### Test 1 — Mesure critique NO2

Commande :

```bash
curl -X POST http://localhost:8080/api/air-quality/measurements \
  -H "Content-Type: application/json" \
  -d '{
    "zoneId": "ZFE-1",
    "stationId": "AIR-STATION-042",
    "pollutant": "NO2",
    "value": 220.5
  }'
```

Résultat REST attendu :

```json
{
  "alertLevel": "CRITICAL"
}
```

Résultat Kafka attendu :

```text
Un événement AirQualityAlertDetected est publié dans air-quality.alert.detected.
```

Résultat attendu côté `alerting-service` :

```text
Air quality alert consumed: eventId=..., zoneId=ZFE-1, pollutant=NO2, level=CRITICAL
Priority CSU notification prepared: eventId=..., zoneId=ZFE-1, stationId=AIR-STATION-042, pollutant=NO2, level=CRITICAL
```

### Test 2 — Mesure normale NO2

Commande :

```bash
curl -X POST http://localhost:8080/api/air-quality/measurements \
  -H "Content-Type: application/json" \
  -d '{
    "zoneId": "ZFE-1",
    "stationId": "AIR-STATION-042",
    "pollutant": "NO2",
    "value": 80
  }'
```

Résultat REST attendu :

```json
{
  "alertLevel": "NORMAL"
}
```

Résultat Kafka attendu :

```text
Aucun nouvel événement n’est publié.
```

---

## 23. Diagnostic en cas de non-consommation Kafka

Si `alerting-service` ne consomme pas, vérifier dans l’ordre :

```text
1. Le topic existe bien : air-quality.alert.detected
2. Le producer publie réellement un message visible avec kafka-console-consumer
3. Le service alerting-service démarre sans erreur
4. @EnableKafka est présent ou KafkaListener est bien activé
5. Le @KafkaListener pointe vers le bon topic
6. Le containerFactory référencé existe bien
7. Le group.id n’a pas déjà consommé les anciens messages
8. Le désérialiseur JacksonJsonDeserializer est compatible avec l’événement
9. Le record AirQualityAlertDetectedEvent côté consumer correspond au JSON publié
10. Les classes sont bien dans le package scanné par Spring Boot
```

Commande utile pour vérifier les messages dans le topic :

```bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic air-quality.alert.detected --from-beginning
```

Astuce de debug : changer temporairement le consumer group pour relire les messages :

```text
alerting-service-test-1
```

---

## 24. CI/CD minimale

Une CI minimale doit être définie pour chaque microservice.

### CI `air-quality-service`

```text
compile
unit tests
build
```

### CI `alerting-service`

```text
compile
unit tests
build
```

Exemple d’objectif :

```text
Le pipeline doit rester rapide et inférieur à 10 minutes.
```

---

## 25. Rétrospective XP

### Ce qui a bien fonctionné

- Le découpage par small releases a permis de sécuriser progressivement l’intégration.
- La logique métier du consommateur a été testée avant le branchement Kafka.
- L’utilisation de ports (`NotificationPort`, `ProcessedEventRepository`) facilite les tests unitaires.
- Le listener Kafka reste mince et ne contient pas de logique métier complexe.

### Difficultés rencontrées

- La configuration Kafka peut masquer les erreurs de topic, de group ou de désérialisation.
- Le typage JSON entre producer et consumer doit rester cohérent.
- L’offset Kafka peut donner l’impression que le consumer ne fonctionne pas si les messages ont déjà été consommés.

### Améliorations possibles

- Ajouter un test d’intégration avec Testcontainers Kafka.
- Persister les `eventId` traités dans une base de données.
- Implémenter réellement retry + DLQ.
- Ajouter des métriques Micrometer.
- Ajouter un dashboard d’observabilité.

---

## 26. Preuves à joindre au rendu

À joindre sous forme de captures d’écran ou d’extraits :

```text
Capture du curl critique
Capture de la réponse REST CRITICAL
Capture du kafka-console-consumer montrant l’événement publié
Capture des logs alerting-service montrant la consommation
Capture des tests unitaires alerting-service verts
Capture de la CI verte
Extrait du contrat AirQualityAlertDetected
```

---

## 27. Conclusion

L’atelier a permis de sécuriser une interaction critique dans une architecture Smart City distribuée.

La chaîne cible est :

```text
API REST
→ air-quality-service
→ Kafka
→ alerting-service
→ notification CSU
```

La démarche XP a été appliquée côté `alerting-service` avec :

```text
TDD
petites boucles Red-Green-Refactor
séparation logique métier / infrastructure
small releases
observabilité minimale
robustesse par idempotence, retry et DLQ
```

Cette approche permet de faire évoluer progressivement l’architecture sans bloquer le projet par une intégration trop lourde dès le départ.
