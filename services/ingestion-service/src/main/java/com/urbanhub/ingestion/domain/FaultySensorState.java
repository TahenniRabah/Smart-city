package com.urbanhub.ingestion.domain;

public class FaultySensorState implements SensorState {

    @Override
    public boolean canSendMeasurement() {
        return false;
    }

    @Override
    public String name() {
        return "FAULTY";
    }
}