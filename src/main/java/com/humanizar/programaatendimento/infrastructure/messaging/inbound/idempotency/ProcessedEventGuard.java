package com.humanizar.programaatendimento.infrastructure.messaging.inbound.idempotency;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.port.ProcessedEventPort;

@Component
public class ProcessedEventGuard {

    private final ProcessedEventPort processedEventPort;

    public ProcessedEventGuard(ProcessedEventPort processedEventPort) {
        this.processedEventPort = processedEventPort;
    }

    public void ensureNotProcessed(String consumerName, UUID eventId, String correlationId) {
        if (processedEventPort.existsByConsumerNameAndEventId(consumerName, eventId)) {
            throw new ProgramaAtendimentoException(ReasonCode.DUPLICATE_EVENT, correlationId);
        }
    }
}
