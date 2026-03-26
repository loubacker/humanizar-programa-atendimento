package com.humanizar.programaatendimento.application.inbound.dto.programa;

import java.util.List;

public record AtEscolaSemanaDTO(
        String diaSemana,
        List<AtEscolaSemanaScheduleDTO> atEscolaSemanaSchedule) {

    public AtEscolaSemanaDTO {
        atEscolaSemanaSchedule = atEscolaSemanaSchedule == null ? List.of() : List.copyOf(atEscolaSemanaSchedule);
    }
}
