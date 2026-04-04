package com.humanizar.programaatendimento.application.usecase.programa;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            List<AbordagemPatient> currentAbordagens = abordagemPatientPort.findByNucleoPatientId(np.nucleoPatientId());
            Map<UUID, AbordagemPatient> currentByAbordagemId = currentAbordagens.stream()
                    .collect(Collectors.toMap(AbordagemPatient::getAbordagemId, Function.identity()));
            Set<UUID> incomingAbordagemIds = np.abordagens().stream()
                    .map(a -> a.abordagemId())
                    .collect(Collectors.toSet());

            List<AbordagemPatient> novasAbordagens = np.abordagens().stream()
                    .filter(a -> !currentByAbordagemId.containsKey(a.abordagemId()))
                    .map(a -> AbordagemPatient.builder()
                            .id(null)
                            .nucleoPatientId(np.nucleoPatientId())
                            .abordagemId(a.abordagemId())
                            .build())
                    .toList();

            for (AbordagemPatient current : currentAbordagens) {
                if (!incomingAbordagemIds.contains(current.getAbordagemId())) {
                    abordagemPatientPort.deleteById(current.getId());
                }
            }

            if (!novasAbordagens.isEmpty()) {
                abordagemPatientPort.saveAll(novasAbordagens);
            }
        }
    }
}
