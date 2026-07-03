\# Communication technique vs fonctionnelle



\## Communication fonctionnelle



UrbanHub permet de détecter automatiquement un épisode de pollution à partir des mesures envoyées par les capteurs urbains.



Lorsqu’une mesure dépasse un seuil critique, une alerte est transmise au centre de supervision afin de faciliter une réaction rapide.



\## Communication technique



Le service `ingestion-service` reçoit une mesure brute via une API REST puis publie un événement `MeasurementReceived` dans Kafka.



Le `quality-service` consomme cet événement, applique les règles de validation, puis publie `MeasurementValidated` ou `MeasurementRejected`.



Le `air-quality-service` consomme les mesures validées, calcule le niveau d’alerte et publie `AirQualityAlertDetected`.



Le `alerting-service` consomme les alertes et prépare une notification CSU si le niveau est `CRITICAL`.

`

