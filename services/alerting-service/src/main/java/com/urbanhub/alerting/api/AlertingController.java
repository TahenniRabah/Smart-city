package com.urbanhub.alerting.api;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Alerting", description = "Supervision du microservice d'alerting")
public class AlertingController {

    @GetMapping("/api/alerting/status")
    @Operation(
            summary = "Consulter le statut du service d'alerting",
            description = "Retourne un statut simple permettant de vérifier que le microservice Alerting est disponible."
    )
    public AlertingStatusResponse status() {
        return new AlertingStatusResponse("UP", "alerting-service");
    }
}

