package com.humanizar.programaatendimento.application.usecase.programa;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.OperationType;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.enums.Status;
import com.humanizar.programaatendimento.domain.port.pending.PendingProgramaAtendimentoPort;

@Component
public class ValidateDeleteProgressUseCase {

    private final PendingProgramaAtendimentoPort pendingProgramaAtendimentoPort;

    public ValidateDeleteProgressUseCase(PendingProgramaAtendimentoPort pendingProgramaAtendimentoPort) {
        this.pendingProgramaAtendimentoPort = pendingProgramaAtendimentoPort;
    }

    public void execute(UUID patientId, String correlationId) {
        boolean hasDeleteInProgress = pendingProgramaAtendimentoPort.checkDeleteStatusByPatientId(
                patientId,
                OperationType.DELETE,
                Status.PENDING);

        if (hasDeleteInProgress) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.DELETE_IN_PROGRESS,
                    correlationId,
                    "Ja existe operacao DELETE pendente para patientId=" + patientId);
        }
    }
}
