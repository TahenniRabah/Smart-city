package com.urbanhub.ingestion.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(description = "Mesure brute envoyée par une passerelle IoT")
public record MeasurementIngestionRequest(

        @NotBlank(message = "zoneId is required")
        @Size(max = 50, message = "zoneId must not exceed 50 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9_-]+$",
                message = "zoneId contains invalid characters"
        )
        @Schema(
                description = "Identifiant de la zone",
                example = "ZFE-1",
                maxLength = 50
        )
        String zoneId,

        @NotBlank(message = "stationId is required")
        @Size(max = 100, message = "stationId must not exceed 100 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9_-]+$",
                message = "stationId contains invalid characters"
        )
        @Schema(
                description = "Identifiant de la station",
                example = "AIR-STATION-042",
                maxLength = 100
        )
        String stationId,

        @NotBlank(message = "indicator is required")
        @Pattern(
                regexp = "^(NO2|PM10|PM25)$",
                message = "indicator must be one of: NO2, PM10, PM25"
        )
        @Schema(
                description = "Indicateur mesuré",
                example = "NO2",
                allowableValues = {"NO2", "PM10", "PM25"}
        )
        String indicator,

        @NotNull(message = "value is required")
        @DecimalMin(
                value = "0.0",
                inclusive = true,
                message = "value must be greater than or equal to 0"
        )
        @DecimalMax(
                value = "5000.0",
                inclusive = true,
                message = "value must be less than or equal to 5000"
        )
        @Schema(
                description = "Valeur mesurée",
                example = "220.5",
                minimum = "0",
                maximum = "5000"
        )
        Double value,

        @NotNull(message = "timestamp is required")
        @PastOrPresent(message = "timestamp must not be in the future")
        @Schema(
                description = "Timestamp de la mesure brute",
                example = "2026-05-06T14:29:58Z"
        )
        Instant timestamp
) {
}
