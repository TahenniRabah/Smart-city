\# Event Contract — MeasurementValidated



\## Topic



`measurements.validated`



\## Producer



`quality-service`



\## Consumer



`air-quality-service`



\## Partition key



`zoneId`



\## Event version



`1.0`



\## Example



```json

{

&#x20; "eventId": "evt-validated-001",

&#x20; "eventType": "MeasurementValidated",

&#x20; "eventVersion": "1.0",

&#x20; "correlationId": "corr-12345",

&#x20; "occurredAt": "2026-05-06T14:30:01Z",

&#x20; "source": "quality-service",

&#x20; "zoneId": "ZFE-1",

&#x20; "stationId": "AIR-STATION-042",

&#x20; "indicator": "NO2",

&#x20; "value": 220.5,

&#x20; "timestamp": "2026-05-06T14:29:58Z"

}

