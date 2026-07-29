# Sécurité

## Objectifs

Les contrôles protègent :

- l'authenticité des passerelles ;
- l'intégrité des mesures ;
- la disponibilité de l'API ;
- la confidentialité des secrets ;
- la supply chain logicielle ;
- l'exécution du conteneur.

## Authentification par clé API

La route d'ingestion exige `X-API-Key`. La clé attendue provient de `INGESTION_API_KEY` et n'est pas stockée en clair dans le dépôt.

La comparaison utilise une méthode adaptée aux données sensibles. Le service reste stateless et ne crée pas de session HTTP.

Limite : une clé partagée ne fournit pas une identité distincte par capteur. Une production mature utilisera HMAC, OAuth2 client credentials ou mTLS.

## Autorisation

La configuration applique le refus par défaut. Seules les routes explicitement autorisées restent accessibles.

Les endpoints de santé et la documentation interactive sont ouverts uniquement selon la configuration de l'environnement.

## Validation des entrées

Bean Validation contrôle :

- présence des champs ;
- longueur des identifiants ;
- caractères autorisés ;
- liste des indicateurs ;
- plage de valeur ;
- cohérence temporelle.

Une validation métier défensive protège aussi le cas d'utilisation en dehors de l'interface HTTP.

## Rate limiting

Bucket4j applique un token bucket par identité authentifiée. Un quota épuisé retourne HTTP 429 et `Retry-After`.

Limite : les buckets sont en mémoire. Une architecture multi-instance doit employer Redis ou une API Gateway.

## Gestion des erreurs

Les réponses n'exposent ni stack trace, ni configuration Kafka, ni secret. Les erreurs utilisent un format JSON homogène avec un code stable.

## Sécurité Kafka

État actuel : environnement local de démonstration.

Évolutions recommandées :

- TLS ;
- authentification SASL ;
- ACL par microservice et topic ;
- quotas producteurs ;
- stratégie retry et DLQ ;
- supervision du lag.

## Sécurité du conteneur

L'image :

- utilise un build multi-stage ;
- contient uniquement le runtime final ;
- s'exécute avec l'utilisateur non-root `urbanhub` ;
- utilise un tag immuable fondé sur le SHA Git dans le pipeline.

Évolutions recommandées :

- filesystem read-only ;
- `no-new-privileges` ;
- suppression des capabilities Linux ;
- scan périodique des images ;
- signature de l'image.

## Gitleaks

Gitleaks analyse l'historique complet et bloque le pipeline lorsqu'un secret est détecté.

Un faux positif doit être qualifié et exclu uniquement par fingerprint exact. Un vrai secret doit être révoqué avant toute autre action.

## Trivy et dépendances

Trivy analyse les dépendances Maven et génère des rapports JSON et SARIF.

La baseline existante doit être priorisée selon :

- sévérité ;
- disponibilité d'un correctif ;
- exploitabilité ;
- criticité du composant ;
- exposition réelle.

La gate Trivy reste temporairement en audit pendant la qualification initiale. La cible est de bloquer les vulnérabilités critiques corrigibles.

## SpotBugs et Find Security Bugs

SpotBugs analyse le bytecode. Find Security Bugs ajoute des règles de sécurité Java.

Résultat initial : zéro finding classé `SECURITY`. Ce résultat ne remplace pas les autres contrôles, car un SAST ne couvre ni les secrets, ni les CVE des dépendances, ni la configuration runtime.

## SBOM CycloneDX

La SBOM inventorie les composants logiciels. Elle permet de répondre rapidement à une nouvelle CVE et améliore la traçabilité de la supply chain.

## Threat model synthétique

| Risque | Contrôle actuel | Risque résiduel |
|---|---|---|
| Usurpation de capteur | Clé API | Clé partagée |
| Flood HTTP | Rate limiting | Quota local à une instance |
| Payload malformé | Validation stricte | Cas métier futurs à maintenir |
| Secret commité | Gitleaks bloquant | Faux positifs à qualifier |
| Dépendance vulnérable | Trivy et SBOM | Baseline à remédier |
| Privilèges conteneur | Utilisateur non-root | Hardening complémentaire possible |
| Kafka compromis | Réseau local restreint | TLS, SASL et ACL à ajouter |

## Réponse à incident

En cas de fuite de secret :

1. révoquer immédiatement ;
2. identifier l'étendue ;
3. remplacer dans les environnements ;
4. retirer du dépôt et de l'historique ;
5. vérifier les journaux d'accès ;
6. relancer Gitleaks ;
7. documenter l'incident.

En cas de CVE critique :

1. identifier les composants avec la SBOM ;
2. vérifier l'exposition ;
3. mettre à jour ou mitiger ;
4. exécuter les tests ;
5. reconstruire l'image ;
6. rescanner ;
7. documenter la remédiation.
