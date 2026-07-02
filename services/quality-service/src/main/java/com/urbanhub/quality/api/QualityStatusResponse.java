package com.urbanhub.quality.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Statut du service qualité")
public record QualityStatusResponse(
        @Schema(example = "UP")
        String status,

        @Schema(example = "quality-service")
        String service
) {
}