package com.humanizar.programaatendimento.application.usecase.callback;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.humanizar.programaatendimento.domain.model.pending.PendingProgramaAtendimento;
import com.humanizar.programaatendimento.domain.model.enums.Status;
import com.humanizar.programaatendimento.domain.model.pending.PendingTargetStatus;
import com.humanizar.programaatendimento.domain.port.pending.PendingProgramaAtendimentoPort;
import com.humanizar.programaatendimento.domain.port.pending.PendingTargetStatusPort;

@Service
public class FinalizePendingProgramaUseCase {

    private final PendingProgramaAtendimentoPort pendingProgramaAtendimentoPort;
    private final PendingTargetStatusPort pendingTargetStatusPort;

    public FinalizePendingProgramaUseCase(
            PendingProgramaAtendimentoPort pendingProgramaAtendimentoPort,
            PendingTargetStatusPort pendingTargetStatusPort) {
        this.pendingProgramaAtendimentoPort = pendingProgramaAtendimentoPort;
        this.pendingTargetStatusPort = pendingTargetStatusPort;
    }

    @Transactional
    public void execute(UUID eventId) {
        PendingProgramaAtendimento pending = pendingProgramaAtendimentoPort.findByEventId(eventId).orElse(null);
        if (pending == null || pending.getStatus() != Status.PENDING) {
            return;
        }

        List<PendingTargetStatus> targetStatuses = pendingTargetStatusPort.findByEventId(eventId);
        if (targetStatuses == null || targetStatuses.isEmpty()) {
            return;
        }

        if (targetStatuses.stream().anyMatch(target -> target.getStatus() == Status.ERROR)) {
            pending.setStatus(Status.ERROR);
            pendingProgramaAtendimentoPort.save(pending);
            return;
        }

        boolean allSuccess = targetStatuses.stream().allMatch(target -> target.getStatus() == Status.SUCCESS);
        if (allSuccess) {
            pending.setStatus(Status.SUCCESS);
            pendingProgramaAtendimentoPort.save(pending);
        }
    }
}
