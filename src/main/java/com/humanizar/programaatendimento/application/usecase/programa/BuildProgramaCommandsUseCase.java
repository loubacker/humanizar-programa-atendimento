package com.humanizar.programaatendimento.application.usecase.programa;

import java.util.List;

import org.springframework.stereotype.Service;

import com.humanizar.programaatendimento.application.inbound.dto.nucleo.AbordagemPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoPatientDTO;
import com.humanizar.programaatendimento.application.outbound.dto.ProgramaCommandDTO;

@Service
public class BuildProgramaCommandsUseCase {

    public List<ProgramaCommandDTO> execute(List<NucleoPatientDTO> nucleoPatients) {
        return nucleoPatients.stream()
                .map(np -> new ProgramaCommandDTO(
                        np.nucleoPatientId(),
                        np.abordagens() != null
                                ? np.abordagens().stream().map(AbordagemPatientDTO::abordagemId).toList()
                                : List.of()))
                .toList();
    }
}
