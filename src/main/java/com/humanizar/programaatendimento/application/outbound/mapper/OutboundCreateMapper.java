package com.humanizar.programaatendimento.application.outbound.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.application.catalog.ExchangeCatalog;
import com.humanizar.programaatendimento.application.catalog.RoutingKeyCatalog;
import com.humanizar.programaatendimento.application.inbound.dto.InboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.outbound.dto.ProgramaCommandDTO;

@Component
public class OutboundCreateMapper {

    private static final String PRODUCER_SERVICE = "humanizar-programa-atendimento";
    private static final String AGGREGATE_TYPE = "programa-atendimento";
    private static final short EVENT_VERSION = 1;

    public OutboundEnvelopeDTO<List<ProgramaCommandDTO>> toCreateCommandEnvelope(
            InboundEnvelopeDTO<ProgramaAtendimentoDTO> inboundEnvelope,
            UUID eventId,
            UUID aggregateId,
            List<ProgramaCommandDTO> commandPayload) {
        Objects.requireNonNull(inboundEnvelope, "inboundEnvelope é obrigatório");
        Objects.requireNonNull(eventId, "eventId é obrigatório");
        Objects.requireNonNull(aggregateId, "aggregateId é obrigatório");
        Objects.requireNonNull(commandPayload, "commandPayload é obrigatório");

        return new OutboundEnvelopeDTO<>(
                eventId,
                inboundEnvelope.correlationId(),
                PRODUCER_SERVICE,
                ExchangeCatalog.PROGRAMA_COMMAND,
                RoutingKeyCatalog.PROGRAMA_CREATED_V1,
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
