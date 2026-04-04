package com.humanizar.programaatendimento.application.usecase.central;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.domain.model.pending.PendingProgramaAtendimento;
import com.humanizar.programaatendimento.domain.port.pending.PendingProgramaAtendimentoPort;

@Component
public class FindPendingByPatientUseCase {

    private static final Logger log = LoggerFactory.getLogger(FindPendingByPatientUseCase.class);

    private final PendingProgramaAtendimentoPort pendingProgramaAtendimentoPort;

    public FindPendingByPatientUseCase(PendingProgramaAtendimentoPort pendingProgramaAtendimentoPort) {
        this.pendingProgramaAtendimentoPort = pendingProgramaAtendimentoPort;
    }

    public Page<PendingProgramaAtendimento> execute(UUID patientId, Pageable pageable) {
        log.debug("Buscando execucoes pendentes para patientId={}, page={}, size={}",
                patientId, pageable.getPageNumber(), pageable.getPageSize());
        return pendingProgramaAtendimentoPort.findByPatientId(patientId, pageable);
    }
}
