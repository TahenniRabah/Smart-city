# Diagramme de classes du service d'ingestion

## Vue principale

Le diagramme présente les classes centrales du flux d'ingestion, de la requête HTTP jusqu'à la publication Kafka.

```mermaid
classDiagram
    direction LR

    class IngestionController {
        -MeasurementIngestionService ingestionService
        +ingest(MeasurementIngestionRequest) MeasurementIngestionResponse
    }

    class MeasurementIngestionRequest {
        +String zoneId
        +String stationId
        +String indicator
        +Double value
        +Instant timestamp
    }

    class MeasurementIngestionResponse {
        +String status
        +String correlationId
    }

    class RawMeasurementCommand {
        +String zoneId
        +String stationId
        +String indicator
        +double value
        +Instant timestamp
    }

    class MeasurementIngestionService {
        -MeasurementReceivedPublisher publisher
        -CorrelationIdGenerator correlationIdGenerator
        +ingest(RawMeasurementCommand) IngestionResult
        -validate(RawMeasurementCommand) void
    }

    class IngestionResult {
        +String status
        +String correlationId
    }

    class MeasurementReceivedPublisher {
        <<interface>>
        +publish(MeasurementReceivedEvent) void
    }

    class CorrelationIdGenerator {
        <<interface>>
        +generate() String
    }

    class KafkaMeasurementReceivedPublisher {
        -KafkaTemplate kafkaTemplate
        -String topicName
        +publish(MeasurementReceivedEvent) void
    }

    class MeasurementReceivedEvent {
        +String eventId
        +String eventType
        +String eventVersion
        +String correlationId
        +Instant occurredAt
        +String source
        +String zoneId
        +String stationId
        +String indicator
        +double value
        +Instant timestamp
    }

    class ApiKeyAuthenticationFilter {
        -byte[] expectedApiKey
        +doFilterInternal(request, response, chain) void
        -isValidApiKey(String) boolean
    }

    class RateLimitFilter {
        -Map~String, Bucket~ buckets
        -long capacity
        -long refillTokens
        -Duration refillDuration
        +doFilterInternal(request, response, chain) void
        -createBucket() Bucket
    }

    class GlobalExceptionHandler {
        +handleValidationException(exception, request) ResponseEntity
        +handleUnreadableMessage(exception, request) ResponseEntity
        +handleIllegalArgument(exception, request) ResponseEntity
    }

    IngestionController --> MeasurementIngestionService : délègue
    IngestionController ..> MeasurementIngestionRequest : reçoit
    IngestionController ..> RawMeasurementCommand : construit
    IngestionController ..> MeasurementIngestionResponse : retourne

    MeasurementIngestionService --> MeasurementReceivedPublisher : dépend du port
    MeasurementIngestionService --> CorrelationIdGenerator : dépend du port
    MeasurementIngestionService ..> MeasurementReceivedEvent : construit
    MeasurementIngestionService ..> IngestionResult : retourne

    KafkaMeasurementReceivedPublisher ..|> MeasurementReceivedPublisher : implémente

    ApiKeyAuthenticationFilter ..> IngestionController : protège
    RateLimitFilter ..> IngestionController : limite
    GlobalExceptionHandler ..> IngestionController : traite les erreurs
```

## Responsabilités

### `IngestionController`

Le contrôleur constitue l'adaptateur HTTP. Le contrôleur reçoit le DTO REST, construit une commande applicative et retourne une réponse HTTP.

### `MeasurementIngestionRequest`

Le record porte les données externes et les contraintes Bean Validation. Le record protège la frontière HTTP contre les champs absents, les formats non autorisés et les valeurs hors limites.

### `MeasurementIngestionService`

Le service orchestre le cas d'utilisation. Le service vérifie les invariants, génère les identifiants, construit l'événement et demande sa publication.

### `MeasurementReceivedPublisher`

L'interface constitue le port de sortie de la couche application. La couche application ne dépend pas directement de Kafka.

### `KafkaMeasurementReceivedPublisher`

L'adaptateur implémente le port de sortie avec `KafkaTemplate`. L'adaptateur utilise la zone comme clé Kafka et publie dans le topic configuré.

### `MeasurementReceivedEvent`

Le record représente le contrat événementiel JSON. Le contrat contient une version explicite et un identifiant de corrélation.

### Filtres de sécurité

`ApiKeyAuthenticationFilter` authentifie la passerelle. `RateLimitFilter` contrôle le nombre de requêtes par identité authentifiée.

### `GlobalExceptionHandler`

Le gestionnaire transforme les exceptions attendues en réponses d'erreur homogènes, sans exposer de détails techniques internes.

## Principes de conception

- responsabilité unique par composant ;
- dépendance de la couche application vers des interfaces ;
- séparation entre contrats REST et contrats Kafka ;
- validation à la frontière et validation métier défensive ;
- sécurité transversale avant l'exécution du contrôleur ;
- objets immuables représentés par des records Java.
