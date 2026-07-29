# Diagramme de séquence du flux d'ingestion

## Flux nominal

Le diagramme suivant décrit le traitement complet d'une mesure valide, depuis la passerelle IoT jusqu'à la publication Kafka.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Passerelle IoT
    participant ApiKey as ApiKeyAuthenticationFilter
    participant RateLimit as RateLimitFilter
    participant Controller as IngestionController
    participant Service as MeasurementIngestionService
    participant Correlation as CorrelationIdGenerator
    participant Publisher as MeasurementReceivedPublisher
    participant Kafka as Apache Kafka

    Client->>ApiKey: POST /api/ingestion/measurements<br/>X-API-Key + JSON
    ApiKey->>ApiKey: Comparer la clé reçue<br/>avec la clé configurée
    ApiKey->>RateLimit: Requête authentifiée
    RateLimit->>RateLimit: Consommer un jeton
    RateLimit->>Controller: Continuer la requête
    Controller->>Controller: Désérialiser le JSON
    Controller->>Controller: Appliquer Bean Validation
    Controller->>Service: ingest(RawMeasurementCommand)
    Service->>Service: Vérifier les invariants métier
    Service->>Correlation: generate()
    Correlation-->>Service: correlationId
    Service->>Service: Générer eventId et occurredAt
    Service->>Service: Construire MeasurementReceivedEvent
    Service->>Publisher: publish(event)
    Publisher->>Kafka: Publier dans measurements.received<br/>Clé Kafka = zoneId
    Kafka-->>Publisher: Accusé de prise en charge
    Publisher-->>Service: Publication demandée
    Service-->>Controller: IngestionResult(ACCEPTED, correlationId)
    Controller-->>Client: HTTP 202 Accepted<br/>status + correlationId
```

## Rejet d'une requête non authentifiée

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client non authentifié
    participant ApiKey as ApiKeyAuthenticationFilter
    participant EntryPoint as ApiKeyAuthenticationEntryPoint
    participant Controller as IngestionController

    Client->>ApiKey: POST /api/ingestion/measurements<br/>Sans clé ou avec une clé invalide
    ApiKey->>ApiKey: Comparaison sécurisée
    ApiKey->>EntryPoint: Authentification absente
    EntryPoint-->>Client: HTTP 401 Unauthorized<br/>Erreur JSON structurée
    Note over Controller: Le contrôleur n'est pas exécuté
```

## Rejet d'une requête dépassant le quota

```mermaid
sequenceDiagram
    autonumber
    actor Client as Passerelle authentifiée
    participant ApiKey as ApiKeyAuthenticationFilter
    participant RateLimit as RateLimitFilter
    participant Bucket as Bucket4j
    participant Controller as IngestionController

    Client->>ApiKey: Requête avec X-API-Key valide
    ApiKey->>RateLimit: Requête authentifiée
    RateLimit->>Bucket: tryConsumeAndReturnRemaining(1)
    Bucket-->>RateLimit: Aucun jeton disponible
    RateLimit-->>Client: HTTP 429 Too Many Requests<br/>Retry-After<br/>X-Rate-Limit-Remaining: 0
    Note over Controller: Le contrôleur et le service métier ne sont pas exécutés
```

## Rejet d'une requête invalide

```mermaid
sequenceDiagram
    autonumber
    actor Client as Passerelle authentifiée
    participant ApiKey as ApiKeyAuthenticationFilter
    participant RateLimit as RateLimitFilter
    participant Controller as IngestionController
    participant Validation as Bean Validation
    participant Handler as GlobalExceptionHandler
    participant Service as MeasurementIngestionService

    Client->>ApiKey: Requête authentifiée
    ApiKey->>RateLimit: Continuer
    RateLimit->>Controller: Jeton consommé
    Controller->>Validation: Valider MeasurementIngestionRequest
    Validation-->>Controller: Contraintes violées
    Controller->>Handler: MethodArgumentNotValidException
    Handler-->>Client: HTTP 400 Bad Request<br/>fieldErrors structurés
    Note over Service: Le service métier n'est pas appelé<br/>Aucun événement Kafka n'est publié
```

## Cas d'erreur JSON

```mermaid
sequenceDiagram
    autonumber
    actor Client as Passerelle authentifiée
    participant Controller as Couche HTTP Spring
    participant Handler as GlobalExceptionHandler

    Client->>Controller: Corps JSON absent ou malformé
    Controller->>Handler: HttpMessageNotReadableException
    Handler-->>Client: HTTP 400 Bad Request<br/>error = MALFORMED_JSON
```

## Données de corrélation

Deux identifiants assurent la traçabilité :

| Identifiant | Rôle |
|---|---|
| `eventId` | Identifie de manière unique l'événement publié |
| `correlationId` | Suit une même mesure dans l'ensemble de la chaîne UrbanHub |

Le `correlationId` est retourné au client dans la réponse HTTP 202 et inclus dans l'événement Kafka.

## Codes de réponse du flux

| Code HTTP | Situation | Publication Kafka |
|---:|---|---|
| `202` | Clé valide, quota disponible et mesure conforme | Oui |
| `400` | JSON invalide ou contraintes non respectées | Non |
| `401` | Clé API absente ou invalide | Non |
| `429` | Quota épuisé | Non |
| `500` | Erreur interne inattendue | Non garantie |

## Garanties du traitement

- une requête non authentifiée n'atteint pas le contrôleur ;
- une requête dépassant le quota n'atteint pas le service métier ;
- une mesure invalide ne produit aucun événement ;
- une mesure acceptée reçoit un identifiant de corrélation ;
- le contrat événementiel est explicite et versionné ;
- la clé Kafka `zoneId` favorise l'ordre relatif des événements par zone.
