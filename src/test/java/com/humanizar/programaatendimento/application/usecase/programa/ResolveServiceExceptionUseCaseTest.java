package com.humanizar.programaatendimento.application.usecase.programa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;

class ResolveServiceExceptionUseCaseTest {

    private ResolveServiceExceptionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ResolveServiceExceptionUseCase();
    }

    @Test
    void shouldReturnSameExceptionWhenAlreadyProgramaException() {
        ProgramaAtendimentoException original = new ProgramaAtendimentoException(
                ReasonCode.VALIDATION_ERROR,
                "corr-1",
                "invalid");

        ProgramaAtendimentoException resolved = useCase.resolve(original, "corr-2", true);

        assertSame(original, resolved);
    }

    @Test
    void shouldMapUkPatientNucleoToDuplicatePatient() {
        Exception ex = duplicateException("duplicate key value violates unique constraint \"uk_patient_nucleo\"");

        ProgramaAtendimentoException resolved = useCase.resolve(ex, "corr-1", true);

        assertEquals(ReasonCode.DUPLICATE_PATIENT, resolved.getReasonCode());
    }

    @Test
    void shouldMapUkProgramaSemanaDiaToDuplicateProgramaSemanaDia() {
        Exception ex = duplicateException("duplicate key value violates unique constraint \"uk_programa_semana_dia\"");

        ProgramaAtendimentoException resolved = useCase.resolve(ex, "corr-2", true);

        assertEquals(ReasonCode.DUPLICATE_PROGRAMA_SEMANA_DIA, resolved.getReasonCode());
    }

    @Test
    void shouldMapUkAtEscolaSemanaDiaToDuplicateAtEscolaSemanaDia() {
        Exception ex = duplicateException("duplicate key value violates unique constraint \"uk_at_escola_semana_dia\"");

        ProgramaAtendimentoException resolved = useCase.resolve(ex, "corr-3", true);

        assertEquals(ReasonCode.DUPLICATE_AT_ESCOLA_SEMANA_DIA, resolved.getReasonCode());
    }

    @Test
    void shouldMapUkNucleoResponsavelToDuplicateNucleoResponsavel() {
        Exception ex = duplicateException("duplicate key value violates unique constraint \"uk_nucleo_responsavel\"");

        ProgramaAtendimentoException resolved = useCase.resolve(ex, "corr-4", true);

        assertEquals(ReasonCode.DUPLICATE_NUCLEO_RESPONSAVEL, resolved.getReasonCode());
    }

    @Test
    void shouldKeepAbordagemDuplicatedForUkAbordagemNucleoPatient() {
        Exception ex = duplicateException(
                "duplicate key value violates unique constraint \"uk_abordagem_nucleo_patient\"");

        ProgramaAtendimentoException resolved = useCase.resolve(ex, "corr-5", true);

        assertEquals(ReasonCode.ABORDAGEM_DUPLICATED, resolved.getReasonCode());
    }

    @Test
    void shouldFallbackToDuplicateConstraintWhenDuplicateNotMapped() {
        Exception ex = duplicateException("duplicate key value violates unique constraint \"uk_unknown_constraint\"");

        ProgramaAtendimentoException resolved = useCase.resolve(ex, "corr-6", true);

        assertEquals(ReasonCode.DUPLICATE_CONSTRAINT, resolved.getReasonCode());
    }

    @Test
    void shouldReturnPersistenceFailureWhenNotDuplicate() {
        Exception ex = new RuntimeException("boom");

        ProgramaAtendimentoException resolved = useCase.resolve(ex, "corr-7", true);

        assertEquals(ReasonCode.PERSISTENCE_FAILURE, resolved.getReasonCode());
        assertEquals("boom", resolved.getMessage());
    }

    @Test
    void shouldReturnPersistenceFailureWhenDuplicateCheckDisabled() {
        Exception ex = duplicateException("duplicate key value violates unique constraint \"uk_programa_semana_dia\"");

        ProgramaAtendimentoException resolved = useCase.resolve(ex, "corr-8", false);

        assertEquals(ReasonCode.PERSISTENCE_FAILURE, resolved.getReasonCode());
    }

    @Test
    void shouldDetectDuplicateBySqlState() {
        Exception ex = duplicateException("any duplicate message");

        assertTrue(useCase.isDuplicateException(ex));
    }

    @Test
    void shouldNotDetectDuplicateForGenericError() {
        Exception ex = new IllegalStateException("generic failure");

        assertFalse(useCase.isDuplicateException(ex));
    }

    private Exception duplicateException(String message) {
        return new RuntimeException(
                "wrapper",
                new SQLException(message, "23505"));
    }
}
