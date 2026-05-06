# Atelier 3 — Adapter XP à la Smart City distribuée

## 1. Contexte

Dans le cadre du projet **Smart City / UrbanHub**, l’architecture évolue vers une approche **multi-services** avec des interactions asynchrones et des API.

L’objectif de cet atelier est de sécuriser une interaction critique entre microservices en définissant :

- une interaction critique API / événement ;
- deux tests d’acceptation ;
- un contrat d’événement ;
- des mesures de robustesse ;
- une observabilité minimale ;
- une stratégie de small releases.

Le projet UrbanHub couvre notamment la supervision du trafic, la qualité de l’air, les alertes opérationnelles et la coordination d’incidents. L’architecture cible s’appuie sur une chaîne événementielle avec ingestion, analyse, stockage, alerting et dashboard.

---

## 2. Interaction critique retenue

L’interaction critique choisie est :

```text
air-quality-service
→ Kafka topic: air-quality.alert.detected
→ alerting-service
```

### Justification du choix

Cette interaction est critique car elle permet de transmettre une alerte de pollution depuis le microservice de qualité de l’air vers un service d’alerting.

Elle répond à plusieurs enjeux du projet Smart City :

- détecter rapidement un dépassement de seuil de pollution ;
- transmettre l’information de manière asynchrone ;
- permettre la notification du CSU ou des citoyens ;
- assurer la traçabilité via `eventId` et `correlationId` ;
- préparer la résilience avec idempotence, retry et DLQ.

---

## 3. État d’implémentation actuel

Dans cette première small release, la partie **producer** a été implémentée côté `air-quality-service`.

### Réalisé

```text
API REST
→ calcul du niveau d’alerte
→ publication Kafka
→ événement visible dans le topic air-quality.alert.detected
```

### Non encore implémenté

Le microservice `alerting-service` n’est pas encore implémenté dans cette version.

Il est prévu dans une small release suivante pour :

- consommer les événements `AirQualityAlertDetected` ;
- appliquer l’idempotence ;
- préparer une notification prioritaire si le niveau est `CRITICAL` ;
- gérer les retries et la DLQ.

---

## 4. Cartographie de l’interaction

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
│ topic Kafka                          │
│ air-quality.alert.detected           │
└────────────┬────────────────────────┘
             │ Kafka consume
             v
┌─────────────────────────┐
│ alerting-service         │
│ - consomme l’événement   │
│ - déduplique             │
│ - notifie si CRITICAL    │
└─────────────────────────┘
```

---

## 5. API exposée par `air-quality-service`

### Endpoint

```http
POST /api/air-quality/measurements
```

### Exemple de requête

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

### Règle de publication Kafka

Un événement Kafka est publié uniquement si le niveau calculé est :

```text
WARNING
CRITICAL
```

Aucun événement n’est publié pour un niveau :

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

### Consommateur cible

```text
alerting-service
```

### Clé Kafka

```text
zoneId
```

Le choix de `zoneId` permet de conserver un traitement cohérent par zone urbaine et facilite le partitionnement des événements.

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

## 9. Champs obligatoires du contrat

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

## 10. Règles de compatibilité

- `eventVersion` est obligatoire.
- Les consommateurs doivent ignorer les champs inconnus.
- Les champs obligatoires ne doivent pas être supprimés en version `1.x`.
- L’ajout d’un champ optionnel est autorisé sans changement majeur de version.
- Toute modification incompatible du contrat devra produire une version `2.0`.

---

## 11. Tests d’acceptation

### Scénario 1 — Publication d’une alerte critique

```gherkin
Feature: Publication d'une alerte qualité de l'air

Scenario: Une mesure NO2 critique publie un événement Kafka
  Given une mesure NO2 de valeur 220.5 dans la zone ZFE-1
  And le seuil critique NO2 est fixé à 200
  When le microservice air-quality-service analyse la mesure
  Then le niveau d'alerte retourné est CRITICAL
  And un événement AirQualityAlertDetected est publié sur le topic air-quality.alert.detected
  And l'événement contient zoneId "ZFE-1"
  And l'événement contient alertLevel "CRITICAL"
```

### Scénario 2 — Une mesure normale ne publie pas d’événement

```gherkin
Feature: Filtrage des mesures sans alerte

Scenario: Une mesure NO2 normale ne publie pas d'événement Kafka
  Given une mesure NO2 de valeur 80 dans la zone ZFE-1
  And le seuil warning NO2 est fixé à 100
  When le microservice air-quality-service analyse la mesure
  Then le niveau d'alerte retourné est NORMAL
  And aucun événement AirQualityAlertDetected n'est publié
```

---

## 12. Mesures de robustesse

### 12.1 Idempotence

Le champ `eventId` sert de clé technique d’idempotence.

Objectif : éviter qu’un même événement traité plusieurs fois entraîne plusieurs notifications.

Règle prévue côté `alerting-service` :

```text
Si eventId a déjà été traité, l'événement est ignoré.
```

Une clé métier de déduplication peut également être utilisée :

```text
zoneId + stationId + pollutant + alertLevel
```

Cette clé permet d’éviter plusieurs alertes identiques sur une même station dans une fenêtre courte.

---

### 12.2 Retry contrôlé

En cas d’erreur temporaire côté consommateur, le traitement doit être rejoué un nombre limité de fois.

Stratégie proposée :

```text
Tentative 1 : immédiate
Tentative 2 : après 1 seconde
Tentative 3 : après 5 secondes
```

Après plusieurs échecs, l’événement ne doit pas bloquer le flux principal.

---

### 12.3 Dead Letter Queue

Topic DLQ prévu :

```text
air-quality.alert.detected.dlq
```

La DLQ reçoit les événements impossibles à traiter après les retries.

Exemples de cas envoyés en DLQ :

```text
payload invalide
zoneId manquant
alertLevel inconnu
pollutant non supporté
erreur de désérialisation
```

Objectif : ne pas bloquer le topic principal et permettre une analyse ou un rejeu manuel.

---

## 13. Schéma de robustesse

```text
┌─────────────────────┐
│ air-quality-service  │
└──────────┬──────────┘
           │
           │ publish AirQualityAlertDetected
           v
┌───────────────────────────────────┐
│ topic: air-quality.alert.detected │
└──────────┬────────────────────────┘
           │
           │ consume
           v
┌─────────────────────┐
│ alerting-service     │
└──────────┬──────────┘
           │
           ├── Idempotence par eventId
           ├── Retry contrôlé
           └── DLQ après échecs répétés
```

---

## 14. Observabilité minimale

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

Exemple de log attendu côté producteur :

```text
eventPublished=true eventType=AirQualityAlertDetected zoneId=ZFE-1 pollutant=NO2 alertLevel=CRITICAL
```

Exemple de log attendu côté consommateur :

```text
eventConsumed=true eventId=... zoneId=ZFE-1 alertLevel=CRITICAL processingStatus=PROCESSED
```

### Métriques minimales

```text
Nombre d'événements publiés
Nombre d'événements consommés
Nombre d'événements dupliqués
Nombre d'erreurs de traitement
Nombre d'événements en DLQ
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
→ notification CSU / citoyens
```

---

## 15. Stratégie de small releases

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

Livrables prévus :

```text
Kafka consumer
Logs de consommation
Préparation notification CSU si CRITICAL
```

### Release 4 — Robustesse

Objectif : ajouter idempotence, retry et DLQ.

Livrables prévus :

```text
Déduplication par eventId
Retry contrôlé
Topic DLQ
```

### Release 5 — Observabilité

Objectif : ajouter des métriques et des logs structurés exploitables.

Livrables prévus :

```text
Logs eventId / correlationId
Métriques Kafka
Suivi DLQ
```

---

## 16. Tests réalisés

### Test 1 — Mesure critique NO2

Commande utilisée :

```bash
curl -X POST http://localhost:8080/api/air-quality/measurements   -H "Content-Type: application/json"   -d '{
    "zoneId": "ZFE-1",
    "stationId": "AIR-STATION-042",
    "pollutant": "NO2",
    "value": 220.5
  }'
```

Résultat REST obtenu :

```json
{
  "alertLevel": "CRITICAL"
}
```

Résultat Kafka :

```text
Un événement AirQualityAlertDetected est visible dans le topic air-quality.alert.detected.
```

### Test 2 — Mesure normale NO2

Commande utilisée :

```bash
curl -X POST http://localhost:8080/api/air-quality/measurements   -H "Content-Type: application/json"   -d '{
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
Aucun nouvel événement n'est publié.
```

---

## 17. Preuves à joindre au rendu

À joindre sous forme de captures d’écran ou d’extraits :

```text
Capture du curl avec une mesure critique
Capture de la réponse REST CRITICAL
Capture du kafka-console-consumer montrant l’événement publié
Capture de la CI verte
Extrait du contrat AirQualityAlertDetected
```

---

## 18. Conclusion

L’atelier a permis de sécuriser une première interaction critique dans une architecture Smart City distribuée.

La partie producteur est opérationnelle :

```text
API REST
→ calcul du niveau d’alerte
→ publication Kafka
→ événement visible dans air-quality.alert.detected
```

Les mesures de robustesse prévues sont :

```text
idempotence
retry contrôlé
DLQ
```

La suite naturelle est la création du microservice `alerting-service`, qui consommera les événements, appliquera l’idempotence et préparera les notifications critiques.
