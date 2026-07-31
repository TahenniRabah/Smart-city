# TP2 · Runbook d’observabilité UrbanHub

## 1. Objet

Ce runbook décrit les actions à effectuer lorsqu’une alerte menace les SLO du service `ingestion-service` d’UrbanHub.

Périmètre observé :

```text
Passerelle IoT
  -> POST /api/ingestion/measurements
  -> ingestion-service
  -> Kafka / measurements.received
```

## 2. Informations utiles

| Composant | Accès local |
|---|---|
| API d’ingestion | `http://localhost:8082` |
| Readiness | `http://localhost:8082/actuator/health/readiness` |
| Métriques | `http://localhost:8082/actuator/prometheus` |
| Prometheus | `http://localhost:9090` |
| Alertmanager | `http://localhost:9093` |
| Grafana | `http://localhost:3000` |
| Loki | `http://localhost:3100` |
| Tempo | `http://localhost:3200` |
| Redpanda Console | `http://localhost:8090` |

## 3. Principes d’intervention

1. Conserver les preuves avant toute action destructive.
2. Noter l’heure de début de l’incident en UTC.
3. Vérifier d’abord le symptôme utilisateur, puis les ressources.
4. Corréler métriques, logs et traces avec la même fenêtre temporelle.
5. Utiliser le `correlationId` et le `traceId` lorsque ceux-ci sont disponibles.
6. Ne redémarrer un service qu’après avoir exporté les données utiles au diagnostic.

## 4. Alerte `UrbanHubHighLatencyP95`

### Déclenchement

La latence P95 de l’endpoint d’ingestion dépasse 500 ms pendant au moins 5 minutes.

### Impact possible

- retard dans l’acceptation des mesures urbaines ;
- consommation accélérée de l’error budget de latence ;
- accumulation de requêtes ou saturation d’une dépendance ;
- dégradation du caractère quasi temps réel de la plateforme.

### Diagnostic

1. Vérifier simultanément le débit, le P95 et le P99 dans le dashboard Golden Signals.
2. Examiner le CPU et la mémoire du conteneur `ingestion-service`.
3. Vérifier le heap, les threads JVM et les éventuelles pauses GC.
4. Ouvrir une trace lente dans Tempo et identifier le span dominant.
5. Vérifier la durée de publication Kafka et la santé du broker.
6. Contrôler le volume de réponses `429` afin de distinguer saturation et rate limiting.

### Commandes CMD

```cmd
curl.exe -s http://localhost:8082/actuator/health/readiness
curl.exe -s http://localhost:9090/api/v1/alerts
curl.exe -s http://localhost:9090/api/v1/targets
docker logs ingestion-service --since 15m
docker logs urbanhub-kafka --since 15m
```

### Actions

- réduire ou arrêter temporairement le générateur de charge ;
- vérifier une modification ou un déploiement récent ;
- restaurer la configuration précédente si la régression est liée à un changement ;
- redémarrer uniquement après conservation des preuves ;
- ouvrir une action d’optimisation si le seuil est reproductiblement dépassé.

## 5. Alerte `UrbanHubHighServerErrorRate`

### Déclenchement

Le taux de réponses HTTP `5xx` dépasse 1 % pendant au moins 5 minutes.

### Impact possible

- mesures valides non acceptées ;
- perte potentielle d’événements ;
- consommation de l’error budget de disponibilité et d’acceptation.

### Diagnostic

1. Vérifier la répartition des codes HTTP dans Grafana.
2. Distinguer les erreurs `400`, `401`, `429` et `5xx`.
3. Examiner les logs du service sur la fenêtre d’alerte.
4. Rechercher un `correlationId` associé à une erreur.
5. Ouvrir la trace correspondante dans Tempo.
6. Vérifier Kafka si l’erreur se produit pendant la publication.

### Commandes CMD

```cmd
docker logs ingestion-service --since 15m > docs	p2-observabilite\preuves\ingestion-errors.log
curl.exe -s http://localhost:9090/api/v1/alerts > docs	p2-observabilite\preuves	p2-alerts.json
```

### Actions

- corriger ou annuler le changement fautif ;
- vérifier la connectivité avec Kafka ;
- conserver une requête et une trace représentatives ;
- classer l’incident comme critique si les mesures valides ne sont plus acceptées.

## 6. Alerte `UrbanHubIngestionSilent`

### Déclenchement

Aucune mesure valide n’a été acceptée depuis plus de 5 minutes, puis la condition est restée vraie pendant 1 minute.

### Impact possible

- interruption des passerelles IoT ;
- indisponibilité de l’API ;
- blocage du flux d’ingestion ;
- absence de données fraîches pour l’analyse et l’alerting.

### Diagnostic

1. Vérifier que les passerelles ou k6 ne sont pas simplement arrêtés volontairement.
2. Tester la readiness de l’API.
3. Envoyer une mesure valide avec la clé API.
4. Vérifier le compteur des réponses `202`.
5. Vérifier le compteur de succès Kafka.
6. Contrôler le topic `measurements.received` dans Redpanda Console.

### Commandes CMD

```cmd
curl.exe -s http://localhost:8082/actuator/health/readiness
curl.exe -s http://localhost:8082/actuator/prometheus | findstr urbanhub_sensor_last_seen_epoch_seconds
curl.exe -s http://localhost:9090/api/v1/alerts
curl.exe -s http://localhost:9093/api/v2/alerts
```

### Actions

- relancer le producteur de mesures si l’arrêt n’était pas prévu ;
- restaurer l’API si la readiness est en échec ;
- vérifier Kafka si l’API accepte les mesures mais qu’aucun événement n’est publié ;
- clôturer l’incident après réception d’une nouvelle mesure et résolution de l’alerte.

## 7. Alerte `UrbanHubKafkaPublishFailures`

### Déclenchement

Au moins une publication Kafka a échoué au cours des 5 dernières minutes et la condition reste vraie pendant 1 minute.

### Impact possible

Une requête peut avoir reçu HTTP `202` alors que l’acquittement Kafka échoue ensuite, car la publication est asynchrone.

### Diagnostic

1. Vérifier la santé du conteneur Kafka.
2. Vérifier la résolution et l’accès à `kafka:29092` depuis `ingestion-service`.
3. Examiner les compteurs Kafka succès/échec.
4. Rechercher le `correlationId` dans les logs.
5. Vérifier la présence de l’événement dans `measurements.received`.

### Commandes CMD

```cmd
docker ps --filter name=urbanhub-kafka
docker logs urbanhub-kafka --since 15m
docker logs ingestion-service --since 15m | findstr "publication failed"
curl.exe -s http://localhost:8082/actuator/prometheus | findstr urbanhub_kafka_publish
```

### Actions

- restaurer Kafka ou la connectivité réseau ;
- vérifier les paramètres du producteur ;
- identifier les événements potentiellement perdus à partir des `correlationId` ;
- prévoir ultérieurement retry, idempotence et DLQ selon l’architecture retenue.

## 8. Collecte des preuves

Les preuves doivent être déposées dans :

```text
docs/tp2-observabilite/preuves/
```

Fichiers utiles :

```text
prometheus-targets.json
prometheus-rules.json
tp2-alerts.json
alertmanager-alerts.json
tp2-alertmanager-events.jsonl
ingestion-response.json
kafka-publication-correlated.log
tempo-trace.json
k6-summary.json
```

## 9. Critères de résolution

Un incident est considéré comme résolu lorsque :

- l’alerte n’est plus en état `firing` ;
- le service est `UP` ;
- une mesure valide reçoit HTTP `202` ;
- la publication Kafka correspondante est confirmée ;
- les métriques reviennent dans les seuils ;
- les preuves et les actions sont documentées.
