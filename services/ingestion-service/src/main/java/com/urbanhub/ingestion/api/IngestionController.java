package com.urbanhub.ingestion.api;


import com.urbanhub.ingestion.application.IngestionResult;
import com.urbanhub.ingestion.application.MeasurementIngestionService;
import com.urbanhub.ingestion.application.RawMeasurementCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/ingestion")
@Tag(name = "Ingestion", description = "Réception des mesures brutes IoT")
public class IngestionController {

    private final MeasurementIngestionService ingestionService;

    public IngestionController(MeasurementIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/measurements")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
            summary = "Recevoir une mesure brute",
            description = "Reçoit une mesure brute depuis une passerelle IoT, génère un correlationId et publie un événement MeasurementReceived."
    )
    public MeasurementIngestionResponse ingest(@RequestBody MeasurementIngestionRequest request) {
        RawMeasurementCommand command = new RawMeasurementCommand(
                request.zoneId(),
                request.stationId(),
                request.indicator(),
                request.value(),
                request.timestamp()
        );

        IngestionResult result = ingestionService.ingest(command);

        return new MeasurementIngestionResponse(
                result.status(),
                result.correlationId()
        );
    }
}
