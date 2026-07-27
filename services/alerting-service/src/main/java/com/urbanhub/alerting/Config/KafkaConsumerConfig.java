package com.urbanhub.alerting.Config;

import com.urbanhub.alerting.Events.AirQualityAlertDetectedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, AirQualityAlertDetectedEvent>
    airQualityAlertConsumerFactory() {

        JacksonJsonDeserializer<AirQualityAlertDetectedEvent> valueDeserializer =
                new JacksonJsonDeserializer<>(
                        AirQualityAlertDetectedEvent.class
                );

        valueDeserializer.ignoreTypeHeaders();

        Map<String, Object> config = new HashMap<>();

        config.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        config.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "alerting-service-v2"
        );

        config.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        config.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AirQualityAlertDetectedEvent>
    airQualityAlertKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, AirQualityAlertDetectedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                airQualityAlertConsumerFactory()
        );

        return factory;
    }
}