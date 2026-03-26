package com.humanizar.programaatendimento.application.inbound.mapper;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaDeleteDTO;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;

@Component
public class InboundProgramaAtendimentoMapper {

    public ProgramaAtendimentoDTO toCreatePayload(ProgramaAtendimentoDTO payload) {
        return validateProgramaPayload(payload, null);
    }

    public ProgramaAtendimentoDTO toCreatePayload(ProgramaAtendimentoDTO payload, String correlationId) {
        return validateProgramaPayload(payload, correlationId);
    }

    public ProgramaAtendimentoDTO toUpdatePayload(ProgramaAtendimentoDTO payload) {
        return validateProgramaPayload(payload, null);
    }

    public ProgramaAtendimentoDTO toUpdatePayload(ProgramaAtendimentoDTO payload, String correlationId) {
        return validateProgramaPayload(payload, correlationId);
    }

    public ProgramaDeleteDTO toDeletePayload(ProgramaDeleteDTO deleteDTO) {
        return requireField(deleteDTO, "payload", null);
    }

    public ProgramaAtendimentoDTO validateProgramaPayload(
            ProgramaAtendimentoDTO payload,
            String correlationId) {
        ProgramaAtendimentoDTO safePayload = requireField(payload, "programaAtendimento", correlationId);
        requireField(safePayload.patientId(), "programaAtendimento.patientId", correlationId);
        requireText(safePayload.dataInicio(), "programaAtendimento.dataInicio", correlationId);
        requireField(safePayload.nucleoPatient(), "programaAtendimento.nucleoPatient", correlationId);

        if (safePayload.nucleoPatient().isEmpty()) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.INBOUND_EMPTY_COLLECTION,
                    correlationId,
                    "Colecao obrigatoria vazia: programaAtendimento.nucleoPatient");
        }

        return safePayload;
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
