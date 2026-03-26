package com.humanizar.programaatendimento.application.usecase.outbox;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.programaatendimento.application.catalog.ExchangeCatalog;
import com.humanizar.programaatendimento.application.catalog.RoutingKeyCatalog;
import com.humanizar.programaatendimento.application.inbound.dto.InboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaDeleteDTO;
import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.outbound.dto.ProgramaDeletedCommandDTO;
import com.humanizar.programaatendimento.application.outbound.mapper.OutboundDeleteMapper;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.OutboxEvent;
import com.humanizar.programaatendimento.domain.model.enums.OutboxStatus;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.port.OutboxEventPort;

@Service
public class DeleteOutboxCommandUseCase {

    private static final String PRODUCER_SERVICE = "humanizar-programa-atendimento";
    private static final String AGGREGATE_TYPE = "programa-atendimento";
    private static final short EVENT_VERSION = 1;
    private static final int DEFAULT_MAX_ATTEMPTS = 5;

    private final OutboxEventPort outboxEventPort;
    private final OutboundDeleteMapper outboundDeleteMapper;
    private final ObjectMapper objectMapper;

    public DeleteOutboxCommandUseCase(
            OutboxEventPort outboxEventPort,
            OutboundDeleteMapper outboundDeleteMapper,
            ObjectMapper objectMapper) {
        this.outboxEventPort = outboxEventPort;
        this.outboundDeleteMapper = outboundDeleteMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void execute(
            InboundEnvelopeDTO<ProgramaDeleteDTO> inboundEnvelope,
            UUID eventId,
            UUID aggregateId) {
        OutboundEnvelopeDTO<ProgramaDeletedCommandDTO> outboundEnvelope = outboundDeleteMapper
                .toDeleteCommandEnvelope(inboundEnvelope, eventId, aggregateId);

        OutboxEvent event = OutboxEvent.builder()
                .eventId(eventId)
                .correlationId(inboundEnvelope.correlationId())
                .producerService(PRODUCER_SERVICE)
                .exchangeName(ExchangeCatalog.PROGRAMA_COMMAND)
                .routingKey(RoutingKeyCatalog.PROGRAMA_DELETED_V1)
                .aggregateType(AGGREGATE_TYPE)
                .aggregateId(aggregateId)
                .eventVersion(EVENT_VERSION)
                .payload(serialize(outboundEnvelope, inboundEnvelope))
                .actorId(inboundEnvelope.actorId())
                .userAgent(inboundEnvelope.userAgent())
                .originIp(inboundEnvelope.originIp())
                .status(OutboxStatus.NEW)
                .attemptCount(0)
                .maxAttempts(DEFAULT_MAX_ATTEMPTS)
                .nextRetryAt(LocalDateTime.now())
                .build();

        outboxEventPort.save(event);
    }

    private String serialize(Object value, InboundEnvelopeDTO<ProgramaDeleteDTO> inboundEnvelope) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.PERSISTENCE_FAILURE,
                    inboundEnvelope.correlationId() != null
                            ? inboundEnvelope.correlationId().toString()
                            : null,
                    "Falha ao serializar payload de command outbox");
        }
    }
}
