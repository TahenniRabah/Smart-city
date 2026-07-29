# UrbanHub - Pipeline CI/CD du service d'ingestion

## 1\. Objet du livrable

Ce livrable présente l'industrialisation du microservice `ingestion-service` de la plateforme UrbanHub.

Le service reçoit des mesures IoT, valide les données entrantes, contrôle l'authentification et le débit des requêtes, puis publie un événement `MeasurementReceived` dans Apache Kafka.

## 2\. Technologies

* Java 25
* Spring Boot
* Maven
* JUnit 5
* Mockito
* MockMvc
* JaCoCo
* Checkstyle
* SpotBugs
* Find Security Bugs
* Gitleaks
* Trivy
* CycloneDX
* Docker
* Docker Compose
* Apache Kafka
* GitHub Actions

## 3\. Structure du pipeline

```text
install
→ test
→ quality
→ security
→ build
→ deploy
```

### Install

* installation de Java 25 ;
* restauration du cache Maven ;
* résolution hors ligne des dépendances avec dependency:go-offline.

### Test

* tests unitaires JUnit ;
* tests de validation REST ;
* tests d'authentification ;
* test du rate limiting ;
* test non fonctionnel du temps de réponse ;
* génération du rapport JaCoCo ;
* contrôle bloquant de la couverture.

### Quality

* Checkstyle en mode bloquant ;
* SpotBugs ;
* Find Security Bugs.

### Security

* Gitleaks avec blocage en cas de secret ;
* Trivy filesystem pour les dépendances ;
* génération d'un rapport SARIF ;
* génération d'une SBOM CycloneDX.

### Build

* construction d'une image Docker multi-stage ;
* runtime Java 25 ;
* exécution avec un utilisateur non-root ;
* tag immuable basé sur le SHA Git.

### Deploy

* lancement local avec Docker Compose ;
* démarrage de Kafka et du service d'ingestion ;
* contrôle de l'endpoint de readiness ;
* smoke test sans clé API (HTTP 401) ;
* smoke test avec clé API valide (HTTP 202) ;
* arrêt et nettoyage de l'environnement.

## 4\. Tests automatisés

La suite contient au moins 21 tests automatisés.

## 5\. Couverture

JaCoCo applique deux gates bloquantes :

* couverture des lignes ≥ 60 % ;
* couverture des branches ≥ 60 %.

## 6\. Qualité du code

Checkstyle vérifie notamment les imports, l'organisation du code, les blocs de contrôle, les accolades, les modificateurs et le formatage.

## 7\. Sécurité DevSecOps

### Gitleaks

Analyse l'historique Git et bloque le pipeline lorsqu'un secret est détecté.

### Trivy

Analyse les dépendances Maven et produit des rapports JSON et SARIF.

### SBOM

Une nomenclature logicielle CycloneDX est générée automatiquement.

## 8\. Sécurité applicative

* validation stricte avec Bean Validation ;
* authentification par clé API ;
* rate limiting avec HTTP 429 ;
* refus par défaut ;
* erreurs HTTP structurées ;
* conteneur non-root.

## 9\. Reproductibilité

### Prérequis

* Java 25
* Maven
* Docker
* Docker Compose

### Vérification Maven

```bash
cd services/ingestion-service
mvn clean verify
```

### Lancement local

```bash
docker compose up --build -d kafka ingestion-service
```

### Arrêt

```bash
docker compose down --volumes --remove-orphans
```

## 10\. Gates bloquantes

Le pipeline échoue en cas d'échec de compilation, tests, Checkstyle, couverture, Gitleaks, Docker, healthcheck ou smoke tests.

## 11\. Limites

* rate limiting en mémoire ;
* Kafka local ;
* SpotBugs non bloquant ;
* vulnérabilités Trivy en cours de qualification.

## 12\. Déclaration d'utilisation de l'intelligence artificielle

Outil utilisé : M365 Copilot fondé sur un modèle GPT-5 reasoning.

