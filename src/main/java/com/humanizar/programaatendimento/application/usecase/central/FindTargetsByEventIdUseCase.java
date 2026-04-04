package com.humanizar.programaatendimento.application.usecase.central;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.domain.model.pending.PendingTargetStatus;
import com.humanizar.programaatendimento.domain.port.pending.PendingTargetStatusPort;

@Component
public class FindTargetsByEventIdUseCase {

    private static final Logger log = LoggerFactory.getLogger(FindTargetsByEventIdUseCase.class);

    private final PendingTargetStatusPort pendingTargetStatusPort;

    public FindTargetsByEventIdUseCase(PendingTargetStatusPort pendingTargetStatusPort) {
        this.pendingTargetStatusPort = pendingTargetStatusPort;
    }

    public List<PendingTargetStatus> execute(UUID eventId) {
        log.debug("Buscando targets para eventId={}", eventId);
        return pendingTargetStatusPort.findByEventId(eventId);
    }
}
