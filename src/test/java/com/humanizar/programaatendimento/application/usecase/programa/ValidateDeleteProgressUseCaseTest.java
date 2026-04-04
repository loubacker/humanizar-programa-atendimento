package com.humanizar.programaatendimento.application.usecase.programa;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.OperationType;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.enums.Status;
import com.humanizar.programaatendimento.domain.port.pending.PendingProgramaAtendimentoPort;

@ExtendWith(MockitoExtension.class)
class ValidateDeleteProgressUseCaseTest {

    @Mock
    private PendingProgramaAtendimentoPort pendingProgramaAtendimentoPort;

    private ValidateDeleteProgressUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ValidateDeleteProgressUseCase(pendingProgramaAtendimentoPort);
    }

    @Test
    void shouldThrowDeleteInProgressWhenThereIsPendingDeleteForPatient() {
        UUID patientId = UUID.randomUUID();
        String correlationId = UUID.randomUUID().toString();

        when(pendingProgramaAtendimentoPort.checkDeleteStatusByPatientId(
                patientId, OperationType.DELETE, Status.PENDING))
                .thenReturn(true);

        ProgramaAtendimentoException ex = assertThrows(
                ProgramaAtendimentoException.class,
                () -> useCase.execute(patientId, correlationId));

        assertEquals(ReasonCode.DELETE_IN_PROGRESS, ex.getReasonCode());
        verify(pendingProgramaAtendimentoPort).checkDeleteStatusByPatientId(
                patientId, OperationType.DELETE, Status.PENDING);
    }

    @Test
    void shouldDoNothingWhenThereIsNoDeleteInProgress() {
        UUID patientId = UUID.randomUUID();
        String correlationId = UUID.randomUUID().toString();

        when(pendingProgramaAtendimentoPort.checkDeleteStatusByPatientId(
                patientId, OperationType.DELETE, Status.PENDING))
                .thenReturn(false);

        assertDoesNotThrow(() -> useCase.execute(patientId, correlationId));

        verify(pendingProgramaAtendimentoPort).checkDeleteStatusByPatientId(
                patientId, OperationType.DELETE, Status.PENDING);
    }
}
