package com.humanizar.programaatendimento.application.outbound.publisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.humanizar.programaatendimento.application.catalog.ExchangeCatalog;
import com.humanizar.programaatendimento.application.catalog.RoutingKeyCatalog;
import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.messaging.handler.EventOutcome;
import com.humanizar.programaatendimento.application.outbound.dto.CallbackDTO;
import com.humanizar.programaatendimento.application.outbound.mapper.OutboundCallbackMapper;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;

@ExtendWith(MockitoExtension.class)
class ProcessingResultPublisherTest {

    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    @Captor
    private ArgumentCaptor<Object> payloadCaptor;

    private ProcessingResultPublisher processingResultPublisher;

    @BeforeEach
    void setUp() {
        processingResultPublisher = new ProcessingResultPublisher(
                outboxEventPublisher,
                new OutboundCallbackMapper());
    }

    @Test
    void shouldPublishProcessedForAcolhimentoRouting() {
        OutboundEnvelopeDTO<Object> envelope = envelope(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1);

        processingResultPublisher.publishProcessed(envelope, RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1);

        verify(outboxEventPublisher).publish(
                eq(ExchangeCatalog.ACOLHIMENTO_EVENT),
                eq(RoutingKeyCatalog.ACOLHIMENTO_PROGRAMA_PROCESSED_V1),
                eq("acolhimento"),
                eq(envelope.aggregateId()),
                eq(envelope.eventId()),
                eq(envelope.correlationId()),
                payloadCaptor.capture(),
                eq(envelope.actorId()),
                eq(envelope.userAgent()),
                eq(envelope.originIp()));

        CallbackDTO payload = (CallbackDTO) payloadCaptor.getValue();
        assertNotNull(payload);
        assertEquals("PROCESSED", payload.status());
        assertEquals(ExchangeCatalog.ACOLHIMENTO_EVENT, payload.exchangeName());
    }

    @Test
    void shouldPublishRejectedForAcolhimentoRouting() {
        OutboundEnvelopeDTO<Object> envelope = envelope(RoutingKeyCatalog.ACOLHIMENTO_UPDATED_V1);
        EventOutcome outcome = EventOutcome.failed(ReasonCode.VALIDATION_ERROR);

        processingResultPublisher.publishRejected(envelope, RoutingKeyCatalog.ACOLHIMENTO_UPDATED_V1, outcome);

        verify(outboxEventPublisher).publish(
                eq(ExchangeCatalog.ACOLHIMENTO_EVENT),
                eq(RoutingKeyCatalog.ACOLHIMENTO_PROGRAMA_REJECTED_V1),
                eq("acolhimento"),
                eq(envelope.aggregateId()),
                eq(envelope.eventId()),
                eq(envelope.correlationId()),
                payloadCaptor.capture(),
                eq(envelope.actorId()),
                eq(envelope.userAgent()),
                eq(envelope.originIp()));

        CallbackDTO payload = (CallbackDTO) payloadCaptor.getValue();
        assertNotNull(payload);
        assertEquals("REJECTED", payload.status());
        assertEquals("VALIDATION_ERROR", payload.reasonCode());
    }

    private OutboundEnvelopeDTO<Object> envelope(String routingKey) {
        return new OutboundEnvelopeDTO<>(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "humanizar-acolhimento",
                ExchangeCatalog.ACOLHIMENTO_COMMAND,
                routingKey,
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
