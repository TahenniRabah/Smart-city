# Event Contract — AirQualityAlertDetected

## Topic

`air-quality.alert.detected`

## Producer

`air-quality-service`

## Consumer

`alerting-service`

## Partition key

`zoneId`

## Event example

```json
{
  "eventId": "evt-2026-000001",
  "eventType": "AirQualityAlertDetected",
  "eventVersion": "1.0",
  "occurredAt": "2026-05-04T14:30:00Z",
  "correlationId": "corr-urbanhub-12345",
  "source": "air-quality-service",
  "zoneId": "ZFE-1",
  "stationId": "AIR-STATION-042",
  "pollutant": "NO2",
  "measuredValue": 220.5,
  "unit": "µg/m3",
  "alertLevel": "CRITICAL",
  "threshold": 200
}