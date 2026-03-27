package com.humanizar.programaatendimento.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.humanizar.programaatendimento.application.outbound.dto.CallbackDTO;
import com.humanizar.programaatendimento.application.usecase.callback.CheckDuplicateEventUseCase;
import com.humanizar.programaatendimento.application.usecase.callback.FinalizePendingProgramaUseCase;
import com.humanizar.programaatendimento.application.usecase.callback.SaveProcessedEventUseCase;
import com.humanizar.programaatendimento.application.usecase.callback.UpdateCallbackUseCase;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.enums.Status;

@ExtendWith(MockitoExtension.class)
class ProgramaCallbackServiceTest {

    @Mock
    private CheckDuplicateEventUseCase checkDuplicateEventUseCase;
    @Mock
    private UpdateCallbackUseCase updateCallbackUseCase;
    @Mock
    private FinalizePendingProgramaUseCase finalizePendingProgramaUseCase;
    @Mock
    private SaveProcessedEventUseCase saveProcessedEventUseCase;

    private ProgramaCallbackService service;

    @BeforeEach
    void setUp() {
        service = new ProgramaCallbackService(
                checkDuplicateEventUseCase,
                updateCallbackUseCase,
                finalizePendingProgramaUseCase,
                saveProcessedEventUseCase);
    }

    @Test
    void shouldProcessProcessedCallbackAsSuccess() {
        String consumer = "callbackProgramaConsumer";
        String target = "humanizar-nucleo-relacionamento";
        CallbackDTO callback = callback("PROCESSED", UUID.randomUUID(), UUID.randomUUID());

        service.processCallback(consumer, target, callback);

        verify(checkDuplicateEventUseCase).execute(consumer, callback.eventId(), callback.correlationId().toString());
        verify(updateCallbackUseCase).execute(callback.eventId(), target, Status.SUCCESS);
        verify(finalizePendingProgramaUseCase).execute(callback.eventId());
        verify(saveProcessedEventUseCase).execute(consumer, callback);
    }

    @Test
    void shouldProcessRejectedCallbackAsError() {
        String consumer = "callbackProgramaConsumer";
        String target = "humanizar-nucleo-relacionamento";
        CallbackDTO callback = callback("REJECTED", UUID.randomUUID(), UUID.randomUUID());

        service.processCallback(consumer, target, callback);

        verify(checkDuplicateEventUseCase).execute(consumer, callback.eventId(), callback.correlationId().toString());
        verify(updateCallbackUseCase).execute(callback.eventId(), target, Status.ERROR);
        verify(finalizePendingProgramaUseCase).execute(callback.eventId());
        verify(saveProcessedEventUseCase).execute(consumer, callback);
    }

    @Test
    void shouldIgnoreDuplicateEvent() {
        String consumer = "callbackProgramaConsumer";
        String target = "humanizar-nucleo-relacionamento";
        CallbackDTO callback = callback("PROCESSED", UUID.randomUUID(), UUID.randomUUID());

        doThrow(new ProgramaAtendimentoException(ReasonCode.DUPLICATE_EVENT, callback.correlationId().toString(), "duplicado"))
                .when(checkDuplicateEventUseCase)
                .execute(eq(consumer), eq(callback.eventId()), any());

        service.processCallback(consumer, target, callback);

        verify(updateCallbackUseCase, never()).execute(any(), any(), any());
        verify(finalizePendingProgramaUseCase, never()).execute(any());
        verify(saveProcessedEventUseCase, never()).execute(any(), any());
    }

    @Test
    void shouldFailWhenCallbackIsNull() {
        ProgramaAtendimentoException ex = assertThrows(
                ProgramaAtendimentoException.class,
                () -> service.processCallback("consumer", "target", null));

        assertEquals(ReasonCode.VALIDATION_ERROR, ex.getReasonCode());
    }

    @Test
    void shouldFailWhenEventIdIsNull() {
        CallbackDTO callback = callback("PROCESSED", null, UUID.randomUUID());

        ProgramaAtendimentoException ex = assertThrows(
                ProgramaAtendimentoException.class,
                () -> service.processCallback("consumer", "target", callback));

        assertEquals(ReasonCode.VALIDATION_ERROR, ex.getReasonCode());
    }

    @Test
    void shouldFailWhenStatusIsBlank() {
        CallbackDTO callback = callback("   ", UUID.randomUUID(), UUID.randomUUID());

        ProgramaAtendimentoException ex = assertThrows(
                ProgramaAtendimentoException.class,
                () -> service.processCallback("consumer", "target", callback));

        assertEquals(ReasonCode.VALIDATION_ERROR, ex.getReasonCode());
    }

    private CallbackDTO callback(String status, UUID eventId, UUID correlationId) {
        return new CallbackDTO(
                "cmd.programa.created.v1",
                eventId,
                correlationId,
                "humanizar-nucleo-relacionamento",
                "humanizar.programa.event",
                "ev.nucleo-relacionamento.programa.processed.v1",
                "programa-atendimento",
                UUID.randomUUID(),
                1,
                LocalDateTime.now(),
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1",
                status,
                null,
                null,
                LocalDateTime.now(),
                null);
    }
}
