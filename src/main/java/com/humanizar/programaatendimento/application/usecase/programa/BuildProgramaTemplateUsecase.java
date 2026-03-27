package com.humanizar.programaatendimento.application.usecase.programa;

import java.util.UUID;
import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;

@Component
public class BuildProgramaTemplateUsecase {

    private final SavePendingProgramaUseCase savePendingUseCase;
    private final ResolveServiceExceptionUseCase resolveExceptionUseCase;

    public BuildProgramaTemplateUsecase(
            SavePendingProgramaUseCase savePendingUseCase,
            ResolveServiceExceptionUseCase resolveExceptionUseCase) {
        this.savePendingUseCase = savePendingUseCase;
        this.resolveExceptionUseCase = resolveExceptionUseCase;
    }

    public <T> T executeWithPendingGuard(
            UUID pendingEventId,
            String correlationId,
            boolean checkDuplicate,
            Callable<T> businessLogic) {
        try {
            return businessLogic.call();
        } catch (ProgramaAtendimentoException ex) {
            savePendingUseCase.markAsError(pendingEventId);
            throw ex;
        } catch (Exception ex) {
            savePendingUseCase.markAsError(pendingEventId);
            throw resolveExceptionUseCase.resolve(ex, correlationId, checkDuplicate);
        }
    }
}
