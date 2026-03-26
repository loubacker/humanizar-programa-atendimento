package com.humanizar.programaatendimento.application.inbound.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.application.inbound.dto.InboundEnvelopeDTO;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;

@Component
public class InboundEnvelopeMapper {

    public <T> InboundEnvelopeDTO<T> validate(InboundEnvelopeDTO<T> envelop) {
        requireField(envelop, "envelop", null);

        UUID correlationId = requireField(envelop.correlationId(), "envelop.correlationId", null);
        String correlationIdAsString = correlationId.toString();

        requireText(envelop.producerService(), "envelop.producerService", correlationIdAsString);
        requireField(envelop.occurredAt(), "envelop.occurredAt", correlationIdAsString);
        requireField(envelop.actorId(), "envelop.actorId", correlationIdAsString);
        requireText(envelop.userAgent(), "envelop.userAgent", correlationIdAsString);
        requireText(envelop.originIp(), "envelop.originIp", correlationIdAsString);
        requireField(envelop.payload(), "envelop.payload", correlationIdAsString);

        return envelop;
    }

    public String correlationIdAsString(InboundEnvelopeDTO<?> envelop) {
        if (envelop == null || envelop.correlationId() == null) {
            return null;
        }
        return envelop.correlationId().toString();
    }

    public <T> InboundEnvelopeDTO<T> toInboundCommand(InboundEnvelopeDTO<T> inboundEnvelopeDTO) {
        return validate(inboundEnvelopeDTO);
    }

    private String requireText(String value, String field, String correlationId) {
        if (value == null || value.isBlank()) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.INBOUND_REQUIRED_FIELD,
                    correlationId,
                    "Campo obrigatorio ausente: " + field);
        }
        return value;
    }

    private <T> T requireField(T value, String field, String correlationId) {
        if (value == null) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.INBOUND_REQUIRED_FIELD,
                    correlationId,
                    "Campo obrigatorio ausente: " + field);
        }
        return value;
    }
}
