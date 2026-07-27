package com.urbanhub.alerting.Application;

import com.urbanhub.alerting.Events.AirQualityAlertDetectedEvent;
import com.urbanhub.alerting.Notification.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AlertingService {

    private static final Logger log =
            LoggerFactory.getLogger(AlertingService.class);

    private final NotificationPort notificationPort;
    private final ProcessedEventRepository processedEventRepository;

    public AlertingService(
            NotificationPort notificationPort,
            ProcessedEventRepository processedEventRepository
    ) {
        this.notificationPort = notificationPort;
        this.processedEventRepository = processedEventRepository;
    }

    public void handle(AirQualityAlertDetectedEvent event) {

        if (processedEventRepository.hasAlreadyBeenProcessed(event.eventId())) {
            log.info(
                    "Duplicate event ignored: eventId={}, correlationId={}",
                    event.eventId(),
                    event.correlationId()
            );
            return;
        }

        if (isCritical(event)) {
            notificationPort.notifyCsu(event);
        }

        processedEventRepository.markAsProcessed(event.eventId());

        log.info(
                "Alert event processed: eventId={}, level={}",
                event.eventId(),
                event.alertLevel()
        );
    }

    private boolean isCritical(AirQualityAlertDetectedEvent event) {
        return "CRITICAL".equals(event.alertLevel());
    }
}