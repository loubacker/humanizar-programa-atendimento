package com.humanizar.programaatendimento.domain.port.programa;

import java.util.List;
import java.util.UUID;

import com.humanizar.programaatendimento.domain.model.programa.ProgramaSemanaSchedule;

public interface ProgramaSemanaSchedulePort {

    ProgramaSemanaSchedule save(ProgramaSemanaSchedule programaSemanaSchedule);

    List<ProgramaSemanaSchedule> saveAll(List<ProgramaSemanaSchedule> programaSemanaSchedule);

    List<ProgramaSemanaSchedule> findByProgramaSemanaId(UUID programaSemanaId);

    void deleteByProgramaSemanaId(UUID programaSemanaId);
}
