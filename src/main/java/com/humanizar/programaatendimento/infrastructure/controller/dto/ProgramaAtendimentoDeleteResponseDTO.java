package com.humanizar.programaatendimento.infrastructure.controller.dto;

import java.util.UUID;

public record ProgramaAtendimentoDeleteResponseDTO(
        String status,
        String operation,
        UUID patientId) {
}
