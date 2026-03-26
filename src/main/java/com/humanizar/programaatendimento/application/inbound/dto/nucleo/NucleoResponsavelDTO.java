package com.humanizar.programaatendimento.application.inbound.dto.nucleo;

import java.util.UUID;

public record NucleoResponsavelDTO(
        UUID responsavelId,
        String role) {
}
