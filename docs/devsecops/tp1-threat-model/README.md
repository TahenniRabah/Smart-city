# TP 1 — Threat Modeling STRIDE UrbanHub

## Objectif

Identifier, coter et prioriser les menaces pesant sur l’architecture événementielle UrbanHub avant une mise en préproduction.

## Méthode

La méthode STRIDE est appliquée aux entités, processus, stockages, flux et frontières de confiance.

## Échelle de cotation

### Vraisemblance

| Score | Signification |
|---:|---|
| 1 | Très improbable |
| 2 | Peu probable |
| 3 | Possible |
| 4 | Probable |
| 5 | Très probable |

### Impact

| Score | Signification |
|---:|---|
| 1 | Impact mineur |
| 2 | Impact limité |
| 3 | Impact significatif |
| 4 | Impact majeur |
| 5 | Impact critique, pouvant affecter la santé publique |

### Score de risque

```text
Score = Vraisemblance × Impact
```

| Score | Niveau |
|---:|---|
| 1 à 4 | Faible |
| 5 à 9 | Modéré |
| 10 à 14 | Élevé |
| 15 à 19 | Très élevé |
| 20 à 25 | Critique |

## Criticité métier

Les composants suivants sont considérés comme critiques :

- ingestion des mesures de pollution ;
- intégrité des événements Kafka ;
- calcul des seuils de pollution ;
- moteur d’alerting ;
- disponibilité du broker Kafka.

Une mesure falsifiée ou une alerte manquée peut provoquer une mauvaise décision de gestion urbaine ou sanitaire.

## Livrables

- périmètre de l’analyse ;
- DFD niveau 1 ;
- tableau STRIDE ;
- cotation Vraisemblance × Impact ;
- Top 5 des risques ;
- contre-mesure par risque ;
- propriétaire et délai ;
- restitution orale.

La criticité « vie humaine » est cohérente avec la matrice du cours pour la pollution de l’air et le moteur d’alertes.
