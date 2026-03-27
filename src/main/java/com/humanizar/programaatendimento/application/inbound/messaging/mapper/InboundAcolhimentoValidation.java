package com.humanizar.programaatendimento.application.inbound.messaging.mapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.humanizar.programaatendimento.application.inbound.dto.messaging.AcolhimentoNucleoPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoResponsavelDTO;
import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.enums.ResponsavelRole;

public final class InboundAcolhimentoValidation {

    private InboundAcolhimentoValidation() {
    }

    public static String correlationIdAsString(OutboundEnvelopeDTO<?> envelope) {
        if (envelope == null || envelope.correlationId() == null) {
            return null;
        }
        return envelope.correlationId().toString();
    }

    public static void requireField(Object value, String fieldName, String correlationId) {
        if (value == null) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.INBOUND_REQUIRED_FIELD,
                    correlationId,
                    "Campo obrigatorio ausente: " + fieldName);
        }
    }

    public static void requireNotBlank(String value, String fieldName, String correlationId) {
        if (value == null || value.isBlank()) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.INBOUND_REQUIRED_FIELD,
                    correlationId,
                    "Campo obrigatorio ausente: " + fieldName);
        }
    }

    public static void requireNotEmptyCollection(List<?> value, String fieldName, String correlationId) {
        if (value == null || value.isEmpty()) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.INBOUND_EMPTY_COLLECTION,
                    correlationId,
                    "Colecao obrigatoria vazia: " + fieldName);
        }
    }

    public static void validateNucleoPatients(List<AcolhimentoNucleoPatientDTO> nucleoPatient, String correlationId) {
        requireNotEmptyCollection(nucleoPatient, "payload.nucleoPatient", correlationId);

        Set<UUID> uniqueIds = new HashSet<>();
        for (int i = 0; i < nucleoPatient.size(); i++) {
            AcolhimentoNucleoPatientDTO entry = nucleoPatient.get(i);
            requireField(entry, "payload.nucleoPatient[" + i + "]", correlationId);
            requireField(entry.nucleoPatientId(), "payload.nucleoPatient[" + i + "].nucleoPatientId", correlationId);
            requireField(entry.nucleoId(), "payload.nucleoPatient[" + i + "].nucleoId", correlationId);

            if (!uniqueIds.add(entry.nucleoPatientId())) {
                throw new ProgramaAtendimentoException(
                        ReasonCode.INBOUND_DUPLICATE_ITEM,
                        correlationId,
                        "Item duplicado: payload.nucleoPatient.nucleoPatientId=" + entry.nucleoPatientId());
            }

            validateResponsaveis(entry.nucleoPatientResponsavel(), i, correlationId);
        }
    }

    public static void validateResponsaveis(List<NucleoResponsavelDTO> responsaveis, int nucleoIndex,
            String correlationId) {
        String fieldBase = "payload.nucleoPatient[" + nucleoIndex + "].nucleoPatientResponsavel";
        requireNotEmptyCollection(responsaveis, fieldBase, correlationId);

        for (int i = 0; i < responsaveis.size(); i++) {
            NucleoResponsavelDTO responsavel = responsaveis.get(i);
            requireField(responsavel, fieldBase + "[" + i + "]", correlationId);
            requireField(responsavel.responsavelId(), fieldBase + "[" + i + "].responsavelId", correlationId);
            requireNotBlank(responsavel.role(), fieldBase + "[" + i + "].role", correlationId);
            validateRole(responsavel.role(), fieldBase + "[" + i + "].role", correlationId);
        }
    }

    public static void validateRole(String role, String fieldName, String correlationId) {
        try {
            ResponsavelRole.valueOf(role);
        } catch (IllegalArgumentException ex) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.INBOUND_INVALID_ENUM,
                    correlationId,
                    "Campo enum invalido: " + fieldName + "=" + role);
        }
    }
}
