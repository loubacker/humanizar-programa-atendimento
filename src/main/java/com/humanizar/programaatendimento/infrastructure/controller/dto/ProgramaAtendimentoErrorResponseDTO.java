package com.humanizar.programaatendimento.infrastructure.controller.dto;

import java.time.OffsetDateTime;

public record ProgramaAtendimentoErrorResponseDTO(
        int status,
        String reasonCode,
        String message,
        String correlationId,
        String path,
        OffsetDateTime timestamp) {
}
