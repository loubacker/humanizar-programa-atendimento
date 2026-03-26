package com.humanizar.programaatendimento.infrastructure.controller.dto;

import java.util.UUID;

public record ProgramaAtendimentoCreateResponseDTO(
        String message,
        UUID patientId,
        UUID correlationId) {
}
