package com.humanizar.programaatendimento.domain.port.programa;

import java.util.List;
import java.util.UUID;

import com.humanizar.programaatendimento.domain.model.programa.ProgramaSemana;

public interface ProgramaSemanaPort {

    ProgramaSemana save(ProgramaSemana programaSemana);

    List<ProgramaSemana> saveAll(List<ProgramaSemana> programasSemana);

    List<ProgramaSemana> findByProgramaAtendimentoId(UUID programaAtendimentoId);

    void deleteById(UUID id);

    void deleteByProgramaAtendimentoId(UUID programaAtendimentoId);
}
