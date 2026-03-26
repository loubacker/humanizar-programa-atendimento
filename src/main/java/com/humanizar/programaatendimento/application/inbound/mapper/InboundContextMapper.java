package com.humanizar.programaatendimento.application.inbound.mapper;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.application.inbound.dto.InboundContextDTO;
import com.humanizar.programaatendimento.application.inbound.dto.InboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;

@Component
public class InboundContextMapper {

    private final InboundEnvelopeMapper inboundEnvelopeMapper;

    public InboundContextMapper(InboundEnvelopeMapper inboundEnvelopeMapper) {
        this.inboundEnvelopeMapper = inboundEnvelopeMapper;
    }

    public InboundContextDTO<ProgramaAtendimentoDTO> normalizeAndValidate(
            InboundContextDTO<ProgramaAtendimentoDTO> context) {
        requireField(context, "context", null);

        InboundEnvelopeDTO<ProgramaAtendimentoDTO> envelop = requireField(context.envelop(), "context.envelop", null);
        InboundEnvelopeDTO<ProgramaAtendimentoDTO> validatedEnvelope = inboundEnvelopeMapper.validate(envelop);

        String correlationId = inboundEnvelopeMapper.correlationIdAsString(validatedEnvelope);
        ProgramaAtendimentoDTO contextPayload = context.payload();
        ProgramaAtendimentoDTO envelopePayload = validatedEnvelope.payload();

        if (contextPayload != null && envelopePayload != null && !Objects.equals(contextPayload, envelopePayload)) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.INBOUND_CONTEXT_INCONSISTENT,
                    correlationId,
                    "context.payload diverge de context.envelop.payload");
        }

        ProgramaAtendimentoDTO normalizedPayload = contextPayload != null ? contextPayload : envelopePayload;
        requireField(normalizedPayload, "context.payload", correlationId);

        return new InboundContextDTO<>(validatedEnvelope, normalizedPayload);
    }

    public InboundContextDTO<ProgramaAtendimentoDTO> normalizeAndValidateUpdate(
            UUID pathPatientId,
            InboundContextDTO<ProgramaAtendimentoDTO> context) {
        requireField(pathPatientId, "path.patientId", null);

        InboundContextDTO<ProgramaAtendimentoDTO> normalized = normalizeAndValidate(context);
        String correlationId = inboundEnvelopeMapper.correlationIdAsString(normalized.envelop());

        UUID payloadPatientId = requireField(
                normalized.payload().patientId(),
                "context.payload.patientId",
                correlationId);

        if (!pathPatientId.equals(payloadPatientId)) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.INBOUND_PATIENT_MISMATCH,
                    correlationId,
                    "path.patientId diverge de payload.patientId");
        }

        return normalized;
    }

    public InboundContextDTO<ProgramaAtendimentoDTO> fromEnvelop(
            InboundEnvelopeDTO<ProgramaAtendimentoDTO> envelop) {
        InboundContextDTO<ProgramaAtendimentoDTO> context = InboundContextDTO.fromEnvelop(envelop);
        return normalizeAndValidate(context);
    }

    public InboundContextDTO<ProgramaAtendimentoDTO> fromUpdate(
            UUID pathPatientId,
            InboundEnvelopeDTO<ProgramaAtendimentoDTO> envelop) {
        InboundContextDTO<ProgramaAtendimentoDTO> context = InboundContextDTO.fromEnvelop(envelop);
        return normalizeAndValidateUpdate(pathPatientId, context);
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
