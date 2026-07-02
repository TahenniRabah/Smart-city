package com.urbanhub.ingestion.domain;

public class ActiveSensorState implements SensorState {

    @Override
    public boolean canSendMeasurement() {
        return true;
    }

    @Override
    public String name() {
        return "ACTIVE";
    }
}