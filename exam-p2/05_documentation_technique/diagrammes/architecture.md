# Diagramme d'architecture UrbanHub

## Architecture événementielle

Le diagramme suivant présente la place de `ingestion-service` dans la chaîne UrbanHub.

```mermaid
flowchart LR
    subgraph External[Zone externe]
        Sensor[Capteur ou passerelle IoT]
    end

    subgraph Platform[Plateforme UrbanHub]
        Ingestion[ingestion-service]
        Kafka[(Apache Kafka)]
        Quality[quality-service]
        Air[air-quality-service]
        Alert[alerting-service]
    end

    subgraph Administration[Administration locale]
        Console[Redpanda Console]
    end

    Sensor -->|POST /api/ingestion/measurements
JSON et X-API-Key| Ingestion
    Ingestion -->|MeasurementReceived
Topic measurements.received| Kafka
    Kafka -->|Consommation| Quality
    Quality -->|MeasurementValidated
ou MeasurementRejected| Kafka
    Kafka -->|MeasurementValidated| Air
    Air -->|AirQualityAlertDetected| Kafka
    Kafka -->|AirQualityAlertDetected| Alert
    Console <-->|Consultation des topics| Kafka
```

## Architecture interne d'ingestion-service

```mermaid
flowchart LR
    Client[Passerelle IoT]

    subgraph Security[Sécurité]
        ApiKey[ApiKeyAuthenticationFilter]
        RateLimit[RateLimitFilter]
        SecurityConfig[SecurityConfig]
    end

    subgraph API[Couche API]
        Controller[IngestionController]
        Request[MeasurementIngestionRequest]
        ErrorHandler[GlobalExceptionHandler]
    end

    subgraph Application[Couche application]
        Service[MeasurementIngestionService]
        Command[RawMeasurementCommand]
        PublisherPort[MeasurementReceivedPublisher]
        CorrelationPort[CorrelationIdGenerator]
    end

    subgraph Adapter[Adaptateurs]
        KafkaPublisher[KafkaMeasurementReceivedPublisher]
        CorrelationAdapter[UUID CorrelationIdGenerator]
    end

    subgraph Infrastructure[Infrastructure]
        KafkaTemplate[KafkaTemplate]
        Broker[(Apache Kafka)]
    end

    Client --> ApiKey
    ApiKey --> RateLimit
    RateLimit --> Controller
    SecurityConfig -. configure .-> ApiKey
    SecurityConfig -. configure .-> RateLimit

    Controller --> Request
    Controller --> Command
    Controller --> Service
    ErrorHandler -. traite les erreurs .-> Controller

    Service --> CorrelationPort
    Service --> PublisherPort
    CorrelationPort --> CorrelationAdapter
    PublisherPort --> KafkaPublisher
    KafkaPublisher --> KafkaTemplate
    KafkaTemplate --> Broker
```

## Frontières de confiance

```mermaid
flowchart TB
    subgraph Untrusted[Frontière non fiable]
        Device[Passerelle ou client HTTP]
    end

    subgraph APIBoundary[Frontière API contrôlée]
        Authentication[Authentification par clé API]
        Limiting[Rate limiting]
        Validation[Bean Validation]
    end

    subgraph Trusted[Zone applicative]
        UseCase[Cas d'utilisation d'ingestion]
        Event[Construction de l'événement]
    end

    subgraph Messaging[Frontière événementielle]
        Kafka[(Kafka)]
    end

    Device --> Authentication
    Authentication --> Limiting
    Limiting --> Validation
    Validation --> UseCase
    UseCase --> Event
    Event --> Kafka
```

## Légende

| Élément | Signification |
|---|---|
| Rectangle | Processus ou composant applicatif |
| Cylindre | Infrastructure de stockage ou bus d'événements |
| Sous-graphe | Couche, zone ou frontière de confiance |
| Flèche pleine | Flux de données ou appel |
| Flèche pointillée | Relation de configuration ou de traitement transversal |

## Principes représentés

- le client est considéré comme non fiable avant authentification et validation ;
- les filtres de sécurité s'exécutent avant le contrôleur ;
- le contrôleur traduit HTTP vers une commande applicative ;
- le service applicatif dépend de ports et non de Kafka directement ;
- l'adaptateur Kafka implémente le port de publication ;
- les microservices en aval consomment des contrats JSON versionnés ;
- Redpanda Console reste un outil d'administration limité à l'environnement local.
