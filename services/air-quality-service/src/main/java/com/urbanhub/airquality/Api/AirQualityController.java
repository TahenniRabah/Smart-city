package com.urbanhub.airquality.Api;

import com.urbanhub.airquality.Application.AirQualityAnalysisService;
import com.urbanhub.airquality.Domain.AirQualityMeasurement;
import com.urbanhub.airquality.Domain.AlertLevel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/air-quality")
public class AirQualityController {

    private final AirQualityAnalysisService analysisService;

    public AirQualityController(AirQualityAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/measurements")
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