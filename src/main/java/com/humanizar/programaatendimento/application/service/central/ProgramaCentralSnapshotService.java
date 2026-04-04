package com.humanizar.programaatendimento.application.service.central;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.humanizar.programaatendimento.application.outbound.dto.central.PendingCentralSnapshotDTO;
import com.humanizar.programaatendimento.application.outbound.dto.central.PendingTargetStatusDTO;
import com.humanizar.programaatendimento.application.usecase.central.FindPendingByEventIdUseCase;
import com.humanizar.programaatendimento.application.usecase.central.FindTargetsByEventIdUseCase;

@Service
public class ProgramaCentralSnapshotService {

    private final FindPendingByEventIdUseCase findPendingByEventIdUseCase;
    private final FindTargetsByEventIdUseCase findTargetsByEventIdUseCase;

    public ProgramaCentralSnapshotService(
            FindPendingByEventIdUseCase findPendingByEventIdUseCase,
            FindTargetsByEventIdUseCase findTargetsByEventIdUseCase) {
        this.findPendingByEventIdUseCase = findPendingByEventIdUseCase;
        this.findTargetsByEventIdUseCase = findTargetsByEventIdUseCase;
    }

    public Optional<PendingCentralSnapshotDTO> execute(UUID eventId) {
        return findPendingByEventIdUseCase.execute(eventId)
                .map(pending -> {
                    List<PendingTargetStatusDTO> targets = findTargetsByEventIdUseCase.execute(eventId).stream()
                            .map(target -> new PendingTargetStatusDTO(
                                    target.getTargetService(),
                                    target.getStatus().name()))
                            .toList();

                    return new PendingCentralSnapshotDTO(
                            pending.getEventId(),
                            pending.getCorrelationId(),
                            pending.getPatientId(),
                            pending.getOperationType().name(),
                            pending.getStatus().name(),
                            pending.getCreatedAt(),
                            pending.getPayloadSnapshot(),
                            targets);
                });
    }
}
