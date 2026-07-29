# Maintenance et évolution

## Objectif

Ce guide explique comment reprendre, maintenir et faire évoluer le microservice `ingestion-service` sans dégrader son contrat API, ses garanties de sécurité ou son pipeline CI/CD.

## Périmètre du service

`ingestion-service` est responsable de :

1. recevoir les mesures IoT par API REST ;
2. authentifier les passerelles avec une clé API ;
3. limiter la fréquence des requêtes ;
4. valider les données entrantes ;
5. générer les identifiants techniques ;
6. publier un événement `MeasurementReceived` dans Apache Kafka.

Le service ne calcule pas la qualité de l'air et ne prépare pas les notifications. Ces responsabilités appartiennent aux microservices situés en aval.

## Reprendre le projet

### 1. Vérifier les prérequis

```bash
java --version
mvn --version
docker --version
docker compose version
```

Versions de référence :

- Java 25 ;
- Maven 3.9 ou version compatible ;
- Docker avec Compose V2.

### 2. Installer les dépendances

Depuis la racine du dépôt :

```bash
mvn   --file services/ingestion-service/pom.xml   --batch-mode   --no-transfer-progress   dependency:go-offline
```

### 3. Exécuter les contrôles locaux

```bash
mvn   --file services/ingestion-service/pom.xml   clean verify
```

La commande doit valider :

- Checkstyle ;
- les tests JUnit ;
- le test non fonctionnel ;
- la couverture JaCoCo ;
- les seuils de couverture ;
- SpotBugs et Find Security Bugs ;
- le packaging du JAR.

### 4. Construire l'image Docker

```bash
docker build   --tag urbanhub-ingestion:local   services/ingestion-service
```

### 5. Démarrer l'environnement local

Définir une clé API sans la versionner :

```powershell
$env:INGESTION_API_KEY = "replace-with-a-local-key"
```

Puis :

```bash
docker compose up --build -d kafka ingestion-service
```

## Organisation du code

```text
src/main/java/com/urbanhub/ingestion/
├── api/            Contrôleurs, DTO REST et gestion des erreurs
├── application/    Cas d'utilisation, commandes et ports
├── config/         Configuration Kafka et OpenAPI
├── domain/         Modèle métier indépendant de l'infrastructure
├── events/         Contrats événementiels publiés
├── messaging/      Adaptateurs Kafka
└── security/       Authentification et rate limiting
```

### Règles de dépendance

- la couche `domain` ne dépend pas de Spring ou de Kafka ;
- la couche `application` dépend de ports, pas directement de `KafkaTemplate` ;
- la couche `api` traduit HTTP vers les commandes applicatives ;
- la couche `messaging` implémente les ports de publication ;
- les données invalides sont rejetées avant toute publication Kafka.

## Faire évoluer l'API REST

Pour ajouter ou modifier un champ :

1. modifier `MeasurementIngestionRequest` ;
2. ajouter les contraintes Bean Validation nécessaires ;
3. adapter `RawMeasurementCommand` ;
4. adapter le mapping du contrôleur ;
5. ajouter ou mettre à jour les tests MockMvc ;
6. vérifier les réponses d'erreur ;
7. régénérer `openapi.json` ;
8. mettre à jour `docs/api.md` et `CHANGELOG.md`.

### Compatibilité

Une suppression, un renommage ou un changement de type constitue potentiellement une rupture de contrat. Dans ce cas :

- introduire une nouvelle version d'API ;
- documenter la période de transition ;
- conserver temporairement l'ancien contrat si nécessaire ;
- ajouter des tests de compatibilité.

## Faire évoluer les événements Kafka

L'événement `MeasurementReceived` contient un champ `eventVersion`.

Pour faire évoluer le contrat :

1. privilégier l'ajout de champs optionnels ;
2. éviter de renommer ou supprimer directement un champ ;
3. incrémenter `eventVersion` pour une évolution incompatible ;
4. adapter les consommateurs avant le producteur lorsque cela est nécessaire ;
5. conserver `eventId` et `correlationId` ;
6. mettre à jour la documentation du contrat ;
7. tester la désérialisation côté consommateur.

Les noms complets des classes Java ne doivent pas devenir des contrats interservices. Les producteurs publient du JSON sans dépendance aux packages internes.

## Faire évoluer la sécurité

### Clé API

La clé API actuelle est adaptée au périmètre pédagogique et à une instance locale. Pour une production multi-capteurs :

- attribuer une identité distincte à chaque passerelle ;
- stocker les secrets dans un gestionnaire dédié ;
- prévoir la rotation et la révocation ;
- envisager une signature HMAC, OAuth2 client credentials ou mTLS.

### Rate limiting

Le rate limiting est stocké en mémoire. Pour plusieurs réplicas :

- externaliser les buckets dans Redis ou un stockage partagé ;
- ou centraliser la limitation dans une API Gateway ;
- conserver les réponses HTTP 429 et le header `Retry-After`.

### Kafka

Évolutions recommandées :

- activer TLS ;
- utiliser SASL pour authentifier les services ;
- définir des ACL par topic ;
- limiter les droits de chaque microservice au strict nécessaire.

## Faire évoluer les tests

Toute évolution fonctionnelle doit inclure :

- un test nominal ;
- au moins un test d'erreur ;
- un test de non-régression lorsque la modification corrige un défaut ;
- une mise à jour des tests de contrat si l'API ou un événement change.

Les seuils JaCoCo sont :

```text
Couverture des lignes >= 60 %
Couverture des branches >= 60 %
```

Une baisse sous ces seuils bloque le build.

## Maintenir les dépendances

Procédure recommandée :

1. consulter régulièrement les alertes de dépendances ;
2. lancer Trivy sur le service et sur l'image finale ;
3. identifier la dépendance directe ou transitive ;
4. vérifier la version corrigée ;
5. mettre à jour une dépendance à la fois ;
6. exécuter `mvn clean verify` ;
7. reconstruire et rescanner l'image ;
8. mettre à jour la SBOM CycloneDX ;
9. documenter la modification dans le changelog.

Une vulnérabilité ne doit pas être masquée sans justification, propriétaire et date de révision.

## Maintenir le pipeline CI/CD

Le pipeline suit l'ordre :

```text
install → test → quality → security → build → deploy
```

Lors d'une évolution du pipeline :

- conserver les dépendances entre jobs ;
- maintenir les permissions au minimum ;
- ne pas ajouter `continue-on-error` aux gates bloquantes ;
- pinner les versions des outils ;
- vérifier que les artefacts restent produits ;
- tester un run complet avant fusion ;
- mettre à jour `docs/pipeline.md`.

## Journal des versions

Chaque évolution significative doit être ajoutée à `CHANGELOG.md` dans une catégorie adaptée :

```text
Added
Changed
Fixed
Security
Deprecated
Removed
```

Les commits suivent Conventional Commits, par exemple :

```text
feat(security): add ingestion rate limiting
fix(ci): provide Maven cache to Trivy
test: add ingestion response time test
docs: document pipeline troubleshooting
```

## Définition de terminé

Une évolution est terminée lorsque :

- le code respecte les responsabilités architecturales ;
- les tests passent ;
- Checkstyle passe ;
- les gates JaCoCo passent ;
- les analyses de sécurité sont exécutées ;
- l'image Docker est construite ;
- les smoke tests passent ;
- OpenAPI est à jour si le contrat change ;
- la documentation et le changelog sont à jour.

## Dette technique et feuille de route

### Priorité élevée

- qualifier et corriger les vulnérabilités Trivy ;
- terminer la qualification du finding SpotBugs ;
- mettre en place TLS et des ACL Kafka ;
- remplacer la clé partagée par des identités distinctes.

### Priorité moyenne

- externaliser le rate limiting ;
- ajouter une DLQ et une stratégie de retry ;
- centraliser les logs et les métriques ;
- automatiser la mise à jour des dépendances.

### Priorité basse

- versionner plusieurs versions du portail avec `mike` ;
- publier automatiquement les release notes ;
- ajouter des tests de charge distribués.
