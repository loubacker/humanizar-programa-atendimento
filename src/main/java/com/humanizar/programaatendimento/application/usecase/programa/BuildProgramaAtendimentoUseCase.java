package com.humanizar.programaatendimento.application.usecase.programa;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaAtendimento;

@Service
public class BuildProgramaAtendimentoUseCase {

    public ProgramaAtendimento buildForCreate(
            UUID patientId, ProgramaAtendimentoDTO payload, String correlationId) {
        return ProgramaAtendimento.builder()
                .id(null)
                .patientId(patientId)
                .dataInicio(parseDateTime(payload.dataInicio(), correlationId))
                .cadastroApp(payload.cadastroApp())
                .atEscolar(payload.atEscolar())
                .observacao(payload.observacao())
                .build();
    }

    public ProgramaAtendimento buildForUpdate(
            UUID programaId, UUID patientId, ProgramaAtendimentoDTO payload, String correlationId) {
        return ProgramaAtendimento.builder()
                .id(programaId)
                .patientId(patientId)
                .dataInicio(parseDateTime(payload.dataInicio(), correlationId))
                .cadastroApp(payload.cadastroApp())
                .atEscolar(payload.atEscolar())
                .observacao(payload.observacao())
                .build();
    }

    private LocalDateTime parseDateTime(String value, String correlationId) {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.INBOUND_INVALID_DATETIME, correlationId,
                    "Formato de dataInicio invalido: " + value);
        }
    }
}
