package com.urbanhub.alerting.Application;

import com.urbanhub.alerting.Events.AirQualityAlertDetectedEvent;
import com.urbanhub.alerting.Notification.NotificationPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.*;

class AlertingServiceTest {

    private final NotificationPort notificationPort = mock(NotificationPort.class);
    private final ProcessedEventRepository processedEventRepository = mock(ProcessedEventRepository.class);

    private final AlertingService alertingService =
            new AlertingService(notificationPort, processedEventRepository);

    @Test
    void shouldNotifyCsuWhenAirQualityAlertIsCritical() {
        AirQualityAlertDetectedEvent event = criticalNo2Event("event-1");

        when(processedEventRepository.hasAlreadyBeenProcessed("event-1"))
                .thenReturn(false);

        alertingService.handle(event);

        verify(notificationPort).notifyCsu(event);
        verify(processedEventRepository).markAsProcessed("event-1");
    }

    private AirQualityAlertDetectedEvent criticalNo2Event(String eventId) {
        return new AirQualityAlertDetectedEvent(
                eventId,
                "AirQualityAlertDetected",
                "1.0",
                Instant.now(),
                "corr-1",
                "air-quality-service",
                "ZFE-1",
                "AIR-STATION-042",
                "NO2",
                220.5,
                "µg/m3",
                "CRITICAL",
                200.0
        );
    }
}