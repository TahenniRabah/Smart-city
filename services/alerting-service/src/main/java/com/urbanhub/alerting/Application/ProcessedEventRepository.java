package com.urbanhub.alerting.Application;


public interface ProcessedEventRepository {

    boolean hasAlreadyBeenProcessed(String eventId);

    void markAsProcessed(String eventId);
}

