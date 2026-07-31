package com.urbanhub.ingestion.messaging;

import com.urbanhub.ingestion.application.MeasurementReceivedPublisher;
import com.urbanhub.ingestion.events.MeasurementReceivedEvent;
import com.urbanhub.ingestion.observability.UrbanHubMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class KafkaMeasurementReceivedPublisher implements MeasurementReceivedPublisher {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(KafkaMeasurementReceivedPublisher.class);

    private final KafkaTemplate<String, MeasurementReceivedEvent> kafkaTemplate;
    private final UrbanHubMetrics metrics;
    private final String topicName;

    public KafkaMeasurementReceivedPublisher(
            KafkaTemplate<String, MeasurementReceivedEvent> kafkaTemplate,
            UrbanHubMetrics metrics,
            @Value("${urbanhub.kafka.topics.measurements-received}") String topicName
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.metrics = metrics;
        this.topicName = topicName;
    }

    @Override
    public void publish(MeasurementReceivedEvent event) {
        Instant startedAt = Instant.now();

        kafkaTemplate.send(topicName, event.zoneId(), event)
                .whenComplete((result, error) -> {
                    Duration duration = Duration.between(startedAt, Instant.now());
                    if (error == null) {
                        metrics.recordKafkaSuccess(duration);
                        LOGGER.info(
                                "measurement published topic={} partition={} offset={} correlationId={}",
                                topicName,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                event.correlationId());
                    } else {
                        metrics.recordKafkaFailure(duration);
                        LOGGER.error(
                                "measurement publication failed topic={} correlationId={}",
                                topicName,
                                event.correlationId(),
                                error);
                    }
                });
    }
}
