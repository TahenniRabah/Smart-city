# UrbanHub Smart City

UrbanHub est un projet Smart City basé sur une architecture microservices événementielle.

\---

## Objectifs

Le projet vise à démontrer :

* une architecture distribuée orientée événements ;
* plusieurs microservices Spring Boot ;
* un message bus Kafka ;
* des contrats API et événements ;
* des pratiques XP : TDD, small releases, CI, documentation ;
* des principes Clean Code : SOLID, DRY, KISS ;
* un exemple de design pattern State avec la classe `Sensor`.

\---

## Flux principal

```text
ingestion-service
→ measurements.received
→ quality-service
→ measurements.validated
→ air-quality-service
→ air-quality.alert.detected
→ alerting-service
```

\---

## Microservices

|Service|Port|Rôle|
|-|-:|-|
|`ingestion-service`|`8082`|Réception des mesures brutes IoT|
|`quality-service`|`8083`|Validation ou rejet des mesures|
|`air-quality-service`|`8080`|Analyse des seuils qualité de l’air|
|`alerting-service`|`8081`|Gestion des alertes et notifications|

\---

## Documentation disponible

* Contrats API et événements ;
* Stratégie de tests ;
* Clean Code ;
* Design Pattern State ;
* Documentation XP ;
* Communication technique vs fonctionnelle.



