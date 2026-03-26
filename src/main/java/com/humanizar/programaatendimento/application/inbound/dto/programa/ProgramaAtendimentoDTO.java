package com.humanizar.programaatendimento.application.inbound.dto.programa;

import java.util.List;
import java.util.UUID;

import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoPatientDTO;
import com.humanizar.programaatendimento.domain.model.enums.SimNao;

public record ProgramaAtendimentoDTO(
        UUID patientId,
        String dataInicio,
        SimNao cadastroApp,
        SimNao atEscolar,
        String observacao,
        List<ProgramaSemanaDTO> programasSemana,
        List<ProgramaEscolaDTO> programasEscola,
        List<NucleoPatientDTO> nucleoPatient) {

    public ProgramaAtendimentoDTO {
        programasSemana = programasSemana == null ? List.of() : List.copyOf(programasSemana);
        programasEscola = programasEscola == null ? List.of() : List.copyOf(programasEscola);
        nucleoPatient = nucleoPatient == null ? List.of() : List.copyOf(nucleoPatient);
    }
}
