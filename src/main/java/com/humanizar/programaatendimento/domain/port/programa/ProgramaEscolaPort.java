package com.humanizar.programaatendimento.domain.port.programa;

import java.util.List;
import java.util.UUID;

import com.humanizar.programaatendimento.domain.model.programa.ProgramaEscola;

public interface ProgramaEscolaPort {

    ProgramaEscola save(ProgramaEscola programaEscola);

    List<ProgramaEscola> saveAll(List<ProgramaEscola> programasEscola);

    List<ProgramaEscola> findByProgramaAtendimentoId(UUID programaAtendimentoId);

    void deleteByProgramaAtendimentoId(UUID programaAtendimentoId);
}
