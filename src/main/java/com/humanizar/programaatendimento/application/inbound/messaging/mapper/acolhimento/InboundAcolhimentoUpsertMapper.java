package com.humanizar.programaatendimento.application.inbound.messaging.mapper.acolhimento;

import static com.humanizar.programaatendimento.application.inbound.messaging.mapper.InboundAcolhimentoValidation.correlationIdAsString;
import static com.humanizar.programaatendimento.application.inbound.messaging.mapper.InboundAcolhimentoValidation.requireField;
import static com.humanizar.programaatendimento.application.inbound.messaging.mapper.InboundAcolhimentoValidation.validateNucleoPatients;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.programaatendimento.application.inbound.dto.messaging.AcolhimentoUpsertDTO;
import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;

@Component
public class InboundAcolhimentoUpsertMapper {

    private final ObjectMapper objectMapper;

    public InboundAcolhimentoUpsertMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AcolhimentoUpsertDTO toPayload(OutboundEnvelopeDTO<Object> envelope) {
        String correlationId = correlationIdAsString(envelope);
        AcolhimentoUpsertDTO payload;

        try {
            payload = objectMapper.convertValue(envelope.payload(), AcolhimentoUpsertDTO.class);
        } catch (IllegalArgumentException ex) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.INBOUND_PARSE_ERROR,
                    correlationId,
                    "Falha ao mapear payload de acolhimento upsert");
        }

        requireField(payload, "payload", correlationId);
        requireField(payload.patientId(), "payload.patientId", correlationId);
        validateNucleoPatients(payload.nucleoPatient(), correlationId);
        return payload;
    }
}
