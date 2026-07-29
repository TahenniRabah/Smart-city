# RÃ©fÃ©rence API du service d'ingestion

## Informations gÃ©nÃ©rales

| PropriÃ©tÃ© | Valeur |
|---|---|
| Service | `ingestion-service` |
| URL locale | `http://localhost:8082` |
| Format | JSON |
| Authentification | ClÃ© API dans le header `X-API-Key` |
| Documentation interactive | `/swagger-ui.html` |
| SpÃ©cification OpenAPI | `/v3/api-docs` |

## Authentification

La route d'ingestion exige le header suivant :

```http
X-API-Key: <clÃ©-configurÃ©e>
```

La clÃ© est fournie au service avec la variable d'environnement :

```text
INGESTION_API_KEY
```

Une clÃ© absente ou invalide produit une rÃ©ponse HTTP 401. Aucun secret rÃ©el ne doit Ãªtre enregistrÃ© dans le dÃ©pÃ´t ou dans la documentation.

## IngÃ©rer une mesure

### RequÃªte

```http
POST /api/ingestion/measurements
Content-Type: application/json
X-API-Key: <clÃ©-configurÃ©e>
```

### Corps JSON

```json
{
  "zoneId": "ZFE-1",
  "stationId": "AIR-STATION-042",
  "indicator": "NO2",
  "value": 220.5,
  "timestamp": "2026-07-27T08:00:00Z"
}
```

### SchÃ©ma d'entrÃ©e

| Champ | Type | Obligatoire | Contraintes | Exemple |
|---|---|---:|---|---|
| `zoneId` | chaÃ®ne | Oui | Non vide, 50 caractÃ¨res maximum, lettres, chiffres, `_` ou `-` | `ZFE-1` |
| `stationId` | chaÃ®ne | Oui | Non vide, 100 caractÃ¨res maximum, lettres, chiffres, `_` ou `-` | `AIR-STATION-042` |
| `indicator` | chaÃ®ne | Oui | Une valeur parmi `NO2`, `PM10`, `PM25` | `NO2` |
| `value` | nombre dÃ©cimal | Oui | Entre 0 et 5 000 inclus | `220.5` |
| `timestamp` | date ISO 8601 | Oui | Date passÃ©e ou prÃ©sente | `2026-07-27T08:00:00Z` |

### RÃ©ponse acceptÃ©e

Statut :

```text
202 Accepted
```

Corps :

```json
{
  "status": "ACCEPTED",
  "correlationId": "identifiant-gÃ©nÃ©rÃ©"
}
```

### SchÃ©ma de sortie

| Champ | Type | Description |
|---|---|---|
| `status` | chaÃ®ne | Statut fonctionnel de la prise en charge |
| `correlationId` | chaÃ®ne | Identifiant utilisÃ© pour suivre la mesure dans la chaÃ®ne distribuÃ©e |

## Exemples de requÃªtes

### curl

```bash
curl -i -X POST http://localhost:8082/api/ingestion/measurements   -H "Content-Type: application/json"   -H "X-API""-Key: ${INGESTION_API_KEY}"   -d '{
    "zoneId": "ZFE-1",
    "stationId": "AIR-STATION-042",
    "indicator": "NO2",
    "value": 220.5,
    "timestamp": "2026-07-27T08:00:00Z"
  }'
```

### PowerShell

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

## Codes de rÃ©ponse

| Statut | Code d'erreur | Signification | Action recommandÃ©e |
|---:|---|---|---|
| `202` | Aucun | Mesure acceptÃ©e | Conserver le `correlationId` |
| `400` | `VALIDATION_ERROR` | Un ou plusieurs champs sont invalides | Corriger les champs indiquÃ©s |
| `400` | `MALFORMED_JSON` | JSON absent ou illisible | VÃ©rifier la syntaxe et le `Content-Type` |
| `400` | `INVALID_MEASUREMENT` | Invariant mÃ©tier non respectÃ© | Corriger la mesure |
| `401` | `UNAUTHORIZED` | ClÃ© API absente ou invalide | Fournir une clÃ© valide |
| `429` | `RATE_LIMIT_EXCEEDED` | Quota de requÃªtes Ã©puisÃ© | Respecter le header `Retry-After` |
| `500` | Variable | Erreur interne non prÃ©vue | Consulter les logs et le guide de dÃ©pannage |

## Format d'erreur standard

```json
{
  "timestamp": "2026-07-29T10:00:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "The request contains invalid fields",
  "path": "/api/ingestion/measurements",
  "fieldErrors": {
    "indicator": "indicator must be one of: NO2, PM10, PM25"
  }
}
```

### Champs d'erreur

| Champ | Type | Description |
|---|---|---|
| `timestamp` | date ISO 8601 | Date de crÃ©ation de la rÃ©ponse |
| `status` | entier | Statut HTTP |
| `error` | chaÃ®ne | Code stable de l'erreur |
| `message` | chaÃ®ne | RÃ©sumÃ© lisible |
| `path` | chaÃ®ne | Route appelÃ©e |
| `fieldErrors` | objet | Erreurs indexÃ©es par champ |

## Rate limiting

Le service applique un token bucket par identitÃ© authentifiÃ©e.

### Headers de rÃ©ponse

| Header | Description |
|---|---|
| `X-Rate-Limit-Remaining` | Nombre de jetons encore disponibles |
| `Retry-After` | DÃ©lai minimal avant une nouvelle tentative aprÃ¨s HTTP 429 |

### Exemple HTTP 429

```json
{
  "timestamp": "2026-07-29T10:00:00Z",
  "status": 429,
  "error": "RATE_LIMIT_EXCEEDED",
  "message": "Too many ingestion requests",
  "path": "/api/ingestion/measurements",
  "fieldErrors": {}
}
```

## SantÃ© du service

### Readiness

```http
GET /actuator/health/readiness
```

RÃ©ponse attendue :

```json
{
  "status": "UP"
}
```

### Information

```http
GET /actuator/info
```

Seuls les endpoints Actuator explicitement autorisÃ©s doivent Ãªtre exposÃ©s.

## Swagger et OpenAPI

Lorsque la documentation interactive est activÃ©e :

```text
http://localhost:8082/swagger-ui.html
```

La spÃ©cification JSON gÃ©nÃ©rÃ©e est disponible Ã  :

```text
http://localhost:8082/v3/api-docs
```

Une copie exportÃ©e est fournie dans :

```text
openapi.json
```

## Ã‰vÃ©nement produit

Une mesure acceptÃ©e produit un Ã©vÃ©nement dans :

```text
Topic : measurements.received
ClÃ© Kafka : zoneId
Type : MeasurementReceived
Version : 1.0
```

Exemple :

```json
{
  "eventId": "identifiant-gÃ©nÃ©rÃ©",
  "eventType": "MeasurementReceived",
  "eventVersion": "1.0",
  "correlationId": "identifiant-de-corrÃ©lation",
  "occurredAt": "2026-07-29T10:00:00Z",
  "source": "ingestion-service",
  "zoneId": "ZFE-1",
  "stationId": "AIR-STATION-042",
  "indicator": "NO2",
  "value": 220.5,
  "timestamp": "2026-07-27T08:00:00Z"
}
```

Le contrat JSON ne dÃ©pend pas du nom complet d'une classe Java.

