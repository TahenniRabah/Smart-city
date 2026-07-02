package com.urbanhub.airquality.Api;


import com.urbanhub.airquality.domain.AlertLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse contenant le niveau d'alerte calculé")
public record AirQualityAlertResponse(
        @Schema(description = "Niveau d'alerte calculé", example = "CRITICAL")
        AlertLevel alertLevel
) {
}

