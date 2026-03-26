package com.humanizar.programaatendimento.application.usecase.programa;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;

@Service
public class ResolveServiceExceptionUseCase {

    public ProgramaAtendimentoException resolve(
            Exception ex, String correlationId, boolean checkDuplicate) {
        if (ex instanceof ProgramaAtendimentoException programaException) {
            return programaException;
        }
        if (checkDuplicate && isDuplicateException(ex)) {
            return new ProgramaAtendimentoException(
                    ReasonCode.DUPLICATE_PATIENT, correlationId,
                    "Programa ja existe para o patientId informado");
        }
        String message = ex != null && ex.getMessage() != null
                ? ex.getMessage()
                : "Falha inesperada no pipeline";
        return new ProgramaAtendimentoException(ReasonCode.PERSISTENCE_FAILURE, correlationId, message);
    }

    public boolean isDuplicateException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof java.sql.SQLException sqlException
                    && "23505".equals(sqlException.getSQLState())) {
                return true;
            }
            String message = current.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase(Locale.ROOT);
                if (normalized.contains("uk_programa_patient_id")
                        || normalized.contains("uk_nucleo_patient_patient_nucleo")
                        || normalized.contains("duplicate key")
                        || normalized.contains("restrição de unicidade")
                        || normalized.contains("restricao de unicidade")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
