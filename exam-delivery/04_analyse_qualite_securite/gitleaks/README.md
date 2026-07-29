# Gitleaks Evidence

Ce dossier contient les résultats du scan de secrets du dépôt UrbanHub.

## Politique

* le scan couvre le contenu actuel et l’historique Git ;
* les secrets sont masqués dans les sorties ;
* tout secret réel détecté doit être révoqué avant sa suppression du dépôt ;
* une exclusion n’est autorisée qu’après qualification comme faux positif ;
* la CI doit échouer lorsqu’un secret est détecté.

## Commande

```bash
gitleaks detect --source /repo --redact

