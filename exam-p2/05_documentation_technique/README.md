# UrbanHub - Service d'ingestion IoT

## PrÃ©sentation

`ingestion-service` est le point d'entrÃ©e des mesures IoT de la plateforme UrbanHub.

Le service :

1. reÃ§oit une mesure environnementale par API REST ;
2. authentifie la passerelle avec une clÃ© API ;
3. applique un contrÃ´le de dÃ©bit ;
4. valide strictement les donnÃ©es ;
5. gÃ©nÃ¨re un identifiant d'Ã©vÃ©nement et un identifiant de corrÃ©lation ;
6. publie un Ã©vÃ©nement `MeasurementReceived` dans Apache Kafka.

## Organisation DiÃ¡taxis

### Tutoriel

Pour dÃ©couvrir le service et exÃ©cuter un premier flux :

- `docs/tutoriel.md`

### Guides pratiques

Pour dÃ©ployer, exploiter et dÃ©panner le service :

- `docs/exploitation.md`
- `docs/pipeline.md`
- `docs/depannage.md`
- `docs/maintenance-evolution.md`

### RÃ©fÃ©rence

Pour consulter les contrats et paramÃ¨tres :

- `docs/api.md`
- `docs/reference.md`
- `openapi.json`

### Explications

Pour comprendre l'architecture et les dÃ©cisions techniques :

- `docs/architecture.md`
- `docs/securite.md`
- `diagrammes/architecture.md`
- `diagrammes/classes.md`
- `diagrammes/sequence-ingestion.md`

## PrÃ©requis

| Outil | Version attendue |
|---|---|
| Java | 25 |
| Maven | 3.9 ou compatible |
| Docker | Version rÃ©cente |
| Docker Compose | Compose V2 |
| Git | Version rÃ©cente |

## Variables d'environnement

| Variable | Obligatoire | Valeur locale | Description |
|---|---:|---|---|
| `INGESTION_API_KEY` | Oui en production | Valeur locale non versionnÃ©e | ClÃ© d'authentification de l'API |
| `KAFKA_BOOTSTRAP_SERVERS` | Non | `localhost:9092` hors Docker | Adresse du broker Kafka |
| `SERVER_PORT` | Non | `8082` | Port HTTP du service |
| `OPENAPI_ENABLED` | Non | `true` en dÃ©monstration | Active la spÃ©cification OpenAPI |
| `SWAGGER_ENABLED` | Non | `true` en dÃ©monstration | Active Swagger UI |

Aucun secret rÃ©el ne doit Ãªtre commitÃ©. Le fichier `.env` reste ignorÃ© par Git. Seul `.env.example` peut Ãªtre versionnÃ© avec des valeurs fictives.

## Installation et vÃ©rification

Depuis la racine du dÃ©pÃ´t :

```bash
mvn   --file services/ingestion-service/pom.xml   clean verify
```

Cette commande exÃ©cute :

- Checkstyle ;
- les tests JUnit ;
- les tests de validation et de sÃ©curitÃ© ;
- le test non fonctionnel ;
- JaCoCo et les gates de couverture ;
- SpotBugs et Find Security Bugs ;
- le packaging du JAR.

## Lancement avec Docker Compose

DÃ©finir une clÃ© locale sous PowerShell :

```powershell
$env:INGESTION_API_KEY = "replace-with-a-local-key"
```

DÃ©marrer Kafka et le service :

```bash
docker compose up --build -d kafka ingestion-service
```

VÃ©rifier l'Ã©tat :

```bash
docker compose ps
curl http://localhost:8082/actuator/health/readiness
```

## PremiÃ¨re requÃªte

```bash
curl -X POST http://localhost:8082/api/ingestion/measurements   -H "Content-Type: application/json"   -H "X-API""-Key: ${INGESTION_API_KEY}"   -d '{
    "zoneId": "ZFE-1",
    "stationId": "AIR-STATION-042",
    "indicator": "NO2",
    "value": 220.5,
    "timestamp": "2026-07-27T08:00:00Z"
  }'
```

RÃ©ponse attendue :

```json
{
  "status": "ACCEPTED",
  "correlationId": "identifiant-gÃ©nÃ©rÃ©"
}
```

## API interactive

Lorsque Swagger est activÃ© :

```text
http://localhost:8082/swagger-ui.html
```

SpÃ©cification OpenAPI :

```text
http://localhost:8082/v3/api-docs
```

Une copie exportÃ©e est fournie dans `openapi.json`.

## Pipeline CI/CD

Le pipeline suit l'ordre bloquant suivant :

```text
install â†’ test â†’ quality â†’ security â†’ build â†’ deploy
```

### Install

- installation de Java 25 ;
- restauration du cache Maven ;
- rÃ©solution des dÃ©pendances avec `dependency:go-offline`.

### Test

- tests unitaires et tests REST ;
- tests de validation, d'authentification et de rate limiting ;
- test non fonctionnel du temps de rÃ©ponse ;
- rapport JaCoCo ;
- couverture des lignes et des branches supÃ©rieure ou Ã©gale Ã  60 %.

### Quality

- Checkstyle en mode bloquant ;
- SpotBugs et Find Security Bugs en mode audit pendant la qualification initiale.

### Security

- Gitleaks en mode bloquant ;
- Trivy filesystem pour l'analyse des dÃ©pendances ;
- rapports JSON et SARIF ;
- SBOM CycloneDX.

### Build

- image Docker multi-stage ;
- runtime Java 25 ;
- utilisateur non-root ;
- tag immuable fondÃ© sur le SHA Git.

### Deploy

- lancement local avec Docker Compose ;
- contrÃ´le de l'endpoint de readiness ;
- smoke test sans clÃ© API, rÃ©sultat attendu HTTP 401 ;
- smoke test avec clÃ© API valide, rÃ©sultat attendu HTTP 202 ;
- collecte des logs et nettoyage de l'environnement.

## RÃ©sultats validÃ©s

- pipeline complet vert de `install` Ã  `deploy` ;
- tous les tests JUnit rÃ©ussissent ;
- couverture des instructions d'environ 78 % ;
- couverture des branches d'environ 69 % ;
- Checkstyle bloquant ;
- aucun finding SpotBugs classÃ© `SECURITY` ;
- Gitleaks bloquant ;
- rapports Trivy et SBOM CycloneDX gÃ©nÃ©rÃ©s ;
- image Docker multi-stage et non-root ;
- smoke tests de readiness, HTTP 401 et HTTP 202 rÃ©ussis.

## Documentation collaborative

La documentation est Ã©crite en Markdown, versionnÃ©e avec Git et publiÃ©e avec MkDocs Material.

Toute modification fonctionnelle ou opÃ©rationnelle doit mettre Ã  jour :

1. le code ;
2. les tests ;
3. la documentation ;
4. la spÃ©cification OpenAPI si le contrat change ;
5. le changelog.

## DÃ©claration d'utilisation de l'intelligence artificielle

### ResponsabilitÃ© et principe d'utilisation

L'intelligence artificielle a Ã©tÃ© utilisÃ©e comme outil d'assistance Ã  l'analyse, Ã  la rÃ©daction, au dÃ©bogage et Ã  la structuration. Les rÃ©ponses produites n'ont pas Ã©tÃ© intÃ©grÃ©es automatiquement. Chaque proposition a Ã©tÃ© relue, comparÃ©e au code rÃ©el, adaptÃ©e Ã  l'environnement du projet, puis validÃ©e par compilation, tests automatisÃ©s, scans de sÃ©curitÃ© ou exÃ©cution Docker.

L'architecture, la sÃ©curitÃ©, la qualitÃ© du code et la dÃ©cision finale d'intÃ©gration restent sous la responsabilitÃ© de l'auteur du livrable.

### Outil utilisÃ©

| Ã‰lÃ©ment | Valeur |
|---|---|
| Outil | M365 Copilot |
| ModÃ¨le | ModÃ¨le fondÃ© sur GPT-5 reasoning |
| Plateforme | Interface Web |
| Modes d'utilisation | Analyse, gÃ©nÃ©ration assistÃ©e, revue, dÃ©bogage et documentation |

### PÃ©rimÃ¨tre d'utilisation

L'outil a Ã©tÃ© utilisÃ© pour :

- analyser les exigences des parties 1 et 2 de l'Ã©preuve ;
- proposer une organisation progressive du pipeline GitHub Actions ;
- assister le dÃ©bogage de Maven, Kafka, Docker, GitHub Actions, Gitleaks et Trivy ;
- proposer des tests de validation, d'authentification, de rate limiting et de temps de rÃ©ponse ;
- assister l'intÃ©gration de JaCoCo, Checkstyle, SpotBugs, Find Security Bugs, Gitleaks et Trivy ;
- structurer la documentation selon DiÃ¡taxis ;
- prÃ©parer les pages MkDocs, les diagrammes Mermaid, la rÃ©fÃ©rence API et le rapport client BLUF ;
- prÃ©parer le document PDF final Ã  partir des contenus Markdown validÃ©s.

L'outil n'a pas Ã©tÃ© autorisÃ© Ã  dÃ©cider seul des seuils de sÃ©curitÃ©, Ã  masquer automatiquement les findings ou Ã  modifier les secrets et environnements distants.

### DÃ©marche de Context Engineering

Les demandes fournissaient systÃ©matiquement le contexte technique utile, notamment :

- Java 25 et Spring Boot 3.5.16 ;
- la structure rÃ©elle du service et les classes concernÃ©es ;
- les fragments du `pom.xml`, du Dockerfile et du workflow ;
- les sorties exactes de Maven et GitHub Actions ;
- les erreurs observÃ©es, par exemple HTTP 429 de Maven Central ou fichier Checkstyle absent du contexte Docker ;
- les exigences du sujet d'examen et les critÃ¨res C16 Ã  C20 ;
- les rÃ©sultats rÃ©els des tests, de JaCoCo, de SpotBugs, de Gitleaks et de Trivy ;
- les contraintes d'anonymat, de chemins relatifs et de reproductibilitÃ©.

Cette dÃ©marche a limitÃ© les rÃ©ponses gÃ©nÃ©riques et permis de comparer chaque proposition Ã  l'Ã©tat rÃ©el du projet.

### Prompts majeurs et dÃ©cisions associÃ©es

#### 1. SÃ©curisation de l'API d'ingestion

**Demande formulÃ©e :** proposer une authentification par clÃ© API, une validation stricte des payloads et un rate limiting compatible avec le service Spring Boot existant.

**RÃ©ponse auditÃ©e :** la proposition a Ã©tÃ© validÃ©e aprÃ¨s vÃ©rification de la chaÃ®ne de filtres Spring Security, du fonctionnement stateless, des statuts HTTP 401 et 429, de l'absence d'appel mÃ©tier aprÃ¨s dÃ©passement de quota et de la non-publication Kafka pour une requÃªte invalide.

**DÃ©cision :** intÃ©gration de la clÃ© `X-API-Key`, de Bean Validation et de Bucket4j. Le choix d'une clÃ© partagÃ©e a Ã©tÃ© acceptÃ© uniquement pour le pÃ©rimÃ¨tre pÃ©dagogique. Une identitÃ© par passerelle, HMAC, OAuth2 client credentials ou mTLS reste recommandÃ©e pour la production.

#### 2. DÃ©couplage des Ã©vÃ©nements Kafka

**Demande formulÃ©e :** supprimer le couplage entre les noms de packages Java du producteur et les consommateurs Kafka.

**RÃ©ponse auditÃ©e :** une premiÃ¨re proposition de serializer a Ã©tÃ© invalidÃ©e car elle ne correspondait pas Ã  la version rÃ©elle de Spring Kafka. Le `pom.xml` et les propriÃ©tÃ©s du serializer ont Ã©tÃ© vÃ©rifiÃ©s avant adaptation.

**DÃ©cision :** conservation d'un contrat JSON explicite et versionnÃ©, sans information de type Java dans les headers. Cette solution a Ã©tÃ© retenue car elle rÃ©duit le couplage interservices et facilite l'Ã©volution indÃ©pendante des consommateurs.

#### 3. Gates de tests et de couverture

**Demande formulÃ©e :** ajouter une gate JaCoCo Ã  60 % et un test non fonctionnel de temps de rÃ©ponse.

**RÃ©ponse auditÃ©e :** les seuils proposÃ©s ont Ã©tÃ© comparÃ©s aux rÃ©sultats rÃ©els, soit environ 78 % de couverture des instructions et 69 % des branches. Le test de performance a Ã©tÃ© exÃ©cutÃ© avec une requÃªte de chauffe afin de ne pas mesurer l'initialisation paresseuse de Spring.

**DÃ©cision :** intÃ©gration des gates de lignes et de branches Ã  60 %. Le test mesure la couche HTTP mais pas la latence Kafka, limite explicitement documentÃ©e.

#### 4. Analyse de qualitÃ© Java

**Demande formulÃ©e :** intÃ©grer Checkstyle, SpotBugs et Find Security Bugs sans masquer les findings existants.

**RÃ©ponse auditÃ©e :** Checkstyle a d'abord Ã©tÃ© exÃ©cutÃ© localement en mode bloquant. SpotBugs a dÃ©tectÃ© un finding gÃ©nÃ©ral et aucun finding de catÃ©gorie `SECURITY`.

**DÃ©cision :** Checkstyle reste bloquant. SpotBugs reste temporairement en audit tant que le finding gÃ©nÃ©ral n'est pas complÃ¨tement qualifiÃ©. Aucune suppression globale n'a Ã©tÃ© ajoutÃ©e.

#### 5. Gestion du finding Gitleaks

**Demande formulÃ©e :** traiter une dÃ©tection `generic-api-key` dans un rapport XML de test commitÃ© dans l'historique.

**RÃ©ponse auditÃ©e :** le chemin, le commit, la rÃ¨gle et le contenu ont Ã©tÃ© examinÃ©s. La valeur Ã©tait fictive et limitÃ©e aux tests, mais le rapport gÃ©nÃ©rÃ© n'avait pas Ã  Ãªtre versionnÃ©.

**DÃ©cision :** retrait du rapport gÃ©nÃ©rÃ©, ajout au `.gitignore`, documentation du faux positif et exclusion limitÃ©e au fingerprint historique exact. La solution consistant Ã  dÃ©sactiver Gitleaks ou Ã  utiliser `continue-on-error` a Ã©tÃ© rejetÃ©e, car elle aurait supprimÃ© la gate de sÃ©curitÃ©.

#### 6. Incident Trivy et Maven Central

**Demande formulÃ©e :** corriger l'Ã©chec Trivy causÃ© par HTTP 429 sur Maven Central.

**RÃ©ponse auditÃ©e :** l'erreur indiquait explicitement un rate limiting externe et recommandait de prÃ©remplir le cache Maven.

**DÃ©cision :** restauration du cache avec `actions/setup-java`, exÃ©cution de `dependency:go-offline`, puis montage de `~/.m2` en lecture seule dans le conteneur Trivy. Un retry long a Ã©tÃ© rejetÃ©, car le serveur imposait un dÃ©lai de 1 800 secondes incompatible avec l'objectif de feedback rapide.

#### 7. Build Docker

**Demande formulÃ©e :** corriger le build de l'image Java 25 et exÃ©cuter le service avec un utilisateur non-root.

**RÃ©ponse auditÃ©e :** une proposition initiale n'a pas rÃ©solu le build. Les logs dÃ©taillÃ©s ont ensuite montrÃ© que Checkstyle ne trouvait pas `config/checkstyle/checkstyle.xml` dans le contexte Docker.

**DÃ©cision :** utilisation de `${project.basedir}` dans Maven, copie explicite de la configuration Checkstyle, validation de sa prÃ©sence pendant le build, runtime Java 25 Alpine, utilisateur `urbanhub` et port 8082. Le correctif a Ã©tÃ© validÃ© par le job `build` puis par le dÃ©ploiement et les smoke tests.

#### 8. Documentation Doc as Code

**Demande formulÃ©e :** produire une documentation Partie 2 conforme Ã  C20, structurÃ©e selon DiÃ¡taxis et destinÃ©e Ã  MkDocs Material.

**RÃ©ponse auditÃ©e :** chaque page a Ã©tÃ© rapprochÃ©e d'un objectif lecteur : tutoriel pour apprendre, guides pour agir, rÃ©fÃ©rence pour consulter et explications pour comprendre. Les commandes ont Ã©tÃ© formulÃ©es avec des chemins relatifs et les limites connues ont Ã©tÃ© conservÃ©es.

**DÃ©cision :** gÃ©nÃ©ration d'un README, de pages MkDocs, de diagrammes Mermaid, d'une rÃ©fÃ©rence API, d'un changelog, d'un rapport client BLUF et d'un PDF consolidÃ©. La documentation ne prÃ©sente pas l'application comme prÃªte pour la production et indique les amÃ©liorations nÃ©cessaires.

### ContrÃ´les rÃ©alisÃ©s avant validation

Les vÃ©rifications suivantes ont Ã©tÃ© exÃ©cutÃ©es :

- `mvn clean verify` sous Java 25 ;
- tests JUnit et MockMvc sans Ã©chec ;
- gates JaCoCo satisfaites ;
- Checkstyle sans violation ;
- SpotBugs et Find Security Bugs exÃ©cutÃ©s ;
- Gitleaks exÃ©cutÃ© sur l'historique complet ;
- Trivy et SBOM CycloneDX gÃ©nÃ©rÃ©s ;
- image Docker multi-stage construite ;
- utilisateur de runtime non-root vÃ©rifiÃ© ;
- readiness disponible ;
- requÃªte sans clÃ© rejetÃ©e avec HTTP 401 ;
- requÃªte authentifiÃ©e acceptÃ©e avec HTTP 202 ;
- `mkdocs build --strict` exÃ©cutÃ© avec succÃ¨s.

### Propositions invalidÃ©es ou adaptÃ©es

Les propositions suivantes n'ont pas Ã©tÃ© intÃ©grÃ©es telles quelles :

- serializer Kafka incompatible avec la version du projet ;
- dÃ©sactivation globale de Gitleaks, rejetÃ©e au profit d'une exclusion exacte et documentÃ©e ;
- simple retry de 30 minutes pour Trivy, remplacÃ© par le cache Maven ;
- Dockerfile ne copiant pas la configuration Checkstyle ;
- comptage des vulnÃ©rabilitÃ©s avec `grep`, fragile avec zÃ©ro rÃ©sultat, remplacÃ© par `jq` ;
- toute formulation laissant entendre que zÃ©ro finding SAST garantit l'absence de vulnÃ©rabilitÃ©.

### Limites de l'assistance IA

- une rÃ©ponse techniquement plausible peut Ãªtre incompatible avec une version de dÃ©pendance ;
- une commande peut Ãªtre valide sur Linux mais incorrecte sous PowerShell ou Git Bash ;
- les outils de sÃ©curitÃ© peuvent produire des faux positifs ou ne pas couvrir toutes les vulnÃ©rabilitÃ©s ;
- les choix de production, notamment les secrets, Kafka et l'observabilitÃ©, exigent une analyse humaine et contextuelle.

### ResponsabilitÃ© finale

Les dÃ©cisions ont Ã©tÃ© retenues uniquement aprÃ¨s vÃ©rification dans l'environnement rÃ©el. L'auteur assume la comprÃ©hension du fonctionnement, des limites, des risques rÃ©siduels et des Ã©volutions recommandÃ©es. Toute nouvelle modification devra conserver la mÃªme dÃ©marche : contexte prÃ©cis, revue critique, test reproductible et documentation du pourquoi.

