package com.urbanhub.quality.application;

import com.urbanhub.quality.events.MeasurementValidatedEvent;

public interface MeasurementValidatedPublisher {

    void publish(MeasurementValidatedEvent event);
}