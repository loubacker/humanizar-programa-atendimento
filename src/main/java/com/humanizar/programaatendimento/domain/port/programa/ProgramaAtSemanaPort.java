package com.humanizar.programaatendimento.domain.port.programa;

import java.util.List;
import java.util.UUID;

import com.humanizar.programaatendimento.domain.model.programa.ProgramaSemana;

public interface ProgramaAtSemanaPort {

    ProgramaSemana save(ProgramaSemana programaAtSemana);

    List<ProgramaSemana> saveAll(List<ProgramaSemana> programasAtSemana);

    List<ProgramaSemana> findByProgramaAtendimentoId(UUID programaAtendimentoId);

    void deleteByProgramaAtendimentoId(UUID programaAtendimentoId);
}
