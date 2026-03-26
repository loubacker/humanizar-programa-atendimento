package com.humanizar.programaatendimento.application.inbound.dto.programa;

import java.util.List;

public record ProgramaEscolaDTO(
        String nomeProfissional,
        String nomeEscola,
        List<AtEscolaSemanaDTO> atEscolaSemana) {

    public ProgramaEscolaDTO {
        atEscolaSemana = atEscolaSemana == null ? List.of() : List.copyOf(atEscolaSemana);
    }
}
