package com.urbanhub.airquality.Api;

import com.urbanhub.airquality.Domain.Pollutant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requête d'analyse d'une mesure de qualité de l'air")
public record AirQualityMeasurementRequest(

        @Schema(description = "Identifiant de la zone urbaine", example = "ZFE-1")
        String zoneId,

        @Schema(description = "Identifiant de la station de mesure", example = "AIR-STATION-042")
        String stationId,

        @Schema(description = "Polluant mesuré", example = "NO2")
        Pollutant pollutant,

        @Schema(description = "Valeur mesurée", example = "220.5")
        double value

) {
}