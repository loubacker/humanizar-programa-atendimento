package com.humanizar.programaatendimento.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaSnapshotUseCase;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaAtendimento;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtendimentoPort;

@Service
public class ProgramaRetrieveService {

    private final ProgramaAtendimentoPort programaAtendimentoPort;
    private final BuildProgramaSnapshotUseCase buildProgramaSnapshotUseCase;

    public ProgramaRetrieveService(
            ProgramaAtendimentoPort programaAtendimentoPort,
            BuildProgramaSnapshotUseCase buildProgramaSnapshotUseCase) {
        this.programaAtendimentoPort = programaAtendimentoPort;
        this.buildProgramaSnapshotUseCase = buildProgramaSnapshotUseCase;
    }

    public ProgramaAtendimentoDTO findByPatientId(UUID patientId) {
        String correlationId = UUID.randomUUID().toString();

        ProgramaAtendimento programa = programaAtendimentoPort.findByPatientId(patientId)
                .orElseThrow(() -> new ProgramaAtendimentoException(
                        ReasonCode.PATIENT_NOT_FOUND, correlationId,
                        "Programa nao encontrado para patientId=" + patientId));

        return buildProgramaSnapshotUseCase.buildSnapshot(programa, patientId);
    }
}
