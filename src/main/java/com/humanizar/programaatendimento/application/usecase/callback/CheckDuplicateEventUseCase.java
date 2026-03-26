package com.humanizar.programaatendimento.application.usecase.callback;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.humanizar.programaatendimento.infrastructure.messaging.inbound.idempotency.ProcessedEventGuard;

@Service
public class CheckDuplicateEventUseCase {

    private final ProcessedEventGuard processedEventGuard;

    public CheckDuplicateEventUseCase(ProcessedEventGuard processedEventGuard) {
        this.processedEventGuard = processedEventGuard;
    }

    public void execute(String consumerName, UUID eventId, String correlationId) {
        processedEventGuard.ensureNotProcessed(consumerName, eventId, correlationId);
    }
}
