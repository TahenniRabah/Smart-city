package com.urbanhub.airquality.Api;

import com.urbanhub.airquality.application.AirQualityAnalysisService;
import com.urbanhub.airquality.Domain.AirQualityMeasurement;
import com.urbanhub.airquality.Domain.AlertLevel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/air-quality")
@Tag(name = "Air Quality", description = "Analyse des mesures de qualité de l'air")
public class AirQualityController {

    private final AirQualityAnalysisService analysisService;

    public AirQualityController(AirQualityAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/measurements")
    @Operation(
            summary = "Analyser une mesure de qualité de l'air",
            description = "Calcule le niveau d'alerte d'une mesure NO2 ou PM10 et publie un événement Kafka si le niveau est WARNING ou CRITICAL."
    )
    public AirQualityAlertResponse analyzeMeasurement(
            @RequestBody AirQualityMeasurementRequest request
    ) {
        AirQualityMeasurement measurement = new AirQualityMeasurement(
                request.zoneId(),
                request.stationId(),
                request.pollutant(),
                request.value()
        );

        AlertLevel alertLevel = analysisService.analyze(measurement);

        return new AirQualityAlertResponse(alertLevel);
    }
}