package com.humanizar.programaatendimento.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.humanizar.programaatendimento.application.inbound.dto.InboundContextDTO;
import com.humanizar.programaatendimento.application.inbound.dto.InboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.messaging.AcolhimentoNucleoPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoResponsavelDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.inbound.mapper.InboundContextMapper;
import com.humanizar.programaatendimento.application.inbound.mapper.InboundProgramaAtendimentoMapper;
import com.humanizar.programaatendimento.application.outbound.dto.ProgramaCommandDTO;
import com.humanizar.programaatendimento.application.usecase.outbox.UpdateOutboxCommandUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaAtendimentoUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaCommandsUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaTemplateUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.DeleteAbordagensUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.DeleteProgramaTreeUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.SaveAbordagensUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.SavePendingProgramaUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.SaveProgramaTreeUseCase;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.OperationType;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.enums.SimNao;
import com.humanizar.programaatendimento.domain.model.pending.PendingProgramaAtendimento;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaAtendimento;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtendimentoPort;
import com.humanizar.programaatendimento.infrastructure.controller.dto.ProgramaAtendimentoUpdateResponseDTO;

@ExtendWith(MockitoExtension.class)
class ProgramaUpdateServiceTest {

    @Mock
    private InboundContextMapper inboundContextMapper;
    @Mock
    private InboundProgramaAtendimentoMapper inboundProgramaAtendimentoMapper;
    @Mock
    private ProgramaAtendimentoPort programaAtendimentoPort;
    @Mock
    private AcolhimentoInboundService acolhimentoInboundService;
    @Mock
    private SaveProgramaTreeUseCase saveProgramaTreeUseCase;
    @Mock
    private DeleteProgramaTreeUseCase deleteProgramaTreeUseCase;
    @Mock
    private SaveAbordagensUseCase saveAbordagensUseCase;
    @Mock
    private DeleteAbordagensUseCase deleteAbordagensUseCase;
    @Mock
    private BuildProgramaAtendimentoUseCase buildProgramaAtendimentoUseCase;
    @Mock
    private BuildProgramaCommandsUseCase buildProgramaCommandsUseCase;
    @Mock
    private SavePendingProgramaUseCase savePendingProgramaUseCase;
    @Mock
    private BuildProgramaTemplateUseCase buildProgramaTemplateUsecase;
    @Mock
    private UpdateOutboxCommandUseCase updateOutboxCommandUseCase;

    @Captor
    private ArgumentCaptor<ProgramaAtendimento> programaCaptor;

    private ProgramaUpdateService service;

    @BeforeEach
    void setUp() {
        service = new ProgramaUpdateService(
                inboundContextMapper,
                inboundProgramaAtendimentoMapper,
                programaAtendimentoPort,
                acolhimentoInboundService,
                saveProgramaTreeUseCase,
                deleteProgramaTreeUseCase,
                saveAbordagensUseCase,
                deleteAbordagensUseCase,
                buildProgramaAtendimentoUseCase,
                buildProgramaCommandsUseCase,
                savePendingProgramaUseCase,
                buildProgramaTemplateUsecase,
                updateOutboxCommandUseCase);
    }

    @Test
    void shouldUpdateProgramaAndPublishOutboxCommand() throws Exception {
        UUID pathPatientId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        UUID patientId = pathPatientId;
        UUID programaId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();
        UUID pendingEventId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(3);

        ProgramaAtendimentoDTO payload = createPayload(patientId, nucleoPatientId, nucleoId);
        InboundEnvelopeDTO<ProgramaAtendimentoDTO> envelope = createEnvelope(correlationId, payload);
        InboundContextDTO<ProgramaAtendimentoDTO> context = new InboundContextDTO<>(envelope, payload);

        ProgramaAtendimento existing = ProgramaAtendimento.builder()
                .id(programaId)
                .patientId(patientId)
                .createdAt(createdAt)
                .build();
        ProgramaAtendimento rebuilt = ProgramaAtendimento.builder()
                .id(programaId)
                .patientId(patientId)
                .build();

        PendingProgramaAtendimento pending = PendingProgramaAtendimento.builder().eventId(pendingEventId).build();
        List<ProgramaCommandDTO> commandPayload = List.of(new ProgramaCommandDTO(nucleoPatientId, List.of(UUID.randomUUID())));

        when(inboundContextMapper.fromUpdate(pathPatientId, envelope)).thenReturn(context);
        when(inboundProgramaAtendimentoMapper.toUpdatePayload(payload, correlationId.toString())).thenReturn(payload);
        when(programaAtendimentoPort.findByPatientId(patientId)).thenReturn(Optional.of(existing));
        when(savePendingProgramaUseCase.serializePayload(payload, correlationId.toString())).thenReturn("{\"snapshot\":true}");
        when(savePendingProgramaUseCase.save(
                correlationId, patientId, programaId, OperationType.UPDATE, "{\"snapshot\":true}"))
                .thenReturn(pending);
        when(buildProgramaAtendimentoUseCase.execute(programaId, patientId, payload, correlationId.toString()))
                .thenReturn(rebuilt);
        when(buildProgramaCommandsUseCase.execute(payload.nucleoPatient())).thenReturn(commandPayload);

        doAnswer(inv -> {
            Callable<ProgramaAtendimentoUpdateResponseDTO> businessLogic = inv.getArgument(3);
            return businessLogic.call();
        }).when(buildProgramaTemplateUsecase).executeWithPendingGuard(
                eq(pendingEventId),
                eq(correlationId.toString()),
                eq(false),
                ArgumentMatchers.<Callable<ProgramaAtendimentoUpdateResponseDTO>>any());

        ProgramaAtendimentoUpdateResponseDTO response = service.updateByPatientId(pathPatientId, envelope);

        assertEquals(patientId, response.patientId());
        assertEquals(correlationId, response.correlationId());

        verify(savePendingProgramaUseCase).save(
                correlationId, patientId, programaId, OperationType.UPDATE, "{\"snapshot\":true}");
        verify(deleteProgramaTreeUseCase).execute(programaId);
        verify(saveProgramaTreeUseCase).saveProgramasSemana(programaId, payload.programasSemana(), correlationId.toString());
        verify(saveProgramaTreeUseCase).saveProgramasEscola(programaId, payload.programasEscola(), correlationId.toString());
        verify(deleteAbordagensUseCase).execute(patientId);

        List<AcolhimentoNucleoPatientDTO> expectedNucleoCommands = List.of(
                new AcolhimentoNucleoPatientDTO(
                        nucleoPatientId,
                        nucleoId,
                        payload.nucleoPatient().getFirst().nucleoPatientResponsavel()));
        verify(acolhimentoInboundService).applyNucleoPatientSnapshot(
                patientId,
                expectedNucleoCommands,
                correlationId);
        verify(saveAbordagensUseCase).execute(payload.nucleoPatient());
        verify(updateOutboxCommandUseCase).execute(envelope, pendingEventId, programaId, commandPayload);

        verify(programaAtendimentoPort).save(programaCaptor.capture());
        assertEquals(createdAt, programaCaptor.getValue().getCreatedAt());
    }

    @Test
    void shouldFailWhenProgramaDoesNotExist() {
        UUID pathPatientId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();

        ProgramaAtendimentoDTO payload = createPayload(pathPatientId, UUID.randomUUID(), UUID.randomUUID());
        InboundEnvelopeDTO<ProgramaAtendimentoDTO> envelope = createEnvelope(correlationId, payload);
        InboundContextDTO<ProgramaAtendimentoDTO> context = new InboundContextDTO<>(envelope, payload);

        when(inboundContextMapper.fromUpdate(pathPatientId, envelope)).thenReturn(context);
        when(inboundProgramaAtendimentoMapper.toUpdatePayload(payload, correlationId.toString())).thenReturn(payload);
        when(programaAtendimentoPort.findByPatientId(pathPatientId)).thenReturn(Optional.empty());

        ProgramaAtendimentoException ex = assertThrows(
                ProgramaAtendimentoException.class,
                () -> service.updateByPatientId(pathPatientId, envelope));

        assertEquals(ReasonCode.PATIENT_NOT_FOUND, ex.getReasonCode());
        verify(savePendingProgramaUseCase, never()).save(any(), any(), any(), any(), any());
        verify(updateOutboxCommandUseCase, never()).execute(any(), any(), any(), any());
    }

    private InboundEnvelopeDTO<ProgramaAtendimentoDTO> createEnvelope(UUID correlationId, ProgramaAtendimentoDTO payload) {
        return new InboundEnvelopeDTO<>(
                correlationId,
                "humanizar-acolhimento",
                LocalDateTime.now(),
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1",
                payload);
    }

    private ProgramaAtendimentoDTO createPayload(UUID patientId, UUID nucleoPatientId, UUID nucleoId) {
        return new ProgramaAtendimentoDTO(
                patientId,
                "2026-03-27T11:20:00",
                SimNao.SIM,
                SimNao.NAO,
                "obs",
                List.of(),
                List.of(),
                List.of(new NucleoPatientDTO(
                        nucleoPatientId,
                        patientId,
                        nucleoId,
                        List.of(new NucleoResponsavelDTO(UUID.randomUUID(), "COORDENADOR")),
                        List.of())));
    }
}
