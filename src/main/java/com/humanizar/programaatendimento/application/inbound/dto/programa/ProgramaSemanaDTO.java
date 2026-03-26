package com.humanizar.programaatendimento.application.inbound.dto.programa;

import java.util.List;

public record ProgramaSemanaDTO(
        String diaSemana,
        List<ProgramaSemanaScheduleDTO> programaSemanaSchedule) {

    public ProgramaSemanaDTO {
        programaSemanaSchedule = programaSemanaSchedule == null ? List.of() : List.copyOf(programaSemanaSchedule);
    }
}
