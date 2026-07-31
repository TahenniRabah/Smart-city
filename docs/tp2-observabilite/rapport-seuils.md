# TP2 · Rapport de justification des seuils d’alerte UrbanHub

## 1. Contexte

Le périmètre observé est le service `ingestion-service` d’UrbanHub. Le service reçoit des mesures sur :

```text
POST /api/ingestion/measurements
```

Une requête valide et authentifiée reçoit HTTP `202`, puis le service publie un événement `MeasurementReceived` dans le topic Kafka `measurements.received`.

L’observabilité repose sur :

- Spring Boot Actuator et Micrometer pour les métriques HTTP et JVM ;
- Prometheus pour la collecte et l’évaluation des règles ;
- Grafana pour les dashboards ;
- OpenTelemetry et Tempo pour les traces ;
- Loki ou les logs Docker pour les événements corrélés ;
- Alertmanager et un webhook local pour les notifications.

Les seuils du TP2 sont dérivés des objectifs initiaux du TP1. Ils constituent encore des cibles à valider après plusieurs campagnes de charge reproductibles.

## 2. Correspondance entre SLO et alertes

### 2.1 Latence de l’ingestion

Le TP1 fixe comme cible initiale :

```text
Au moins 99 % des requêtes valides acceptées en moins de 500 ms.
```

L’alerte associée est :

```text
UrbanHubHighLatencyP95
```

Condition :

```text
Latence P95 > 500 ms pendant 5 minutes.
```

### Justification

Le seuil de 500 ms est cohérent avec l’objectif de quasi temps réel tout en laissant une marge pour Spring Security, la validation, le rate limiting, la sérialisation et l’appel au producteur Kafka. La fenêtre de 5 minutes évite de déclencher une alerte sur un pic isolé ou sur une phase de chauffe de la JVM.

Sévérité retenue : `warning`, car une latence élevée constitue d’abord une dégradation avant de devenir une indisponibilité.

## 3. Taux de réponses `5xx`

L’alerte associée est :

```text
UrbanHubHighServerErrorRate
```

Condition :

```text
Taux de réponses HTTP 5xx > 1 % pendant 5 minutes.
```

### Justification

Une réponse `5xx` correspond à un échec imputable au service. Le seuil de 1 % est aligné sur la cible de 99 % utilisée pour la qualité de service. Une fenêtre de 5 minutes limite les alertes instables tout en détectant rapidement une panne durable.

Les réponses suivantes ne doivent pas être confondues avec des `5xx` :

- `400` : payload invalide ;
- `401` : authentification absente ou invalide ;
- `429` : protection par rate limiting ;
- `202` : mesure acceptée par l’API.

Sévérité retenue : `critical`, car des mesures valides risquent de ne pas être acceptées.

## 4. Flux d’ingestion silencieux

L’alerte associée est :

```text
UrbanHubIngestionSilent
```

Condition :

```text
Aucune mesure acceptée depuis plus de 5 minutes, condition maintenue pendant 1 minute.
```

### Justification

UrbanHub est une plateforme orientée événements. Une absence globale de mesures pendant plus de 5 minutes peut signaler une panne de passerelle, une indisponibilité de l’API ou une interruption de la chaîne d’ingestion.

La métrique est volontairement globale :

```text
urbanhub_sensor_last_seen_epoch_seconds
```

Aucun label `stationId` n’est utilisé afin d’éviter une forte cardinalité Prometheus lorsque le nombre de capteurs augmente.

Sévérité retenue : `warning`, car un silence global peut aussi correspondre à l’arrêt volontaire d’un générateur dans l’environnement local.

## 5. Échec de publication Kafka

L’alerte associée est :

```text
UrbanHubKafkaPublishFailures
```

Condition :

```text
Au moins un échec de publication Kafka sur 5 minutes, maintenu pendant 1 minute.
```

### Justification

Toute erreur de publication menace la conservation d’une mesure acceptée. Le seuil est volontairement strict, car l’API retourne actuellement HTTP `202` avant la confirmation asynchrone définitive du broker. Sans instrumentation du résultat de `KafkaTemplate.send`, un `202` ne suffit donc pas à prouver que Kafka a acquitté l’événement.

Sévérité retenue : `critical`.

## 6. Dashboard Golden Signals

Le dashboard Golden Signals couvre :

- débit HTTP ;
- taux de `5xx` ;
- disponibilité SLI ;
- latence P95 et P99 ;
- CPU et mémoire du conteneur ;
- publications Kafka réussies et échouées ;
- P95 de la durée de publication Kafka.

Cette vue s’adresse principalement à l’exploitation et permet de relier un symptôme visible à la saturation éventuelle des ressources.

## 7. Dashboard métier

Le dashboard métier couvre :

- mesures acceptées par seconde ;
- mesures âgées de plus de 60 secondes ;
- âge de la dernière mesure ;
- taux de succès Kafka ;
- réponses `429` ;
- répartition par code HTTP ;
- heap et threads JVM.

Cette vue permet de vérifier que le service remplit réellement sa fonction d’ingestion, et pas seulement que le processus Java est démarré.

## 8. Résultats observés

À compléter avec les résultats réellement obtenus.

### 8.1 Targets Prometheus

```text
État de ingestion-service : À compléter
État de otel-collector    : À compléter
État de cadvisor          : À compléter
```

Preuve :

```text
docs/tp2-observabilite/preuves/prometheus-targets.json
```

### 8.2 Test k6

```text
Latence moyenne : À compléter
Latence P95     : À compléter
Latence P99     : À compléter
Taux d’échec    : À compléter
Taux HTTP 202   : À compléter
```

Preuve :

```text
docs/tp2-observabilite/preuves/k6-summary.json
```

### 8.3 Corrélation

```text
correlationId : À compléter
traceId       : À compléter
Publication Kafka confirmée : oui / non
Trace visible dans Tempo    : oui / non
Log visible dans Loki       : oui / non
Log Docker corrélé          : oui / non
```

Preuves :

```text
ingestion-response.json
kafka-publication-correlated.log
tempo-trace.json
```

### 8.4 Alerte contrôlée

```text
Alerte testée : UrbanHubIngestionSilent
État observé  : À compléter
Heure firing  : À compléter
Notification reçue par le webhook : oui / non
```

Preuves :

```text
tp2-alerts.json
alertmanager-alerts.json
tp2-alertmanager-events.jsonl
```

## 9. Limites

### Kafka local

Kafka utilise un seul broker et un facteur de réplication de 1. Cette configuration convient à un TP local, mais ne fournit pas de haute disponibilité.

### Sémantique de HTTP `202`

HTTP `202` est renvoyé avant la confirmation asynchrone définitive de Kafka. La réussite métier doit donc être confirmée par la métrique de publication Kafka et les logs corrélés.

### Rate limiting

Bucket4j conserve les buckets en mémoire dans chaque instance. Le quota n’est pas partagé entre plusieurs replicas. Tous les appels authentifiés utilisent actuellement l’identité `iot-sensor`, ce qui mutualise le quota à l’intérieur d’une même instance.

### Alerte de silence

L’alerte surveille le flux global et non chaque station. Ce choix évite une cardinalité élevée mais ne détecte pas directement la disparition d’un capteur isolé.

### Logs Loki

Si l’export automatique des logs vers Loki n’est pas fonctionnel, la preuve doit l’indiquer explicitement. La corrélation peut alors reposer sur les métriques Prometheus, la trace Tempo et un log Docker horodaté contenant le `correlationId`.

### Baseline

Les seuils restent des cibles initiales tant que plusieurs campagnes k6 exécutées dans des conditions comparables n’ont pas établi une baseline suffisamment stable.

## 10. Conclusion

Le TP2 relie les SLO du TP1 à des métriques, des dashboards et des alertes actionnables. La stack permet de surveiller le comportement HTTP, la JVM, les ressources du conteneur et le résultat de la publication Kafka.

Les prochaines étapes consistent à compléter ce rapport avec les valeurs réellement observées, conserver les preuves dans Git et utiliser cette baseline pour le profiling et les optimisations du TP3.
