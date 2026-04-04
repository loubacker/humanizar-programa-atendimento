package com.humanizar.programaatendimento.application.usecase.central;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.domain.model.pending.PendingProgramaAtendimento;
import com.humanizar.programaatendimento.domain.port.pending.PendingProgramaAtendimentoPort;

@Component
public class FindPendingByEventIdUseCase {

    private static final Logger log = LoggerFactory.getLogger(FindPendingByEventIdUseCase.class);

    private final PendingProgramaAtendimentoPort pendingProgramaAtendimentoPort;

    public FindPendingByEventIdUseCase(PendingProgramaAtendimentoPort pendingProgramaAtendimentoPort) {
        this.pendingProgramaAtendimentoPort = pendingProgramaAtendimentoPort;
    }

    public Optional<PendingProgramaAtendimento> execute(UUID eventId) {
        log.debug("Buscando execucao pendente para eventId={}", eventId);
        return pendingProgramaAtendimentoPort.findByEventId(eventId);
    }
}
