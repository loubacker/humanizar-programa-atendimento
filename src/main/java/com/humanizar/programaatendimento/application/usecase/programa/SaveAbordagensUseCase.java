package com.humanizar.programaatendimento.application.usecase.programa;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoPatientDTO;
import com.humanizar.programaatendimento.domain.model.nucleo.AbordagemPatient;
import com.humanizar.programaatendimento.domain.port.nucleo.AbordagemPatientPort;

@Service
public class SaveAbordagensUseCase {

    private final AbordagemPatientPort abordagemPatientPort;

    public SaveAbordagensUseCase(AbordagemPatientPort abordagemPatientPort) {
        this.abordagemPatientPort = abordagemPatientPort;
    }

    public void execute(List<NucleoPatientDTO> nucleoPatients) {
        for (NucleoPatientDTO np : nucleoPatients) {
            if (np.abordagens() != null && !np.abordagens().isEmpty()) {
                List<AbordagemPatient> abordagens = np.abordagens().stream()
                        .map(a -> AbordagemPatient.builder()
                                .id(UUID.randomUUID())
                                .nucleoPatientId(np.nucleoPatientId())
                                .abordagemId(a.abordagemId())
                                .build())
                        .toList();
                abordagemPatientPort.saveAll(abordagens);
            }
        }
    }
}
