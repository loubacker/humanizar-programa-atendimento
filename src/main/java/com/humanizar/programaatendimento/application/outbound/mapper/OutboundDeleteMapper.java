package com.humanizar.programaatendimento.application.outbound.mapper;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.application.catalog.ExchangeCatalog;
import com.humanizar.programaatendimento.application.catalog.RoutingKeyCatalog;
import com.humanizar.programaatendimento.application.inbound.dto.InboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaDeleteDTO;
import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.outbound.dto.ProgramaDeletedCommandDTO;

@Component
public class OutboundDeleteMapper {

    private static final String PRODUCER_SERVICE = "humanizar-programa-atendimento";
    private static final String AGGREGATE_TYPE = "programa-atendimento";
    private static final short EVENT_VERSION = 1;

    public OutboundEnvelopeDTO<ProgramaDeletedCommandDTO> toDeleteCommandEnvelope(
            InboundEnvelopeDTO<ProgramaDeleteDTO> inboundEnvelope,
            UUID eventId,
            UUID aggregateId) {
        Objects.requireNonNull(inboundEnvelope, "inboundEnvelope é obrigatório");
        Objects.requireNonNull(inboundEnvelope.payload(), "inboundEnvelope.payload é obrigatório");
        Objects.requireNonNull(inboundEnvelope.payload().patientId(),
                "inboundEnvelope.payload.patientId é obrigatório");
        Objects.requireNonNull(eventId, "eventId é obrigatório");
        Objects.requireNonNull(aggregateId, "aggregateId é obrigatório");

        ProgramaDeletedCommandDTO commandPayload = new ProgramaDeletedCommandDTO(
                inboundEnvelope.payload().patientId());

        return new OutboundEnvelopeDTO<>(
                eventId,
                inboundEnvelope.correlationId(),
                PRODUCER_SERVICE,
                ExchangeCatalog.PROGRAMA_COMMAND,
                RoutingKeyCatalog.PROGRAMA_DELETED_V1,
                AGGREGATE_TYPE,
                aggregateId,
                EVENT_VERSION,
                LocalDateTime.now(),
                inboundEnvelope.actorId(),
                inboundEnvelope.userAgent(),
                inboundEnvelope.originIp(),
                commandPayload);
    }
}
