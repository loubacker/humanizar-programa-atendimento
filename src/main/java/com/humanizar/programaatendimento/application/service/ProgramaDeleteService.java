package com.humanizar.programaatendimento.application.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.humanizar.programaatendimento.application.catalog.TargetCatalog;
import com.humanizar.programaatendimento.application.inbound.dto.InboundDeleteContextDTO;
import com.humanizar.programaatendimento.application.inbound.dto.InboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaDeleteDTO;
import com.humanizar.programaatendimento.application.inbound.mapper.InboundDeleteContextMapper;
import com.humanizar.programaatendimento.application.usecase.central.FindPendingByEventIdUseCase;
import com.humanizar.programaatendimento.application.usecase.outbox.DeleteOutboxCommandUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaSnapshotUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaTemplateUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.DeleteAbordagensUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.DeleteProgramaTreeUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.SavePendingProgramaUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.ValidateDeleteProgressUseCase;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.OperationType;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.enums.Status;
import com.humanizar.programaatendimento.domain.model.pending.PendingProgramaAtendimento;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaAtendimento;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtendimentoPort;
import com.humanizar.programaatendimento.infrastructure.controller.dto.ProgramaAtendimentoDeleteResponseDTO;

@Service
public class ProgramaDeleteService {

    private static final Logger log = LoggerFactory.getLogger(ProgramaDeleteService.class);

    private final InboundDeleteContextMapper inboundDeleteContextMapper;
    private final ProgramaAtendimentoPort programaAtendimentoPort;
    private final AcolhimentoInboundService acolhimentoInboundService;
    private final DeleteProgramaTreeUseCase deleteProgramaTreeUseCase;
    private final DeleteAbordagensUseCase deleteAbordagensUseCase;
    private final BuildProgramaSnapshotUseCase buildProgramaSnapshotUseCase;
    private final SavePendingProgramaUseCase savePendingProgramaUseCase;
    private final BuildProgramaTemplateUseCase buildProgramaTemplateUsecase;
    private final DeleteOutboxCommandUseCase deleteOutboxCommandUseCase;
    private final ValidateDeleteProgressUseCase validateDeleteProgressUseCase;
    private final FindPendingByEventIdUseCase findPendingByEventIdUseCase;

    public ProgramaDeleteService(
            InboundDeleteContextMapper inboundDeleteContextMapper,
            ProgramaAtendimentoPort programaAtendimentoPort,
            AcolhimentoInboundService acolhimentoInboundService,
            DeleteProgramaTreeUseCase deleteProgramaTreeUseCase,
            DeleteAbordagensUseCase deleteAbordagensUseCase,
            BuildProgramaSnapshotUseCase buildProgramaSnapshotUseCase,
            SavePendingProgramaUseCase savePendingProgramaUseCase,
            BuildProgramaTemplateUseCase buildProgramaTemplateUsecase,
            DeleteOutboxCommandUseCase deleteOutboxCommandUseCase,
            ValidateDeleteProgressUseCase validateDeleteProgressUseCase,
            FindPendingByEventIdUseCase findPendingByEventIdUseCase) {
        this.inboundDeleteContextMapper = inboundDeleteContextMapper;
        this.programaAtendimentoPort = programaAtendimentoPort;
        this.acolhimentoInboundService = acolhimentoInboundService;
        this.deleteProgramaTreeUseCase = deleteProgramaTreeUseCase;
        this.deleteAbordagensUseCase = deleteAbordagensUseCase;
        this.buildProgramaSnapshotUseCase = buildProgramaSnapshotUseCase;
        this.savePendingProgramaUseCase = savePendingProgramaUseCase;
        this.buildProgramaTemplateUsecase = buildProgramaTemplateUsecase;
        this.deleteOutboxCommandUseCase = deleteOutboxCommandUseCase;
        this.validateDeleteProgressUseCase = validateDeleteProgressUseCase;
        this.findPendingByEventIdUseCase = findPendingByEventIdUseCase;
    }

    @Transactional
    public ProgramaAtendimentoDeleteResponseDTO deleteByPatientId(
            UUID pathPatientId,
            InboundEnvelopeDTO<ProgramaDeleteDTO> envelop) {
        InboundDeleteContextDTO context = inboundDeleteContextMapper.fromDelete(pathPatientId, envelop);
        UUID patientId = context.payload().patientId();
        UUID correlationId = context.envelop().correlationId();
        String correlationIdText = correlationId != null ? correlationId.toString() : null;

        validateDeleteProgressUseCase.execute(patientId, correlationIdText);

        ProgramaAtendimento existing = programaAtendimentoPort.findByPatientId(patientId)
                .orElseThrow(() -> new ProgramaAtendimentoException(
                        ReasonCode.PATIENT_NOT_FOUND, correlationIdText,
                        "Programa não encontrado para patientId=" + patientId));

        ProgramaAtendimentoDTO snapshot = buildProgramaSnapshotUseCase.buildSnapshot(existing, patientId);

        PendingProgramaAtendimento pending = savePendingProgramaUseCase.save(
                correlationId, patientId, existing.getId(), OperationType.DELETE,
                savePendingProgramaUseCase.serializePayload(snapshot, correlationIdText));

        return buildProgramaTemplateUsecase.executeWithPendingGuard(
                pending.getEventId(), correlationIdText, false,
                () -> {
                    deleteOutboxCommandUseCase.execute(
                            context.envelop(), pending.getEventId(), existing.getId());

                    return new ProgramaAtendimentoDeleteResponseDTO("SUCCESS", "DELETE", patientId);
                });
    }

    @Transactional
    public void processDeletePosCallback(UUID eventId, String completedTarget, String callbackStatus) {
        if (!"PROCESSED".equalsIgnoreCase(callbackStatus)) {
            return;
        }

        if (!TargetCatalog.TARGET_NUCLEO_RELACIONAMENTO.equals(completedTarget)) {
            return;
        }

        PendingProgramaAtendimento pending = findPendingByEventIdUseCase.execute(eventId).orElse(null);
        if (pending == null || pending.getOperationType() != OperationType.DELETE || pending.getStatus() != Status.SUCCESS) {
            return;
        }

        try {
            executeLocalDelete(pending);
        } catch (Exception ex) {
            String correlationId = pending.getCorrelationId() != null ? pending.getCorrelationId().toString() : null;
            String errorMessage = extractErrorMessage(ex);
            log.error(
                    "Falha no delete local pós-callback. eventId={}, patientId={}, programaId={}, target={}, callbackStatus={}, correlationId={}",
                    eventId,
                    pending.getPatientId(),
                    pending.getProgramaAtendimentoId(),
                    completedTarget,
                    callbackStatus,
                    correlationId,
                    ex);
            safeMarkPendingAsError(eventId, errorMessage);
            throw unwrap(ex, pending.getCorrelationId() != null ? pending.getCorrelationId().toString() : null);
        }
    }

    private void executeLocalDelete(PendingProgramaAtendimento pending) {
        UUID programaId = pending.getProgramaAtendimentoId();
        UUID patientId = pending.getPatientId();
        UUID correlationId = pending.getCorrelationId();

        if (programaId != null) {
            deleteProgramaTreeUseCase.execute(programaId);
        }
        deleteAbordagensUseCase.execute(patientId);
        acolhimentoInboundService.deleteAllNucleosByPatientId(patientId, correlationId);
        programaAtendimentoPort.deleteByPatientId(patientId);
    }

    private void safeMarkPendingAsError(UUID eventId, String errorMessage) {
        try {
            savePendingProgramaUseCase.markAsError(eventId, errorMessage);
        } catch (Exception ex) {
            log.error("Falha ao marcar pending_programa_atendimento como ERROR no DELETE. eventId={}", eventId, ex);
        }
    }

    private ProgramaAtendimentoException unwrap(Exception ex, String correlationId) {
        if (ex instanceof ProgramaAtendimentoException programaAtendimentoException) {
            return programaAtendimentoException;
        }

        String message = ex != null && ex.getMessage() != null
                ? ex.getMessage()
                : "Falha no pipeline de delete";
        return new ProgramaAtendimentoException(ReasonCode.PERSISTENCE_FAILURE, correlationId, message);
    }

    private String extractErrorMessage(Exception ex) {
        if (ex == null) {
            return "Falha desconhecida no pipeline de delete";
        }

        Throwable cursor = ex;
        while (cursor.getCause() != null && cursor.getCause() != cursor) {
            cursor = cursor.getCause();
        }

        String message = cursor.getMessage();
        if (message == null || message.isBlank()) {
            message = ex.getMessage();
        }

        if (message == null || message.isBlank()) {
            return cursor.getClass().getSimpleName();
        }
        return message;
    }
}
