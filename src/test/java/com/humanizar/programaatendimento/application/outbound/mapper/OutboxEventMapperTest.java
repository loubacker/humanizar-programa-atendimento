package com.humanizar.programaatendimento.application.outbound.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.programaatendimento.application.catalog.ExchangeCatalog;
import com.humanizar.programaatendimento.application.catalog.RoutingKeyCatalog;
import com.humanizar.programaatendimento.application.outbound.dto.CallbackDTO;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.OutboxEvent;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;

class OutboxEventMapperTest {

    private final OutboxEventMapper mapper = new OutboxEventMapper(new ObjectMapper().findAndRegisterModules());

    @Test
    void shouldUseProvidedExchangeAndRouting() {
        UUID aggregateId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();

        OutboxEvent event = mapper.toOutboxEvent(
                ExchangeCatalog.ACOLHIMENTO_EVENT,
                RoutingKeyCatalog.ACOLHIMENTO_PROGRAMA_PROCESSED_V1,
                "acolhimento",
                aggregateId,
                eventId,
                correlationId,
                Map.of("status", "PROCESSED"),
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1");

        assertEquals(ExchangeCatalog.ACOLHIMENTO_EVENT, event.getExchangeName());
        assertEquals(RoutingKeyCatalog.ACOLHIMENTO_PROGRAMA_PROCESSED_V1, event.getRoutingKey());
        assertEquals("humanizar-programa-atendimento", event.getProducerService());
        assertNotNull(event.getPayload());
    }

    @Test
    void shouldFailFastWhenCorrelationIdIsMissing() {
        ProgramaAtendimentoException ex = assertThrows(
                ProgramaAtendimentoException.class,
                () -> mapper.toOutboxEvent(
                        ExchangeCatalog.ACOLHIMENTO_EVENT,
                        RoutingKeyCatalog.ACOLHIMENTO_PROGRAMA_REJECTED_V1,
                        "acolhimento",
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        Map.of("status", "REJECTED"),
                        UUID.randomUUID(),
                        "JUnit",
                        "127.0.0.1"));

        assertEquals(ReasonCode.VALIDATION_ERROR, ex.getReasonCode());
    }

    @Test
    void shouldSerializeCallbackWithMetadata() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        CallbackDTO callback = new CallbackDTO(
                RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1,
                eventId,
                correlationId,
                "humanizar-programa-atendimento",
                ExchangeCatalog.ACOLHIMENTO_EVENT,
                RoutingKeyCatalog.ACOLHIMENTO_PROGRAMA_PROCESSED_V1,
                "acolhimento",
                aggregateId,
                1,
                LocalDateTime.now(),
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1",
                "PROCESSED",
                null,
                null,
                LocalDateTime.now(),
                null);

        OutboxEvent outboxEvent = mapper.toOutboxEvent(
                ExchangeCatalog.ACOLHIMENTO_EVENT,
                RoutingKeyCatalog.ACOLHIMENTO_PROGRAMA_PROCESSED_V1,
                "acolhimento",
                aggregateId,
                eventId,
                correlationId,
                callback,
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1");

        @SuppressWarnings("unchecked")
        Map<String, Object> serialized = new ObjectMapper().readValue(outboxEvent.getPayload(), Map.class);
        assertEquals(eventId.toString(), serialized.get("eventId"));
        assertEquals(correlationId.toString(), serialized.get("correlationId"));
        assertEquals("PROCESSED", serialized.get("status"));
    }
}
