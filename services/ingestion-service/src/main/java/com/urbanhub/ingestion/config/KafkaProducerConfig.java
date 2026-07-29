package com.urbanhub.ingestion.config;

import com.urbanhub.ingestion.events.MeasurementReceivedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, MeasurementReceivedEvent>
    measurementReceivedProducerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        JsonSerializer<MeasurementReceivedEvent> valueSerializer =
                new JsonSerializer<MeasurementReceivedEvent>()
                        .noTypeInfo();

        return new DefaultKafkaProducerFactory<>(
                config,
                new StringSerializer(),
                valueSerializer
        );
    }

    @Bean
    public KafkaTemplate<String, MeasurementReceivedEvent>
    measurementReceivedKafkaTemplate() {

        return new KafkaTemplate<>(
                measurementReceivedProducerFactory()
        );
    }
}
