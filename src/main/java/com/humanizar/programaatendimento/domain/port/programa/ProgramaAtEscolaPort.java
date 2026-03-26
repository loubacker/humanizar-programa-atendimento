package com.humanizar.programaatendimento.domain.port.programa;

import java.util.List;
import java.util.UUID;

import com.humanizar.programaatendimento.domain.model.programa.ProgramaEscola;

public interface ProgramaAtEscolaPort {

    ProgramaEscola save(ProgramaEscola programaAtEscola);

    List<ProgramaEscola> saveAll(List<ProgramaEscola> programasAtEscola);

    List<ProgramaEscola> findByProgramaAtendimentoId(UUID programaAtendimentoId);

    void deleteByProgramaAtendimentoId(UUID programaAtendimentoId);
}
