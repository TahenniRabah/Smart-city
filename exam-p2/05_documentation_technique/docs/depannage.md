# Dépannage

## Le service ne démarre pas

### Message lié à `INGESTION_API_KEY`

Cause : la clé obligatoire n'est pas définie.

```powershell
$env:INGESTION_API_KEY = "replace-with-a-local-key"
docker compose up -d --force-recreate ingestion-service
```

## HTTP 401 Unauthorized

Causes possibles :

- header `X-API-Key` absent ;
- clé incorrecte ;
- conteneur créé avec une ancienne valeur.

```powershell
docker compose config
docker compose logs ingestion-service
docker compose up -d --force-recreate ingestion-service
```

## HTTP 400 Bad Request

Vérifier :

```text
zoneId non vide
stationId non vide
indicator parmi NO2, PM10, PM25
value entre 0 et 5000
timestamp non futur
Content-Type application/json
```

## HTTP 429 Too Many Requests

Le quota est épuisé.

- attendre le délai `Retry-After` ;
- vérifier l'absence de boucle côté client ;
- ajuster la configuration uniquement après validation du besoin.

## Kafka indisponible

Symptômes possibles :

```text
Bootstrap broker disconnected
Connection to node could not be established
```

Diagnostic :

```powershell
docker compose ps
docker compose logs kafka
docker compose logs ingestion-service
```

Résolution :

```powershell
docker compose restart kafka
docker compose restart ingestion-service
```

Dans Docker, l'adresse attendue est généralement `kafka:29092` selon la configuration Compose.

## Healthcheck unhealthy

```powershell
docker inspect ingestion-service `
  --format "{{json .State.Health}}"

docker compose logs ingestion-service
```

Causes possibles :

- démarrage incomplet ;
- port incorrect ;
- endpoint Actuator non exposé ;
- erreur de configuration ;
- dépendance Kafka indisponible.

## Checkstyle ne trouve pas sa configuration

Message :

```text
Unable to find configuration file: config/checkstyle/checkstyle.xml
```

Le fichier attendu est :

```text
services/ingestion-service/config/checkstyle/checkstyle.xml
```

Le `pom.xml` doit utiliser :

```text
${project.basedir}/config/checkstyle/checkstyle.xml
```

Le Dockerfile doit copier explicitement le fichier avant le build Maven.

## Échec de la couverture JaCoCo

Message possible :

```text
Coverage checks have not been met
```

Procédure :

1. ouvrir `target/site/jacoco/index.html` ;
2. identifier les classes et branches non couvertes ;
3. ajouter des tests utiles ;
4. ne pas réduire le seuil uniquement pour rendre le build vert.

## SpotBugs remonte un finding

- identifier le type, la classe et la ligne ;
- qualifier le vrai ou faux positif ;
- corriger le code si nécessaire ;
- documenter toute acceptation temporaire ;
- ne pas ajouter une exclusion globale sans justification.

## Gitleaks bloque le pipeline

Pour un vrai secret :

1. révoquer le secret ;
2. créer une nouvelle valeur ;
3. retirer le secret du code ;
4. purger l'historique si nécessaire ;
5. relancer le scan.

Pour un faux positif :

1. prouver que la valeur n'est pas opérationnelle ;
2. retirer les rapports générés du dépôt ;
3. documenter la qualification ;
4. limiter l'exclusion au fingerprint exact.

## Trivy reçoit HTTP 429 de Maven Central

Le runner partagé est limité temporairement.

Le pipeline doit :

1. restaurer le cache Maven ;
2. exécuter `dependency:go-offline` ;
3. monter `~/.m2` en lecture seule dans Trivy.

## Build Docker en échec

```powershell
docker build `
  --no-cache `
  --progress=plain `
  -t urbanhub-ingestion:diagnostic `
  services/ingestion-service
```

Vérifier :

- `pom.xml` ;
- `src/` ;
- la configuration Checkstyle ;
- `.dockerignore` ;
- l'accès à Maven Central.

## Réinitialisation locale

```powershell
docker compose down --volumes --remove-orphans
docker compose up --build -d kafka ingestion-service
```

`docker system prune` ne doit être utilisé qu'en connaissance de son impact sur les ressources Docker inutilisées.
