package com.urbanhub.alerting.api;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Statut du microservice Alerting")
public record AlertingStatusResponse(

        @Schema(description = "Statut du service", example = "UP")
        String status,

        @Schema(description = "Nom du service", example = "alerting-service")
        String service
) {
}

