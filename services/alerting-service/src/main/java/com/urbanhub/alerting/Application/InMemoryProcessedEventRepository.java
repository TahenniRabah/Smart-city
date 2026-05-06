package com.urbanhub.alerting.Application;

import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryProcessedEventRepository implements ProcessedEventRepository {

    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    @Override
    public boolean hasAlreadyBeenProcessed(String eventId) {
        return processedEventIds.contains(eventId);
    }

    @Override
    public void markAsProcessed(String eventId) {
        processedEventIds.add(eventId);
    }
}