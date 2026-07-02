package com.urbanhub.quality.application;

import com.urbanhub.quality.events.MeasurementRejectedEvent;

public interface MeasurementRejectedPublisher {

    void publish(MeasurementRejectedEvent event);
}