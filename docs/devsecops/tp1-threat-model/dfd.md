# Data Flow Diagram niveau 1

## 1. Légende

```text
[Entité externe]       Source externe au système
(Processus)            Microservice ou composant applicatif
[(Stockage)]           Stockage persistant ou message bus
--- frontière ---      Changement de niveau de confiance
→                      Flux de données
```

```text
                            FRONTIÈRE INTERNET / POSTE CLIENT
┌─────────────────────────────────────────────────────────────────┐
│ [Capteur IoT simulé / Postman]                                  │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               │ JSON via HTTP
                               │ POST /api/ingestion/measurements
                               ▼
---------------------- Frontière API exposée -----------------------

┌─────────────────────────────────────────────────────────────────┐
│ (ingestion-service)                                             │
│ - valide la requête minimale                                    │
│ - génère eventId et correlationId                               │
│ - publie MeasurementReceived                                    │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               │ MeasurementReceived
                               ▼
------------------- Frontière réseau événementiel -----------------

┌─────────────────────────────────────────────────────────────────┐
│ [(Apache Kafka)]                                                │
│ Topic: measurements.received                                    │
│ Topic: measurements.validated                                   │
│ Topic: measurements.rejected                                    │
│ Topic: air-quality.alert.detected                               │
└──────────────┬──────────────────────┬───────────────────────────┘
               │                      │
               ▼                      ▼
┌──────────────────────────┐  ┌───────────────────────────────────┐
│ (quality-service)        │  │ [Redpanda Console / opérateur]    │
│ - valide la mesure       │  │ - consulte topics et messages     │
│ - publie Validated       │  │ - peut produire des messages      │
│   ou Rejected            │  │ - consulte consumer groups        │
└──────────────┬───────────┘  └───────────────────────────────────┘
               │
               │ MeasurementValidated
               ▼
┌─────────────────────────────────────────────────────────────────┐
│ (air-quality-service)                                           │
│ - calcule le niveau d’alerte                                    │
│ - publie AirQualityAlertDetected                                │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               │ AirQualityAlertDetected
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│ (alerting-service)                                              │
│ - contrôle l’idempotence par eventId                            │
│ - prépare une notification CSU si CRITICAL                      │
└─────────────────────────────────────────────────────────────────┘

---------------- Frontière développement / supply chain ------------

┌─────────────────────────┐
│ [Développeur]           │
└────────────┬────────────┘
             │ commit / push
             ▼
┌─────────────────────────────────────────────────────────────────┐
│ (GitHub Actions)                                                │
│ - build                                                         │
│ - tests                                                         │
│ - futurs scans de sécurité                                      │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               │ images et artefacts
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│ [(GitHub Container Registry prévu)]                             │
└─────────────────────────────────────────────────────────────────┘
```

## 3. Processus

- **P1 – ingestion-service** : Recevoir et transformer une mesure en événement
- **P2 – quality-service** : Valider ou rejeter une mesure
- **P3 – air-quality-service** : Calculer le niveau d’alerte
- **P4 – alerting-service** : Dédupliquer et préparer les notifications
- **P5 – GitHub Actions** : Compiler, tester, scanner et publier
- **P6 – Redpanda Console** : Administrer et observer Kafka

## 4. Stockages et bus

- **D1 – Kafka** : Mesures reçues, validées, rejetées et alertes
- **D2 – Git** : Code, workflows, contrats et documentation
- **D3 – GHCR (prévu)** : Images Docker UrbanHub
- **D4 – Mémoire alerting-service** : eventId déjà traités

## 5. Entités externes

- **E1 – Capteur IoT simulé / Postman** : Envoie une mesure brute
- **E2 – Développeur** : Modifie le code et les workflows
- **E3 – Opérateur** : Utilise Redpanda Console
- **E4 – Agent municipal simulé** : Destinataire logique de la notification CSU

## 6. Frontières de confiance

### TB1 - Client vers API d’ingestion
Le client est considéré comme non fiable. Toute donnée entrante doit être validée.

### TB2 - API vers Kafka
Le producteur doit être authentifié et autorisé à publier uniquement dans ses topics.

### TB3 - Kafka vers consommateurs
Les événements reçus sont non fiables jusqu’à validation du contrat et de la version.

### TB4 - Opérateur vers Redpanda Console
L’interface d’administration permet de lire et produire des messages. Elle nécessite un contrôle d’accès fort.

### TB5 - Développeur vers pipeline CI
Une modification de code ou de workflow peut affecter les images produites et la supply chain.

### TB6 - Pipeline vers registre
Les images doivent être scannées, identifiées par un tag immuable et signées avant publication.

## Important

Pour éviter d’inventer une base de données, Kafka et la mémoire du service d’alerting sont les seuls stockages effectivement représentés. Le stockage time-series prévu par l’architecture initiale est mentionné comme évolution future.
