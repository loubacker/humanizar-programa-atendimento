package com.humanizar.programaatendimento.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.humanizar.programaatendimento.application.outbound.dto.CallbackDTO;
import com.humanizar.programaatendimento.application.usecase.callback.CheckDuplicateEventUseCase;
import com.humanizar.programaatendimento.application.usecase.callback.FinalizePendingProgramaUseCase;
import com.humanizar.programaatendimento.application.usecase.callback.SaveProcessedEventUseCase;
import com.humanizar.programaatendimento.application.usecase.callback.UpdateCallbackUseCase;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.enums.Status;

@Service
public class ProgramaCallbackService {

    private static final Logger log = LoggerFactory.getLogger(ProgramaCallbackService.class);

    private final CheckDuplicateEventUseCase checkDuplicateEventUseCase;
    private final UpdateCallbackUseCase updatePendingTargetStatusFromCallbackUseCase;
    private final FinalizePendingProgramaUseCase finalizePendingProgramaUseCase;
    private final SaveProcessedEventUseCase saveProcessedEventUseCase;
    private final ProgramaDeleteService programaDeleteService;

    public ProgramaCallbackService(
            CheckDuplicateEventUseCase checkDuplicateEventUseCase,
            UpdateCallbackUseCase updatePendingTargetStatusFromCallbackUseCase,
            FinalizePendingProgramaUseCase finalizePendingProgramaUseCase,
            SaveProcessedEventUseCase saveProcessedEventUseCase,
            ProgramaDeleteService programaDeleteService) {
        this.checkDuplicateEventUseCase = checkDuplicateEventUseCase;
        this.updatePendingTargetStatusFromCallbackUseCase = updatePendingTargetStatusFromCallbackUseCase;
        this.finalizePendingProgramaUseCase = finalizePendingProgramaUseCase;
        this.saveProcessedEventUseCase = saveProcessedEventUseCase;
        this.programaDeleteService = programaDeleteService;
    }

    public void processCallback(String consumerName, String targetService, CallbackDTO callback) {
        validateCallback(callback);

        String correlationId = callback.correlationId() != null ? callback.correlationId().toString() : null;
        try {
            checkDuplicateEventUseCase.execute(consumerName, callback.eventId(), correlationId);
        } catch (ProgramaAtendimentoException ex) {
            if (ex.getReasonCode() == ReasonCode.DUPLICATE_EVENT) {
                log.info("callback duplicado ignorado. consumer={}, eventId={}", consumerName, callback.eventId());
                return;
            }
            throw ex;
        }

        updatePendingTargetStatusFromCallbackUseCase.execute(
                callback.eventId(),
                targetService,
                resolveTargetStatus(callback.status()));
        finalizePendingProgramaUseCase.execute(callback.eventId());
        programaDeleteService.processDeletePosCallback(callback.eventId(), targetService, callback.status());

        saveProcessedEventUseCase.execute(consumerName, callback);
    }

    private Status resolveTargetStatus(String callbackStatus) {
        if ("PROCESSED".equalsIgnoreCase(callbackStatus)) {
            return Status.SUCCESS;
        }
        if (!"REJECTED".equalsIgnoreCase(callbackStatus)) {
            log.warn("Status de callback inesperado recebido: '{}'. Tratado como ERROR.", callbackStatus);
        }
        return Status.ERROR;
    }

    private void validateCallback(CallbackDTO callback) {
        if (callback == null) {
            throw new ProgramaAtendimentoException(ReasonCode.VALIDATION_ERROR, null, "callback é obrigatório");
        }

        String correlationId = callback.correlationId() != null ? callback.correlationId().toString() : null;

        if (callback.eventId() == null) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.VALIDATION_ERROR,
                    correlationId,
                    "callback.eventId é obrigatório");
        }

        if (callback.status() == null || callback.status().isBlank()) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.VALIDATION_ERROR,
                    correlationId,
                    "callback.status é obrigatório");
        }
    }
}
