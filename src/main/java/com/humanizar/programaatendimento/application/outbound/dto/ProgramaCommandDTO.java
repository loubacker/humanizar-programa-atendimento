package com.humanizar.programaatendimento.application.outbound.dto;

import java.util.List;
import java.util.UUID;

public record ProgramaCommandDTO(
        UUID nucleoPatientId,
        List<UUID> abordagemId) {

    public ProgramaCommandDTO {
        abordagemId = abordagemId == null ? List.of() : List.copyOf(abordagemId);
    }
}
