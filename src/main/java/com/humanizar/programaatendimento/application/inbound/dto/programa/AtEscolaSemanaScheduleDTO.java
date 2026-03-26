package com.humanizar.programaatendimento.application.inbound.dto.programa;

import java.util.UUID;

public record AtEscolaSemanaScheduleDTO(
        UUID nucleoId,
        String horarioInicio,
        String horarioTermino,
        String turno) {
}
