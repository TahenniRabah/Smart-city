package com.urbanhub.airquality.config;

import com.urbanhub.airquality.events.AirQualityAlertDetectedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, AirQualityAlertDetectedEvent>
    airQualityAlertProducerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        JacksonJsonSerializer<AirQualityAlertDetectedEvent> valueSerializer =
                new JacksonJsonSerializer<AirQualityAlertDetectedEvent>()
                        .noTypeInfo();

        return new DefaultKafkaProducerFactory<>(
                config,
                new StringSerializer(),
                valueSerializer
        );
    }

    @Bean
    public KafkaTemplate<String, AirQualityAlertDetectedEvent>
    airQualityAlertKafkaTemplate() {

        return new KafkaTemplate<>(
                airQualityAlertProducerFactory()
        );
    }
}