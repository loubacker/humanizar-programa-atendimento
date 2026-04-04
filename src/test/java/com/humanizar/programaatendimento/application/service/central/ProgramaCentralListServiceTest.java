package com.humanizar.programaatendimento.application.service.central;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.humanizar.programaatendimento.application.outbound.dto.central.PendingCentralPageDTO;
import com.humanizar.programaatendimento.application.usecase.central.FindPendingByPatientUseCase;
import com.humanizar.programaatendimento.application.usecase.central.FindTargetsByEventIdUseCase;
import com.humanizar.programaatendimento.domain.model.enums.OperationType;
import com.humanizar.programaatendimento.domain.model.enums.Status;
import com.humanizar.programaatendimento.domain.model.pending.PendingProgramaAtendimento;
import com.humanizar.programaatendimento.domain.model.pending.PendingTargetStatus;

@ExtendWith(MockitoExtension.class)
class ProgramaCentralListServiceTest {

    @Mock
    private FindPendingByPatientUseCase findPendingByPatientUseCase;

    @Mock
    private FindTargetsByEventIdUseCase findTargetsByEventIdUseCase;

    private ProgramaCentralListService service;

    @BeforeEach
    void setUp() {
        service = new ProgramaCentralListService(findPendingByPatientUseCase, findTargetsByEventIdUseCase);
    }

    @Test
    void shouldReturnPageWithMappedTargets() {
        UUID patientId = UUID.fromString("5e64a1f5-35d1-4c75-bebb-9845d952f018");
        UUID eventId = UUID.fromString("9dce2ce7-28d2-46ab-83d2-f60b69c65a17");
        UUID correlationId = UUID.fromString("299cb151-e181-4b2c-a9df-6f2b77f9cf11");
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 2, 14, 30);

        PendingProgramaAtendimento pending = PendingProgramaAtendimento.builder()
                .eventId(eventId)
                .correlationId(correlationId)
                .patientId(patientId)
                .programaAtendimentoId(UUID.fromString("f96a4c13-bc13-4179-b5cf-43ffd2b38f85"))
                .operationType(OperationType.UPDATE)
                .payloadSnapshot("{\"snapshot\":true}")
                .createdAt(createdAt)
                .status(Status.SUCCESS)
                .build();

        Page<PendingProgramaAtendimento> pendingPage = new PageImpl<>(
                List.of(pending),
                PageRequest.of(0, 10),
                1);

        when(findPendingByPatientUseCase.execute(eq(patientId), any()))
                .thenReturn(pendingPage);
        when(findTargetsByEventIdUseCase.execute(eventId))
                .thenReturn(List.of(new PendingTargetStatus(
                        UUID.fromString("6f0e0c05-6304-48bc-aa11-644bd763058c"),
                        eventId,
                        "nucleo-relacionamento-service",
                        Status.SUCCESS)));

        PendingCentralPageDTO response = service.execute(patientId, 0, 10);

        assertEquals(1, response.data().size());
        assertEquals(0, response.page());
        assertEquals(10, response.size());
        assertEquals(1, response.totalPages());
        assertEquals(1L, response.totalElements());
        assertEquals(eventId, response.data().get(0).eventId());
        assertEquals(correlationId, response.data().get(0).correlationId());
        assertEquals(patientId, response.data().get(0).patientId());
        assertEquals("UPDATE", response.data().get(0).operationType());
        assertEquals("SUCCESS", response.data().get(0).status());
        assertEquals(createdAt, response.data().get(0).createdAt());
        assertEquals(1, response.data().get(0).targets().size());
        assertEquals("nucleo-relacionamento-service", response.data().get(0).targets().get(0).targetService());
        assertEquals("SUCCESS", response.data().get(0).targets().get(0).status());

        verify(findTargetsByEventIdUseCase).execute(eventId);
    }

    @Test
    void shouldReturnEmptyPageWhenNoPendingExecutionsExist() {
        UUID patientId = UUID.fromString("9398c00f-a3f9-4f6c-8cc3-5cc748e3f459");
        Page<PendingProgramaAtendimento> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0);

        when(findPendingByPatientUseCase.execute(eq(patientId), any()))
                .thenReturn(emptyPage);

        PendingCentralPageDTO response = service.execute(patientId, 0, 10);

        assertTrue(response.data().isEmpty());
        assertEquals(0, response.page());
        assertEquals(10, response.size());
        assertEquals(0, response.totalPages());
        assertEquals(0L, response.totalElements());
    }
}
