package com.urbanhub.quality.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Quality", description = "Supervision du service qualité")
public class QualityStatusController {

    @GetMapping("/api/quality/status")
    @Operation(summary = "Consulter le statut du service qualité")
    public QualityStatusResponse status() {
        return new QualityStatusResponse("UP", "quality-service");
    }
}