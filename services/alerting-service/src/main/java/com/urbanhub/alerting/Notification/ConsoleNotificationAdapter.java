package com.urbanhub.alerting.Notification;

import com.urbanhub.alerting.Events.AirQualityAlertDetectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConsoleNotificationAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(ConsoleNotificationAdapter.class);

    @Override
    public void notifyCsu(AirQualityAlertDetectedEvent event) {
        log.info(
                "Priority CSU notification prepared: eventId={}, zoneId={}, stationId={}, pollutant={}, level={}",
                event.eventId(),
                event.zoneId(),
                event.stationId(),
                event.pollutant(),
                event.alertLevel()
        );
    }
}