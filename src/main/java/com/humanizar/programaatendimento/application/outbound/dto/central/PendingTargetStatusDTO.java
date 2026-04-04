package com.humanizar.programaatendimento.application.outbound.dto.central;

public record PendingTargetStatusDTO(
        String targetService,
        String status) {
}
