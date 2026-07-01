package com.urbanhub.ingestion.application;


public record IngestionResult(
        String status,
        String correlationId
) {
}

