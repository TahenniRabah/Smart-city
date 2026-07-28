# Périmètre du Threat Modeling UrbanHub

## 1. Objectif

Cette analyse identifie les menaces pesant sur la chaîne événementielle UrbanHub, depuis la réception d’une mesure IoT jusqu’à la préparation d’une notification d’alerte.

La méthode utilisée est STRIDE :

- Spoofing
- Tampering
- Repudiation
- Information Disclosure
- Denial of Service
- Elevation of Privilege

## 2. Périmètre étudié

Le périmètre comprend les composants actuellement implémentés :

- client IoT simulé ou Postman
- `ingestion-service`
- Apache Kafka
- `quality-service`
- `air-quality-service`
- `alerting-service`
- Redpanda Console
- Docker Compose
- GitHub Actions
- GitHub Container Registry (prévu pour les images)
- Swagger / OpenAPI
- endpoints Spring Boot Actuator

## 3. Flux principal

```text
Client IoT simulé
→ POST /api/ingestion/measurements
→ ingestion-service
→ Kafka: measurements.received
→ quality-service
→ Kafka: measurements.validated ou measurements.rejected
→ air-quality-service
→ Kafka: air-quality.alert.detected
→ alerting-service
→ notification CSU simulée
```

## 4. Éléments hors périmètre actuel

Les composants suivants ne sont pas encore implémentés et ne seront pas présentés comme opérationnels :

- authentification des utilisateurs
- dashboard Web métier
- stockage PostgreSQL ou TimescaleDB
- envoi réel de SMS, d’e-mails ou de notifications push
- infrastructure Kubernetes
- service mesh et mTLS
- environnement cloud de production

Ces composants pourront être intégrés à une version ultérieure du threat model.

## 5. Hypothèses

- la plateforme est déployée localement avec Docker Compose ;
- Kafka est accessible uniquement au sein du réseau Docker et via un port local de développement ;
- les mesures sont échangées sous forme d’événements JSON versionnés ;
- les API Swagger sont activées pour la démonstration ;
- Redpanda Console est un outil d’administration réservé aux personnes autorisées ;
- aucun secret de production ne doit être stocké dans le dépôt Git ;
- les données de test sont synthétiques.

## 6. Données et actifs à protéger

### Données métier

- mesures de pollution ;
- identifiants des zones ;
- identifiants des stations ;
- seuils d’alerte ;
- niveaux NORMAL, WARNING et CRITICAL ;
- événements de notification.

### Données techniques

- eventId ;
- correlationId ;
- offsets Kafka ;
- consumer groups ;
- traces applicatives ;
- contrats OpenAPI ;
- SBOM et rapports de sécurité.

### Actifs critiques

- intégrité des mesures ;
- disponibilité du broker Kafka ;
- disponibilité de l’ingestion ;
- exactitude des niveaux d’alerte ;
- non-duplication des notifications ;
- intégrité des images Docker ;
- intégrité du pipeline GitHub Actions.

---

## 7. Préparer le DFD niveau 1

Le cours demande de représenter les sources externes, processus, stockages et frontières de confiance.

La référence UrbanHub du cours place les capteurs en amont, le broker comme point central, puis l’ingestion, le stockage, les alertes et le dashboard.

Le DFD doit adapter cette référence aux services réellement présents dans l’architecture actuelle.
