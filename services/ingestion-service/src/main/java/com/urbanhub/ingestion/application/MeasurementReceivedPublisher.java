package com.urbanhub.ingestion.application;


import com.urbanhub.ingestion.events.MeasurementReceivedEvent;

public interface MeasurementReceivedPublisher {

    void publish(MeasurementReceivedEvent event);
}
