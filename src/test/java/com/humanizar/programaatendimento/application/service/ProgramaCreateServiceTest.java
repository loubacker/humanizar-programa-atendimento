package com.humanizar.programaatendimento.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.humanizar.programaatendimento.application.inbound.dto.InboundContextDTO;
import com.humanizar.programaatendimento.application.inbound.dto.InboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoResponsavelDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.inbound.mapper.InboundContextMapper;
import com.humanizar.programaatendimento.application.inbound.mapper.InboundProgramaAtendimentoMapper;
import com.humanizar.programaatendimento.application.outbound.dto.ProgramaCommandDTO;
import com.humanizar.programaatendimento.application.usecase.outbox.CreateOutboxCommandUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaAtendimentoUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaCommandsUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaTemplateUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.SaveAbordagensUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.SavePendingProgramaUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.SaveProgramaTreeUseCase;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.OperationType;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.enums.SimNao;
import com.humanizar.programaatendimento.domain.model.pending.PendingProgramaAtendimento;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaAtendimento;
import com.humanizar.programaatendimento.domain.port.nucleo.NucleoPatientPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtendimentoPort;
import com.humanizar.programaatendimento.infrastructure.controller.dto.ProgramaAtendimentoCreateResponseDTO;

@ExtendWith(MockitoExtension.class)
class ProgramaCreateServiceTest {

    @Mock
    private InboundContextMapper inboundContextMapper;
    @Mock
    private InboundProgramaAtendimentoMapper inboundProgramaAtendimentoMapper;
    @Mock
    private ProgramaAtendimentoPort programaAtendimentoPort;
    @Mock
    private AcolhimentoInboundService acolhimentoInboundService;
    @Mock
    private NucleoPatientPort nucleoPatientPort;
    @Mock
    private SaveProgramaTreeUseCase saveProgramaTreeUseCase;
    @Mock
    private SaveAbordagensUseCase saveAbordagensUseCase;
    @Mock
    private BuildProgramaAtendimentoUseCase buildProgramaAtendimentoUseCase;
    @Mock
    private BuildProgramaCommandsUseCase buildProgramaCommandsUseCase;
    @Mock
    private SavePendingProgramaUseCase savePendingProgramaUseCase;
    @Mock
    private BuildProgramaTemplateUseCase buildProgramaTemplateUsecase;
    @Mock
    private CreateOutboxCommandUseCase createOutboxCommandUseCase;

    @Captor
    private ArgumentCaptor<UUID> aggregateIdCaptor;

    private ProgramaCreateService service;

    @BeforeEach
    void setUp() {
        service = new ProgramaCreateService(
                inboundContextMapper,
                inboundProgramaAtendimentoMapper,
                programaAtendimentoPort,
                acolhimentoInboundService,
                nucleoPatientPort,
                saveProgramaTreeUseCase,
                saveAbordagensUseCase,
                buildProgramaAtendimentoUseCase,
                buildProgramaCommandsUseCase,
                savePendingProgramaUseCase,
                buildProgramaTemplateUsecase,
                createOutboxCommandUseCase);
    }

    @Test
    void shouldRegisterProgramaAndPublishOutboxCommand() throws Exception {
        UUID correlationId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();
        UUID pendingEventId = UUID.randomUUID();

        ProgramaAtendimentoDTO payload = createPayload(patientId, nucleoPatientId, nucleoId);
        InboundEnvelopeDTO<ProgramaAtendimentoDTO> envelope = createEnvelope(correlationId, payload);
        InboundContextDTO<ProgramaAtendimentoDTO> context = new InboundContextDTO<>(envelope, payload);
        PendingProgramaAtendimento pending = PendingProgramaAtendimento.builder().eventId(pendingEventId).build();
        List<ProgramaCommandDTO> commandPayload = List.of(new ProgramaCommandDTO(nucleoPatientId, List.of(UUID.randomUUID())));

        when(inboundContextMapper.fromEnvelop(envelope)).thenReturn(context);
        when(inboundProgramaAtendimentoMapper.toCreatePayload(payload, correlationId.toString())).thenReturn(payload);
        when(savePendingProgramaUseCase.serializePayload(payload, correlationId.toString())).thenReturn("{\"ok\":true}");
        when(savePendingProgramaUseCase.save(
                correlationId, patientId, null, OperationType.CREATE, "{\"ok\":true}"))
                .thenReturn(pending);
        when(programaAtendimentoPort.findByPatientId(patientId)).thenReturn(Optional.empty());
        when(buildProgramaAtendimentoUseCase.execute(
                any(UUID.class), eq(patientId), eq(payload), eq(correlationId.toString())))
                .thenAnswer(inv -> ProgramaAtendimento.builder()
                        .id(inv.getArgument(0))
                        .patientId(patientId)
                        .dataInicio(LocalDateTime.now())
                        .cadastroApp(SimNao.SIM)
                        .atEscolar(SimNao.NAO)
                        .build());
        when(nucleoPatientPort.existsById(nucleoPatientId)).thenReturn(false);
        when(buildProgramaCommandsUseCase.execute(payload.nucleoPatient())).thenReturn(commandPayload);

        doAnswer(inv -> {
            Callable<?> businessLogic = inv.getArgument(3);
            return businessLogic.call();
        }).when(buildProgramaTemplateUsecase).executeWithPendingGuard(
                eq(pendingEventId),
                eq(correlationId.toString()),
                eq(true),
                any(Callable.class));

        ProgramaAtendimentoCreateResponseDTO response = service.register(envelope);

        assertEquals(patientId, response.patientId());
        assertEquals(correlationId, response.correlationId());

        verify(savePendingProgramaUseCase).save(
                correlationId, patientId, null, OperationType.CREATE, "{\"ok\":true}");
        verify(buildProgramaTemplateUsecase).executeWithPendingGuard(
                eq(pendingEventId), eq(correlationId.toString()), eq(true), any(Callable.class));
        verify(programaAtendimentoPort).save(any(ProgramaAtendimento.class));
        verify(saveProgramaTreeUseCase).saveProgramasSemana(any(UUID.class), eq(payload.programasSemana()), eq(correlationId.toString()));
        verify(saveProgramaTreeUseCase).saveProgramasEscola(any(UUID.class), eq(payload.programasEscola()), eq(correlationId.toString()));
        verify(acolhimentoInboundService).createNucleoPatient(
                eq(nucleoPatientId),
                eq(patientId),
                eq(nucleoId),
                eq(payload.nucleoPatient().getFirst().nucleoPatientResponsavel()),
                eq(correlationId));
        verify(saveAbordagensUseCase).execute(payload.nucleoPatient());
        verify(createOutboxCommandUseCase).execute(
                eq(envelope),
                eq(pendingEventId),
                aggregateIdCaptor.capture(),
                eq(commandPayload));
        assertNotNull(aggregateIdCaptor.getValue());
        assertEquals(nucleoPatientId, commandPayload.getFirst().nucleoPatientId());
    }

    @Test
    void shouldMarkPendingAsErrorWhenPatientAlreadyExists() {
        UUID correlationId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID pendingEventId = UUID.randomUUID();

        ProgramaAtendimentoDTO payload = createPayload(patientId, UUID.randomUUID(), UUID.randomUUID());
        InboundEnvelopeDTO<ProgramaAtendimentoDTO> envelope = createEnvelope(correlationId, payload);
        InboundContextDTO<ProgramaAtendimentoDTO> context = new InboundContextDTO<>(envelope, payload);
        PendingProgramaAtendimento pending = PendingProgramaAtendimento.builder().eventId(pendingEventId).build();

        when(inboundContextMapper.fromEnvelop(envelope)).thenReturn(context);
        when(inboundProgramaAtendimentoMapper.toCreatePayload(payload, correlationId.toString())).thenReturn(payload);
        when(savePendingProgramaUseCase.serializePayload(payload, correlationId.toString())).thenReturn("{\"ok\":true}");
        when(savePendingProgramaUseCase.save(
                correlationId, patientId, null, OperationType.CREATE, "{\"ok\":true}"))
                .thenReturn(pending);
        when(programaAtendimentoPort.findByPatientId(patientId))
                .thenReturn(Optional.of(ProgramaAtendimento.builder().id(UUID.randomUUID()).patientId(patientId).build()));

        ProgramaAtendimentoException ex = assertThrows(ProgramaAtendimentoException.class, () -> service.register(envelope));

        assertEquals(ReasonCode.DUPLICATE_PATIENT, ex.getReasonCode());
        verify(savePendingProgramaUseCase).markAsError(pendingEventId);
        verify(buildProgramaTemplateUsecase, never()).executeWithPendingGuard(any(), anyString(), anyBoolean(), any());
        verify(createOutboxCommandUseCase, never()).execute(any(), any(), any(), any());
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
                "2026-03-27T10:15:30",
                SimNao.SIM,
                SimNao.NAO,
                "observacao",
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
