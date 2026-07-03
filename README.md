\#Smart-city





| Service | Port | Rôle |

|---|---:|---|

| ingestion-service | 8082 | Réception des mesures brutes IoT |

| quality-service | 8083 | Validation / rejet des mesures |

| air-quality-service | 8080 | Analyse des seuils qualité de l’air |

| alerting-service | 8081 | Gestion des alertes et notifications |







\## Topics Kafka



| Topic | Producteur | Consommateur |

|---|---|---|

| measurements.received | ingestion-service | quality-service |

| measurements.validated | quality-service | air-quality-service |

| measurements.rejected | quality-service | monitoring / dashboard |

| air-quality.alert.detected | air-quality-service | alerting-service |

