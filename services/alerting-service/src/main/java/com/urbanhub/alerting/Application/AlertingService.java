package com.urbanhub.alerting.Application;

import com.urbanhub.alerting.Events.AirQualityAlertDetectedEvent;
import com.urbanhub.alerting.Notification.NotificationPort;
import org.springframework.stereotype.Service;

@Service
public class AlertingService {

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
            return;
        }

        if (isCritical(event)) {
            notificationPort.notifyCsu(event);
        }


        processedEventRepository.markAsProcessed(event.eventId());
    }

    private boolean isCritical(AirQualityAlertDetectedEvent event) {
        return "CRITICAL".equals(event.alertLevel());
    }

}