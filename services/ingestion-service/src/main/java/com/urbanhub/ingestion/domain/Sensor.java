package com.urbanhub.ingestion.domain;

public class Sensor {

    private final String sensorId;
    private final String stationId;
    private final String zoneId;
    private SensorState state;

    public Sensor(
            String sensorId,
            String stationId,
            String zoneId,
            SensorState initialState
    ) {
        if (isBlank(sensorId)) {
            throw new IllegalArgumentException("sensorId is required");
        }

        if (isBlank(stationId)) {
            throw new IllegalArgumentException("stationId is required");
        }

        if (isBlank(zoneId)) {
            throw new IllegalArgumentException("zoneId is required");
        }

        if (initialState == null) {
            throw new IllegalArgumentException("initialState is required");
        }

        this.sensorId = sensorId;
        this.stationId = stationId;
        this.zoneId = zoneId;
        this.state = initialState;
    }

    public String sensorId() {
        return sensorId;
    }

    public String stationId() {
        return stationId;
    }

    public String zoneId() {
        return zoneId;
    }

    public String status() {
        return state.name();
    }

    public boolean canSendMeasurement() {
        return state.canSendMeasurement();
    }

    public void activate() {
        this.state = new ActiveSensorState();
    }

    public void deactivate() {
        this.state = new InactiveSensorState();
    }

    public void markAsFaulty() {
        this.state = new FaultySensorState();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}