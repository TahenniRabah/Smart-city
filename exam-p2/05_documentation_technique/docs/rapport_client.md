# Rapport de synthèse client - UrbanHub

## BLUF

UrbanHub dispose désormais d'un service d'ingestion IoT industrialisé, testé et sécurisé pour un déploiement local reproductible. Le pipeline automatisé vérifie le code, les tests, la couverture, la qualité, les secrets, les dépendances et l'image Docker avant de lancer des smoke tests.

La plateforme réduit ainsi le risque d'accepter des données invalides, de publier un secret par erreur ou de livrer une version non testée. Les prochaines priorités sont la sécurisation complète de Kafka, l'identité distincte de chaque passerelle et la remédiation progressive des vulnérabilités de dépendances.

## Valeur pour la ville

### Fiabilité des données

Les mesures sont authentifiées, limitées et validées avant publication. Une donnée invalide ne doit produire aucun événement Kafka.

### Réactivité opérationnelle

L'architecture événementielle permet de distribuer rapidement une mesure vers les services de qualité, d'analyse et d'alerte sans bloquer l'API d'entrée.

### Réduction du risque

Le pipeline bloque :

- les tests en échec ;
- les violations de style ;
- une couverture insuffisante ;
- les secrets détectés ;
- une image Docker non construite ;
- un déploiement local non fonctionnel.

### Maintenabilité

La documentation Doc as Code, les contrats OpenAPI, les diagrammes et le changelog facilitent la reprise du projet et limitent la dette technique.

## Résultats principaux

| Indicateur | Résultat |
|---|---|
| Pipeline | 6 jobs automatisés et enchaînés |
| Tests | Tous réussis |
| Couverture des instructions | Environ 78 % |
| Couverture des branches | Environ 69 % |
| Finding SpotBugs SECURITY | 0 |
| Détection de secrets | Gate Gitleaks bloquante |
| Inventaire logiciel | SBOM CycloneDX |
| Déploiement | Docker Compose local validé |
| Smoke tests | Readiness, HTTP 401 et HTTP 202 validés |

## Impacts métier

### Impact financier

- réduction du temps consacré aux vérifications manuelles ;
- détection plus précoce des défauts, donc correction moins coûteuse ;
- environnement reproductible limitant les écarts entre postes ;
- documentation réduisant le coût de transfert de connaissance.

### Impact écologique

- meilleure qualité des données utilisées pour suivre la pollution ;
- détection plus rapide des anomalies de mesure ;
- base technique pour déclencher des alertes et décisions urbaines ciblées ;
- image multi-stage limitant les composants inutiles déployés.

### Impact opérationnel

- traçabilité par `correlationId` ;
- disponibilité contrôlée par healthcheck ;
- erreurs standardisées pour accélérer le diagnostic ;
- automatisation du build et des smoke tests.

## Risques résiduels

| Risque | Niveau | Recommandation |
|---|---|---|
| Clé API partagée | Modéré | Identité distincte par passerelle |
| Rate limiting en mémoire | Modéré | Redis ou API Gateway |
| Kafka local non chiffré | Élevé avant production | TLS, SASL et ACL |
| Vulnérabilités de dépendances | Modéré | Priorisation et mises à jour |
| Observabilité limitée | Modéré | Métriques, traces et alertes centralisées |

## Recommandations prioritaires

### Court terme

1. qualifier et corriger les vulnérabilités Trivy ;
2. terminer la qualification du finding SpotBugs ;
3. renforcer Kafka avec TLS, SASL et ACL ;
4. ajouter retry et DLQ.

### Moyen terme

1. attribuer une identité à chaque passerelle ;
2. externaliser les quotas ;
3. centraliser logs, métriques et traces ;
4. automatiser les mises à jour de dépendances.

### Long terme

1. déployer une API Gateway ;
2. signer les images ;
3. suivre la SBOM dans une plateforme dédiée ;
4. versionner le portail documentaire avec `mike`.

## Conclusion

La solution atteint un niveau opérationnel adapté à une démonstration industrialisée. Le pipeline, les tests, la sécurité applicative, la conteneurisation et la documentation fournissent une base solide. Le passage à la production nécessite principalement le renforcement des identités, du broker Kafka et de l'observabilité.
