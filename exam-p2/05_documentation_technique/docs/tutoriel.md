# Tutoriel : dÃ©marrer UrbanHub et ingÃ©rer une premiÃ¨re mesure

## Objectif

Ce tutoriel accompagne la premiÃ¨re exÃ©cution de `ingestion-service`.

Ã€ la fin du parcours, vous aurez :

- vÃ©rifiÃ© le code avec Maven ;
- dÃ©marrÃ© Kafka et le service d'ingestion ;
- contrÃ´lÃ© l'Ã©tat de santÃ© du service ;
- envoyÃ© une mesure IoT authentifiÃ©e ;
- vÃ©rifiÃ© la crÃ©ation d'un Ã©vÃ©nement `MeasurementReceived`.

## 1. VÃ©rifier les prÃ©requis

Depuis un terminal :

```bash
java --version
mvn --version
docker --version
docker compose version
```

Versions de rÃ©fÃ©rence :

```text
Java 25
Maven 3.9 ou compatible
Docker avec Compose V2
```

## 2. VÃ©rifier le service avec Maven

Depuis la racine du dÃ©pÃ´t :

```bash
mvn   --file services/ingestion-service/pom.xml   clean verify
```

La commande exÃ©cute notamment :

- Checkstyle ;
- les tests JUnit ;
- les tests REST et de sÃ©curitÃ© ;
- le test non fonctionnel ;
- la gÃ©nÃ©ration du rapport JaCoCo ;
- les gates de couverture ;
- SpotBugs et Find Security Bugs ;
- le packaging du JAR.

RÃ©sultat attendu :

```text
All coverage checks have been met.
BUILD SUCCESS
```

## 3. DÃ©finir une clÃ© API locale

La route d'ingestion est protÃ©gÃ©e par le header `X-API-Key`.

Sous PowerShell :

```powershell
$env:INGESTION_API_KEY = "urbanhub-local-tutorial-key"
```

Sous Bash :

```bash
export INGESTION_API_KEY="urbanhub-local-tutorial-key"
```

Cette valeur sert uniquement Ã  l'environnement local. Elle ne doit pas Ãªtre enregistrÃ©e dans Git.

## 4. DÃ©marrer Kafka et ingestion-service

Depuis la racine du dÃ©pÃ´t :

```bash
docker compose up --build -d kafka ingestion-service
```

Afficher l'Ã©tat des conteneurs :

```bash
docker compose ps
```

Les composants doivent Ãªtre dÃ©marrÃ©s et le service d'ingestion doit devenir sain aprÃ¨s son initialisation.

## 5. VÃ©rifier la readiness

Sous PowerShell :

```powershell
Invoke-RestMethod `
  http://localhost:8082/actuator/health/readiness
```

Avec curl :

```bash
curl http://localhost:8082/actuator/health/readiness
```

RÃ©ponse attendue :

```json
{
  "status": "UP"
}
```

## 6. VÃ©rifier la protection de l'API

Envoyer une requÃªte sans clÃ© API :

```bash
curl -i -X POST http://localhost:8082/api/ingestion/measurements   -H "Content-Type: application/json"   -d '{
    "zoneId": "ZFE-1",
    "stationId": "AIR-STATION-042",
    "indicator": "NO2",
    "value": 220.5,
    "timestamp": "2026-07-27T08:00:00Z"
  }'
```

RÃ©sultat attendu :

```text
HTTP 401 Unauthorized
```

Cette vÃ©rification confirme que la route refuse une passerelle non authentifiÃ©e.

## 7. Envoyer une mesure valide

Avec curl :

```bash
curl -i -X POST http://localhost:8082/api/ingestion/measurements   -H "Content-Type: application/json"   -H "X-API""-Key: ${INGESTION_API_KEY}"   -d '{
    "zoneId": "ZFE-1",
    "stationId": "AIR-STATION-042",
    "indicator": "NO2",
    "value": 220.5,
    "timestamp": "2026-07-27T08:00:00Z"
  }'
```

Sous PowerShell :

```powershell
$body = @{
    zoneId = "ZFE-1"
    stationId = "AIR-STATION-042"
    indicator = "NO2"
    value = 220.5
    timestamp = "2026-07-27T08:00:00Z"
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8082/api/ingestion/measurements" `
  -Headers @{
      "X-API-Key" = $env:INGESTION_API_KEY
  } `
  -ContentType "application/json" `
  -Body $body
```

RÃ©sultat attendu :

```text
HTTP 202 Accepted
```

Exemple de rÃ©ponse :

```json
{
  "status": "ACCEPTED",
  "correlationId": "identifiant-gÃ©nÃ©rÃ©"
}
```

## 8. Comprendre l'Ã©vÃ©nement publiÃ©

AprÃ¨s acceptation, le service publie un Ã©vÃ©nement `MeasurementReceived` dans le topic Kafka `measurements.received`.

L'Ã©vÃ©nement contient notamment :

```json
{
  "eventId": "identifiant-gÃ©nÃ©rÃ©",
  "eventType": "MeasurementReceived",
  "eventVersion": "1.0",
  "correlationId": "identifiant-de-corrÃ©lation",
  "occurredAt": "date-de-publication",
  "source": "ingestion-service",
  "zoneId": "ZFE-1",
  "stationId": "AIR-STATION-042",
  "indicator": "NO2",
  "value": 220.5,
  "timestamp": "2026-07-27T08:00:00Z"
}
```

`correlationId` permet de suivre la mesure dans les microservices situÃ©s en aval.

## 9. Tester une erreur de validation

Envoyer un indicateur inconnu et une valeur nÃ©gative :

```bash
curl -i -X POST http://localhost:8082/api/ingestion/measurements   -H "Content-Type: application/json"   -H "X-API""-Key: ${INGESTION_API_KEY}"   -d '{
    "zoneId": "ZFE-1",
    "stationId": "AIR-STATION-042",
    "indicator": "UNKNOWN",
    "value": -1,
    "timestamp": "2026-07-27T08:00:00Z"
  }'
```

RÃ©sultat attendu :

```text
HTTP 400 Bad Request
```

Aucun Ã©vÃ©nement Kafka ne doit Ãªtre publiÃ© pour une mesure invalide.

## 10. Consulter Swagger

Lorsque Swagger est activÃ© :

```text
http://localhost:8082/swagger-ui.html
```

Utiliser le bouton d'autorisation pour renseigner la clÃ© API, puis exÃ©cuter la route d'ingestion depuis l'interface.

## 11. Consulter les logs

```bash
docker compose logs -f ingestion-service
```

Pour Kafka :

```bash
docker compose logs -f kafka
```

## 12. ArrÃªter l'environnement

```bash
docker compose down --volumes --remove-orphans
```

## RÃ©sultat final

Le parcours est rÃ©ussi lorsque :

- Maven affiche `BUILD SUCCESS` ;
- la readiness retourne `UP` ;
- une requÃªte sans clÃ© retourne HTTP 401 ;
- une requÃªte invalide retourne HTTP 400 ;
- une requÃªte valide retourne HTTP 202 ;
- un Ã©vÃ©nement `MeasurementReceived` est publiÃ© dans Kafka.

