package com.urbanhub.ingestion.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class UrbanHubMetrics {

    private final MeterRegistry registry;
    private final Counter accepted;
    private final Counter stale;
    private final AtomicLong lastSeenEpochSeconds = new AtomicLong(0);

    public UrbanHubMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.accepted = Counter.builder("urbanhub.ingestion.accepted")
                .description("Mesures valides acceptées par l'API")
                .register(registry);
        this.stale = Counter.builder("urbanhub.ingestion.stale")
                .description("Mesures acceptées âgées de plus de 60 secondes")
                .register(registry);
        Gauge.builder("urbanhub.sensor.last.seen.epoch.seconds", lastSeenEpochSeconds, AtomicLong::get)
                .description("Epoch de la dernière mesure acceptée")
                .register(registry);
    }

    public void recordAccepted(Instant measurementTimestamp) {
        accepted.increment();
        lastSeenEpochSeconds.set(Instant.now().getEpochSecond());
        if (measurementTimestamp != null
                && Duration.between(measurementTimestamp, Instant.now()).compareTo(Duration.ofSeconds(60)) > 0) {
            stale.increment();
        }
    }

    public void recordKafkaSuccess(Duration duration) {
        kafkaCounter("success").increment();
        kafkaTimer("success").record(duration);
    }

    public void recordKafkaFailure(Duration duration) {
        kafkaCounter("failure").increment();
        kafkaTimer("failure").record(duration);
    }

    private Counter kafkaCounter(String result) {
        return Counter.builder("urbanhub.kafka.publish")
                .tag("result", result)
                .description("Résultats des publications vers Kafka")
                .register(registry);
    }

    private Timer kafkaTimer(String result) {
        return Timer.builder("urbanhub.kafka.publish.duration")
                .tag("result", result)
                .publishPercentileHistogram()
                .serviceLevelObjectives(
                        Duration.ofMillis(10),
                        Duration.ofMillis(50),
                        Duration.ofMillis(100),
                        Duration.ofMillis(250),
                        Duration.ofMillis(500))
                .register(registry);
    }
}
