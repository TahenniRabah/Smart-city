# Changelog

Toutes les modifications notables d'UrbanHub Ingestion Service sont documentées dans ce fichier.

Le format s'inspire de Keep a Changelog et les commits suivent Conventional Commits.

## [Unreleased]

### Added

- Documentation technique structurée selon Diátaxis.
- Portail MkDocs Material.
- Diagrammes Mermaid d'architecture, de classes et de séquence.
- Référence OpenAPI exportée.

### Changed

- Aucun changement fonctionnel non publié.

## [1.1.0] - 2026-07-29

### Added

- Authentification de l'API d'ingestion avec `X-API-Key`.
- Rate limiting Bucket4j avec HTTP 429 et `Retry-After`.
- Validation stricte des requêtes avec Bean Validation.
- Réponses d'erreur JSON standardisées.
- Test non fonctionnel du temps de réponse.
- Gates JaCoCo à 60 % pour les lignes et les branches.
- Checkstyle bloquant.
- SpotBugs et Find Security Bugs.
- Gitleaks bloquant sur l'historique Git.
- Trivy filesystem, rapports JSON et SARIF.
- SBOM CycloneDX.
- Pipeline EC03 complet : install, test, quality, security, build et deploy.
- Smoke tests de readiness, HTTP 401 et HTTP 202.

### Changed

- Image Docker multi-stage sous Java 25.
- Exécution du conteneur avec l'utilisateur non-root `urbanhub`.
- Interfaces Kafka administratives limitées à localhost.
- Configuration Checkstyle référencée par `${project.basedir}`.

### Fixed

- Copie de la configuration Checkstyle dans le stage de build Docker.
- Utilisation du cache Maven par Trivy dans GitHub Actions.
- Gestion d'un résultat vide dans la synthèse de sécurité.
- Qualification d'un faux positif Gitleaks provenant d'un rapport de test généré.

### Security

- Refus des requêtes non authentifiées.
- Limitation des requêtes excessives.
- Détection des secrets, vulnérabilités de dépendances et défauts SAST.
- Publication d'une nomenclature logicielle CycloneDX.

## [1.0.0] - 2026-05-06

### Added

- API REST d'ingestion des mesures IoT.
- Publication de `MeasurementReceived` dans Kafka.
- Identifiants `eventId` et `correlationId`.
- Contrats événementiels JSON versionnés.
- Tests unitaires et tests MockMvc.
- Docker Compose avec Kafka et Redpanda Console.
- Documentation initiale des contrats et de l'architecture.
