package com.urbanhub.ingestion.api;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse d'acceptation d'une mesure brute")
public record MeasurementIngestionResponse(

        @Schema(description = "Statut de prise en charge", example = "ACCEPTED")
        String status,

        @Schema(description = "Identifiant de corrélation de bout en bout", example = "corr-12345")
        String correlationId
) {
}

