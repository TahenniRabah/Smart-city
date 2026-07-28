package com.urbanhub.ingestion.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Erreur retournée par l’API")
public record ApiErrorResponse(

        @Schema(example = "2026-07-28T08:30:00Z")
        Instant timestamp,

        @Schema(example = "400")
        int status,

        @Schema(example = "VALIDATION_ERROR")
        String error,

        @Schema(example = "The request contains invalid fields")
        String message,

        @Schema(example = "/api/ingestion/measurements")
        String path,

        Map<String, String> fieldErrors
) {
}