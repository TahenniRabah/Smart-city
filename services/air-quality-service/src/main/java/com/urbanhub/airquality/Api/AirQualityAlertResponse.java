package com.urbanhub.airquality.Api;


import com.urbanhub.airquality.Domain.AlertLevel;

public record AirQualityAlertResponse(
        AlertLevel alertLevel
) {
}

