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

        if (checkDuplicate) {
            ReasonCode duplicateReasonCode = resolveDuplicateReasonCode(ex);
            if (duplicateReasonCode != null) {
                return new ProgramaAtendimentoException(
                        duplicateReasonCode,
                        correlationId,
                        duplicateReasonCode.getMessage());
            }
        }

        String message = ex != null && ex.getMessage() != null
                ? ex.getMessage()
                : "Falha inesperada no pipeline";
        return new ProgramaAtendimentoException(ReasonCode.PERSISTENCE_FAILURE, correlationId, message);
    }

    public boolean isDuplicateException(Throwable throwable) {
        return resolveDuplicateReasonCode(throwable) != null;
    }

    private ReasonCode resolveDuplicateReasonCode(Throwable throwable) {
        Throwable current = throwable;
        boolean duplicateViolationDetected = false;

        while (current != null) {
            if (current instanceof java.sql.SQLException sqlException
                    && "23505".equals(sqlException.getSQLState())) {
                duplicateViolationDetected = true;
            }

            String message = current.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase(Locale.ROOT);
                if (normalized.contains("uk_patient_nucleo")) {
                    return ReasonCode.DUPLICATE_PATIENT;
                }
                if (normalized.contains("uk_programa_semana_dia")) {
                    return ReasonCode.DUPLICATE_PROGRAMA_SEMANA_DIA;
                }
                if (normalized.contains("uk_at_escola_semana_dia")) {
                    return ReasonCode.DUPLICATE_AT_ESCOLA_SEMANA_DIA;
                }
                if (normalized.contains("uk_nucleo_responsavel")) {
                    return ReasonCode.DUPLICATE_NUCLEO_RESPONSAVEL;
                }
                if (normalized.contains("uk_abordagem_nucleo_patient")) {
                    return ReasonCode.ABORDAGEM_DUPLICATED;
                }
                if (normalized.contains("duplicate key")
                        || normalized.contains("restricao de unicidade")
                        || normalized.contains("violacao de chave unica")
                        || normalized.contains("violacao de restricao unica")) {
                    duplicateViolationDetected = true;
                }
            }

            current = current.getCause();
        }

        if (duplicateViolationDetected) {
            return ReasonCode.DUPLICATE_CONSTRAINT;
        }
        return null;
    }
}
