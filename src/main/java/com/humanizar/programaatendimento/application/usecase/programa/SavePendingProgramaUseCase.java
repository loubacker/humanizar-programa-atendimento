package com.humanizar.programaatendimento.application.usecase.programa;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.pending.PendingProgramaAtendimento;
import com.humanizar.programaatendimento.domain.model.enums.OperationType;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.enums.Status;
import com.humanizar.programaatendimento.domain.port.pending.PendingProgramaAtendimentoPort;

@Service
public class SavePendingProgramaUseCase {

    private static final Logger log = LoggerFactory.getLogger(SavePendingProgramaUseCase.class);

    private final PendingProgramaAtendimentoPort pendingPort;
    private final ObjectMapper objectMapper;

    public SavePendingProgramaUseCase(
            PendingProgramaAtendimentoPort pendingPort,
            ObjectMapper objectMapper) {
        this.pendingPort = pendingPort;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PendingProgramaAtendimento save(
            UUID correlationId, UUID patientId, UUID programaAtendimentoId,
            OperationType operationType, String payloadSnapshot) {
        PendingProgramaAtendimento pending = PendingProgramaAtendimento.builder()
                .correlationId(correlationId)
                .patientId(patientId)
                .programaAtendimentoId(programaAtendimentoId)
                .operationType(operationType)
                .payloadSnapshot(payloadSnapshot)
                .build();
        return pendingPort.save(pending);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsError(UUID eventId) {
        try {
            pendingPort.findByEventId(eventId).ifPresent(pending -> {
                pending.setStatus(Status.ERROR);
                pendingPort.save(pending);
            });
        } catch (Exception ex) {
            log.error("Falha ao marcar pending_programa_atendimento como ERROR. eventId={}", eventId, ex);
        }
    }

    public String serializePayload(Object payload, String correlationId) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.PERSISTENCE_FAILURE, correlationId,
                    "Falha ao serializar payloadSnapshot de pending_programa_atendimento");
        }
    }
}
