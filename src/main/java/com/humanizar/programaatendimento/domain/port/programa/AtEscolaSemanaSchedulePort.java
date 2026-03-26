package com.humanizar.programaatendimento.domain.port.programa;

import java.util.List;
import java.util.UUID;

import com.humanizar.programaatendimento.domain.model.programa.AtEscolaSemanaSchedule;

public interface AtEscolaSemanaSchedulePort {

    AtEscolaSemanaSchedule save(AtEscolaSemanaSchedule atEscolaSemanaSchedule);

    List<AtEscolaSemanaSchedule> saveAll(List<AtEscolaSemanaSchedule> atEscolaSemanaSchedule);

    List<AtEscolaSemanaSchedule> findByAtEscolaSemanaId(UUID atEscolaSemanaId);

    void deleteByAtEscolaSemanaId(UUID atEscolaSemanaId);
}
