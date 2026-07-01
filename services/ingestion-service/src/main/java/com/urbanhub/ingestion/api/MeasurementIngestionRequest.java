package com.urbanhub.ingestion.api;


import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Mesure brute envoyée par une passerelle IoT")
public record MeasurementIngestionRequest(

        @Schema(description = "Identifiant de la zone", example = "ZFE-1")
        String zoneId,

        @Schema(description = "Identifiant de la station", example = "AIR-STATION-042")
        String stationId,

        @Schema(description = "Indicateur mesuré", example = "NO2")
        String indicator,

        @Schema(description = "Valeur mesurée", example = "220.5")
        double value,

        @Schema(description = "Timestamp de la mesure brute", example = "2026-05-06T14:29:58Z")
        Instant timestamp
) {
}
