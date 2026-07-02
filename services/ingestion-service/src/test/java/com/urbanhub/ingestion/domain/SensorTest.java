package com.urbanhub.ingestion.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SensorTest {

    @Test
    void activeSensorShouldBeAbleToSendMeasurement() {
        Sensor sensor = new Sensor(
                "SENSOR-001",
                "AIR-STATION-042",
                "ZFE-1",
                new ActiveSensorState()
        );

        assertTrue(sensor.canSendMeasurement());
        assertEquals("ACTIVE", sensor.status());
    }

    @Test
    void inactiveSensorShouldNotBeAbleToSendMeasurement() {
        Sensor sensor = new Sensor(
                "SENSOR-001",
                "AIR-STATION-042",
                "ZFE-1",
                new InactiveSensorState()
        );

        assertFalse(sensor.canSendMeasurement());
        assertEquals("INACTIVE", sensor.status());
    }

    @Test
    void faultySensorShouldNotBeAbleToSendMeasurement() {
        Sensor sensor = new Sensor(
                "SENSOR-001",
                "AIR-STATION-042",
                "ZFE-1",
                new FaultySensorState()
        );

        assertFalse(sensor.canSendMeasurement());
        assertEquals("FAULTY", sensor.status());
    }

    @Test
    void sensorShouldChangeStateFromInactiveToActive() {
        Sensor sensor = new Sensor(
                "SENSOR-001",
                "AIR-STATION-042",
                "ZFE-1",
                new InactiveSensorState()
        );

        sensor.activate();

        assertTrue(sensor.canSendMeasurement());
        assertEquals("ACTIVE", sensor.status());
    }

    @Test
    void sensorShouldChangeStateToFaulty() {
        Sensor sensor = new Sensor(
                "SENSOR-001",
                "AIR-STATION-042",
                "ZFE-1",
                new ActiveSensorState()
        );

        sensor.markAsFaulty();

        assertFalse(sensor.canSendMeasurement());
        assertEquals("FAULTY", sensor.status());
    }

    @Test
    void sensorShouldRejectMissingSensorId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Sensor(
                        "",
                        "AIR-STATION-042",
                        "ZFE-1",
                        new ActiveSensorState()
                )
        );

        assertEquals("sensorId is required", exception.getMessage());
    }
}