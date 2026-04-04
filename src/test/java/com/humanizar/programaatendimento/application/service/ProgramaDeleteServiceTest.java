package com.humanizar.programaatendimento.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.humanizar.programaatendimento.domain.model.enums.SimNao;
import com.humanizar.programaatendimento.domain.model.enums.Status;
import com.humanizar.programaatendimento.domain.model.pending.PendingProgramaAtendimento;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaAtendimento;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtendimentoPort;
import com.humanizar.programaatendimento.infrastructure.controller.dto.ProgramaAtendimentoDeleteResponseDTO;

@ExtendWith(MockitoExtension.class)
class ProgramaDeleteServiceTest {

    @Mock
    private InboundDeleteContextMapper inboundDeleteContextMapper;
    @Mock
    private ProgramaAtendimentoPort programaAtendimentoPort;
    @Mock
    private AcolhimentoInboundService acolhimentoInboundService;
    @Mock
    private DeleteProgramaTreeUseCase deleteProgramaTreeUseCase;
    @Mock
    private DeleteAbordagensUseCase deleteAbordagensUseCase;
    @Mock
    private BuildProgramaSnapshotUseCase buildProgramaSnapshotUseCase;
    @Mock
    private SavePendingProgramaUseCase savePendingProgramaUseCase;
    @Mock
    private BuildProgramaTemplateUseCase buildProgramaTemplateUsecase;
    @Mock
    private DeleteOutboxCommandUseCase deleteOutboxCommandUseCase;
    @Mock
    private ValidateDeleteProgressUseCase validateDeleteProgressUseCase;
    @Mock
    private FindPendingByEventIdUseCase findPendingByEventIdUseCase;

    private ProgramaDeleteService service;

    @BeforeEach
    void setUp() {
        service = new ProgramaDeleteService(
                inboundDeleteContextMapper,
                programaAtendimentoPort,
                acolhimentoInboundService,
                deleteProgramaTreeUseCase,
                deleteAbordagensUseCase,
                buildProgramaSnapshotUseCase,
                savePendingProgramaUseCase,
                buildProgramaTemplateUsecase,
                deleteOutboxCommandUseCase,
                validateDeleteProgressUseCase,
                findPendingByEventIdUseCase);
    }

    @Test
    void shouldCreatePendingAndPublishOutboxCommandWithoutDeletingLocally() throws Exception {
        UUID pathPatientId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        UUID patientId = pathPatientId;
        UUID programaId = UUID.randomUUID();
        UUID pendingEventId = UUID.randomUUID();

        InboundEnvelopeDTO<ProgramaDeleteDTO> envelope = createEnvelope(correlationId, patientId);
        InboundDeleteContextDTO context = new InboundDeleteContextDTO(envelope, envelope.payload());

        ProgramaAtendimento existing = ProgramaAtendimento.builder()
                .id(programaId)
                .patientId(patientId)
                .build();
        ProgramaAtendimentoDTO snapshot = new ProgramaAtendimentoDTO(
                patientId,
                "2026-03-27T12:00:00",
                SimNao.SIM,
                SimNao.NAO,
                "snapshot",
                List.of(),
                List.of(),
                List.of());
        PendingProgramaAtendimento pending = PendingProgramaAtendimento.builder().eventId(pendingEventId).build();

        when(inboundDeleteContextMapper.fromDelete(pathPatientId, envelope)).thenReturn(context);
        when(programaAtendimentoPort.findByPatientId(patientId)).thenReturn(Optional.of(existing));
        when(buildProgramaSnapshotUseCase.buildSnapshot(existing, patientId)).thenReturn(snapshot);
        when(savePendingProgramaUseCase.serializePayload(snapshot, correlationId.toString()))
                .thenReturn("{\"snapshot\":true}");
        when(savePendingProgramaUseCase.save(
                correlationId, patientId, programaId, OperationType.DELETE, "{\"snapshot\":true}"))
                .thenReturn(pending);

        doAnswer(inv -> {
            Callable<ProgramaAtendimentoDeleteResponseDTO> businessLogic = inv.getArgument(3);
            return businessLogic.call();
        }).when(buildProgramaTemplateUsecase).executeWithPendingGuard(
                eq(pendingEventId),
                eq(correlationId.toString()),
                eq(false),
                ArgumentMatchers.<Callable<ProgramaAtendimentoDeleteResponseDTO>>any());

        ProgramaAtendimentoDeleteResponseDTO response = service.deleteByPatientId(pathPatientId, envelope);

        assertEquals("SUCCESS", response.status());
        assertEquals("DELETE", response.operation());
        assertEquals(patientId, response.patientId());

        verify(validateDeleteProgressUseCase).execute(patientId, correlationId.toString());
        verify(savePendingProgramaUseCase).save(
                correlationId, patientId, programaId, OperationType.DELETE, "{\"snapshot\":true}");
        verify(deleteOutboxCommandUseCase).execute(envelope, pendingEventId, programaId);
        verify(deleteProgramaTreeUseCase, never()).execute(any());
        verify(deleteAbordagensUseCase, never()).execute(any());
        verify(acolhimentoInboundService, never()).deleteAllNucleosByPatientId(any(), any());
        verify(programaAtendimentoPort, never()).deleteByPatientId(any());
    }

    @Test
    void shouldFailWhenProgramaDoesNotExist() {
        UUID pathPatientId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();

        InboundEnvelopeDTO<ProgramaDeleteDTO> envelope = createEnvelope(correlationId, pathPatientId);
        InboundDeleteContextDTO context = new InboundDeleteContextDTO(envelope, envelope.payload());

        when(inboundDeleteContextMapper.fromDelete(pathPatientId, envelope)).thenReturn(context);
        when(programaAtendimentoPort.findByPatientId(pathPatientId)).thenReturn(Optional.empty());

        ProgramaAtendimentoException ex = assertThrows(
                ProgramaAtendimentoException.class,
                () -> service.deleteByPatientId(pathPatientId, envelope));

        assertEquals(ReasonCode.PATIENT_NOT_FOUND, ex.getReasonCode());
        verify(validateDeleteProgressUseCase).execute(pathPatientId, correlationId.toString());
        verify(savePendingProgramaUseCase, never()).save(any(), any(), any(), any(), any());
        verify(deleteOutboxCommandUseCase, never()).execute(any(), any(), any());
    }

    @Test
    void shouldBlockDeleteWhenThereIsPendingDeleteInProgress() {
        UUID pathPatientId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();

        InboundEnvelopeDTO<ProgramaDeleteDTO> envelope = createEnvelope(correlationId, pathPatientId);
        InboundDeleteContextDTO context = new InboundDeleteContextDTO(envelope, envelope.payload());

        when(inboundDeleteContextMapper.fromDelete(pathPatientId, envelope)).thenReturn(context);
        doThrow(new ProgramaAtendimentoException(
                ReasonCode.DELETE_IN_PROGRESS,
                correlationId.toString(),
                "Ja existe operacao DELETE pendente para patientId=" + pathPatientId))
                .when(validateDeleteProgressUseCase)
                .execute(pathPatientId, correlationId.toString());

        ProgramaAtendimentoException ex = assertThrows(
                ProgramaAtendimentoException.class,
                () -> service.deleteByPatientId(pathPatientId, envelope));

        assertEquals(ReasonCode.DELETE_IN_PROGRESS, ex.getReasonCode());
        verify(validateDeleteProgressUseCase).execute(pathPatientId, correlationId.toString());
        verify(programaAtendimentoPort, never()).findByPatientId(any());
        verify(savePendingProgramaUseCase, never()).save(any(), any(), any(), any(), any());
        verify(deleteOutboxCommandUseCase, never()).execute(any(), any(), any());
    }

    @Test
    void shouldExecuteLocalDeleteAfterProcessedCallbackWhenPendingIsSuccess() {
        UUID eventId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID programaId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();

        PendingProgramaAtendimento pending = PendingProgramaAtendimento.builder()
                .eventId(eventId)
                .patientId(patientId)
                .programaAtendimentoId(programaId)
                .correlationId(correlationId)
                .operationType(OperationType.DELETE)
                .status(Status.SUCCESS)
                .payloadSnapshot("{\"snapshot\":true}")
                .build();

        when(findPendingByEventIdUseCase.execute(eventId)).thenReturn(Optional.of(pending));

        service.processDeletePosCallback(eventId, "humanizar-nucleo-relacionamento", "PROCESSED");

        verify(deleteProgramaTreeUseCase).execute(programaId);
        verify(deleteAbordagensUseCase).execute(patientId);
        verify(acolhimentoInboundService).deleteAllNucleosByPatientId(patientId, correlationId);
        verify(programaAtendimentoPort).deleteByPatientId(patientId);
        verify(savePendingProgramaUseCase, never()).markAsError(any());
    }

    @Test
    void shouldIgnorePostCallbackSagaWhenPendingIsNotSuccess() {
        UUID eventId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        PendingProgramaAtendimento pending = PendingProgramaAtendimento.builder()
                .eventId(eventId)
                .patientId(patientId)
                .operationType(OperationType.DELETE)
                .status(Status.PENDING)
                .payloadSnapshot("{\"snapshot\":true}")
                .build();

        when(findPendingByEventIdUseCase.execute(eventId)).thenReturn(Optional.of(pending));

        service.processDeletePosCallback(eventId, "humanizar-nucleo-relacionamento", "PROCESSED");

        verify(deleteProgramaTreeUseCase, never()).execute(any());
        verify(deleteAbordagensUseCase, never()).execute(any());
        verify(acolhimentoInboundService, never()).deleteAllNucleosByPatientId(any(), any());
        verify(programaAtendimentoPort, never()).deleteByPatientId(any());
    }

    @Test
    void shouldIgnorePostCallbackSagaWhenTargetIsNotNucleoRelacionamento() {
        UUID eventId = UUID.randomUUID();

        service.processDeletePosCallback(eventId, "humanizar-outro-servico", "PROCESSED");

        verify(findPendingByEventIdUseCase, never()).execute(any());
        verify(deleteProgramaTreeUseCase, never()).execute(any());
    }

    private InboundEnvelopeDTO<ProgramaDeleteDTO> createEnvelope(UUID correlationId, UUID patientId) {
        return new InboundEnvelopeDTO<>(
                correlationId,
                "humanizar-acolhimento",
                LocalDateTime.now(),
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1",
                new ProgramaDeleteDTO(patientId));
    }
}
