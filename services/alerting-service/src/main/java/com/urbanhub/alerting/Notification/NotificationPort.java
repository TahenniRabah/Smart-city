package com.urbanhub.alerting.Notification;


import com.urbanhub.alerting.Events.AirQualityAlertDetectedEvent;

public interface NotificationPort {

    void notifyCsu(AirQualityAlertDetectedEvent event);
}

