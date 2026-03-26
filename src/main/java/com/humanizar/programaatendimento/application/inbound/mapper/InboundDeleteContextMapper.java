package com.humanizar.programaatendimento.application.inbound.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.application.inbound.dto.InboundDeleteContextDTO;
import com.humanizar.programaatendimento.application.inbound.dto.InboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaDeleteDTO;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;

@Component
public class InboundDeleteContextMapper {

    private final InboundEnvelopeMapper inboundEnvelopeMapper;
    private final InboundProgramaAtendimentoMapper inboundProgramaAtendimentoMapper;

    public InboundDeleteContextMapper(
            InboundEnvelopeMapper inboundEnvelopeMapper,
            InboundProgramaAtendimentoMapper inboundProgramaAtendimentoMapper) {
        this.inboundEnvelopeMapper = inboundEnvelopeMapper;
        this.inboundProgramaAtendimentoMapper = inboundProgramaAtendimentoMapper;
    }

    public InboundDeleteContextDTO fromDelete(
            UUID pathPatientId,
            InboundEnvelopeDTO<ProgramaDeleteDTO> envelop) {
        requireField(pathPatientId, "path.patientId", null);

        InboundEnvelopeDTO<ProgramaDeleteDTO> validatedEnvelope = inboundEnvelopeMapper.validate(envelop);
        String correlationId = inboundEnvelopeMapper.correlationIdAsString(validatedEnvelope);

        ProgramaDeleteDTO payload = inboundProgramaAtendimentoMapper.toDeletePayload(validatedEnvelope.payload());
        UUID payloadPatientId = requireField(payload.patientId(), "context.payload.patientId", correlationId);

        if (!pathPatientId.equals(payloadPatientId)) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.INBOUND_PATIENT_MISMATCH,
                    correlationId,
                    "path.patientId diverge de payload.patientId");
        }

        return new InboundDeleteContextDTO(validatedEnvelope, payload);
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
