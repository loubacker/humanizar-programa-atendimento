package com.humanizar.programaatendimento.application.usecase.programa;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.humanizar.programaatendimento.domain.model.nucleo.NucleoPatient;
import com.humanizar.programaatendimento.domain.port.nucleo.AbordagemPatientPort;
import com.humanizar.programaatendimento.domain.port.nucleo.NucleoPatientPort;

@Service
public class DeleteAbordagensUseCase {

    private final NucleoPatientPort nucleoPatientPort;
    private final AbordagemPatientPort abordagemPatientPort;

    public DeleteAbordagensUseCase(
            NucleoPatientPort nucleoPatientPort,
            AbordagemPatientPort abordagemPatientPort) {
        this.nucleoPatientPort = nucleoPatientPort;
        this.abordagemPatientPort = abordagemPatientPort;
    }

    public void execute(UUID patientId) {
        List<NucleoPatient> nucleos = nucleoPatientPort.findAllByPatientId(patientId);
        for (NucleoPatient nucleo : nucleos) {
            abordagemPatientPort.deleteByNucleoPatientId(nucleo.getId());
        }
    }
}
