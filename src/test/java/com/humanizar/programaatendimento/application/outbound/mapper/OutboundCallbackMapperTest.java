package com.humanizar.programaatendimento.application.outbound.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.humanizar.programaatendimento.application.catalog.ExchangeCatalog;
import com.humanizar.programaatendimento.application.catalog.RoutingKeyCatalog;
import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.outbound.dto.CallbackDTO;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;

class OutboundCallbackMapperTest {

    private final OutboundCallbackMapper mapper = new OutboundCallbackMapper();

    @Test
    void shouldMapProcessedCallbackWithTraceMetadata() {
        OutboundEnvelopeDTO<Object> inbound = inboundEnvelope(UUID.randomUUID(), UUID.randomUUID());

        CallbackDTO callback = mapper.toProcessedCallback(
                inbound,
                RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1,
                ExchangeCatalog.ACOLHIMENTO_EVENT,
                RoutingKeyCatalog.ACOLHIMENTO_PROGRAMA_PROCESSED_V1);

        assertEquals(inbound.eventId(), callback.eventId());
        assertEquals(inbound.correlationId(), callback.correlationId());
        assertEquals("humanizar-programa-atendimento", callback.producerService());
        assertEquals("PROCESSED", callback.status());
        assertNotNull(callback.occurredAt());
        assertNotNull(callback.processedAt());
    }

    @Test
    void shouldFailFastWhenInboundEventIdIsMissing() {
        OutboundEnvelopeDTO<Object> inbound = inboundEnvelope(null, UUID.randomUUID());

        ProgramaAtendimentoException ex = assertThrows(
                ProgramaAtendimentoException.class,
                () -> mapper.toProcessedCallback(
                        inbound,
                        RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1,
                        ExchangeCatalog.ACOLHIMENTO_EVENT,
                        RoutingKeyCatalog.ACOLHIMENTO_PROGRAMA_PROCESSED_V1));

        assertEquals(ReasonCode.VALIDATION_ERROR, ex.getReasonCode());
    }

    private OutboundEnvelopeDTO<Object> inboundEnvelope(UUID eventId, UUID correlationId) {
        return new OutboundEnvelopeDTO<>(
                eventId,
                correlationId,
                "humanizar-acolhimento",
                ExchangeCatalog.ACOLHIMENTO_COMMAND,
                RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1,
                "acolhimento",
                UUID.randomUUID(),
                1,
                LocalDateTime.now(),
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1",
                new Object());
    }
}
