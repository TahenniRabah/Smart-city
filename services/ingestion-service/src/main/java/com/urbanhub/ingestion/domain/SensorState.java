package com.urbanhub.ingestion.domain;


public interface SensorState {

    boolean canSendMeasurement();

    String name();
}
