package com.urbanhub.ingestion.messaging;


import com.urbanhub.ingestion.application.MeasurementReceivedPublisher;
import com.urbanhub.ingestion.events.MeasurementReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaMeasurementReceivedPublisher implements MeasurementReceivedPublisher {

    private final KafkaTemplate<String, MeasurementReceivedEvent> kafkaTemplate;
    private final String topicName;

    public KafkaMeasurementReceivedPublisher(
            KafkaTemplate<String, MeasurementReceivedEvent> kafkaTemplate,
            @Value("${urbanhub.kafka.topics.measurements-received}") String topicName
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    @Override
    public void publish(MeasurementReceivedEvent event) {
        kafkaTemplate.send(topicName, event.zoneId(), event);
    }
}

