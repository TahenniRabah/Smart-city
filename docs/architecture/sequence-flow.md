# Architecture — Flux événementiel UrbanHub

## Flux principal

```text
ingestion-service
→ measurements.received
→ quality-service
→ measurements.validated
→ air-quality-service
→ air-quality.alert.detected
→ alerting-service
```

\---

## Rôle des services

* `ingestion-service` reçoit les mesures brutes IoT.
* `quality-service` valide ou rejette les mesures.
* `air-quality-service` calcule les seuils d’alerte.
* `alerting-service` prépare les notifications CSU.



