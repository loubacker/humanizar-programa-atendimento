package com.humanizar.programaatendimento.application.outbound.dto;

import java.util.UUID;

public record ProgramaDeletedCommandDTO(
        UUID patientId) {
}
