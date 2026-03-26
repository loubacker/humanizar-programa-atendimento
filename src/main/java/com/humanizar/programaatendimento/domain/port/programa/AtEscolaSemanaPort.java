package com.humanizar.programaatendimento.domain.port.programa;

import java.util.List;
import java.util.UUID;

import com.humanizar.programaatendimento.domain.model.programa.AtEscolaSemana;

public interface AtEscolaSemanaPort {

    AtEscolaSemana save(AtEscolaSemana atEscolaSemana);

    List<AtEscolaSemana> saveAll(List<AtEscolaSemana> atEscolaSemana);

    List<AtEscolaSemana> findByProgramaAtEscolaId(UUID programaAtEscolaId);

    void deleteByProgramaAtEscolaId(UUID programaAtEscolaId);
}
