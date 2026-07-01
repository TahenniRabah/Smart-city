\# Event Contract — MeasurementReceived



\## Topic



`measurements.received`



\## Producer



`ingestion-service`



\## Consumers



`quality-service`



\## Partition key



`zoneId`



\## Event version



`1.0`



\## Example



```json

{

&#x20; "eventId": "evt-001",

&#x20; "eventType": "MeasurementReceived",

&#x20; "eventVersion": "1.0",

&#x20; "correlationId": "corr-12345",

&#x20; "occurredAt": "2026-05-06T14:30:00Z",

&#x20; "source": "ingestion-service",

&#x20; "zoneId": "ZFE-1",

&#x20; "stationId": "AIR-STATION-042",

&#x20; "indicator": "NO2",

&#x20; "value": 220.5,

&#x20; "timestamp": "2026-05-06T14:29:58Z"

}

