package com.humanizar.programaatendimento.application.inbound.dto.nucleo;

import java.util.UUID;

public record AbordagemPatientDTO(
        UUID nucleoPatientId,
        UUID abordagemId) {
}
