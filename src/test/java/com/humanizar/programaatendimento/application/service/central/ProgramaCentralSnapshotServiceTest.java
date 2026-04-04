package com.humanizar.programaatendimento.application.service.central;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.humanizar.programaatendimento.application.outbound.dto.central.PendingCentralSnapshotDTO;
import com.humanizar.programaatendimento.application.usecase.central.FindPendingByEventIdUseCase;
import com.humanizar.programaatendimento.application.usecase.central.FindTargetsByEventIdUseCase;
import com.humanizar.programaatendimento.domain.model.enums.OperationType;
import com.humanizar.programaatendimento.domain.model.enums.Status;
import com.humanizar.programaatendimento.domain.model.pending.PendingProgramaAtendimento;
import com.humanizar.programaatendimento.domain.model.pending.PendingTargetStatus;

@ExtendWith(MockitoExtension.class)
class ProgramaCentralSnapshotServiceTest {

    @Mock
    private FindPendingByEventIdUseCase findPendingByEventIdUseCase;

    @Mock
    private FindTargetsByEventIdUseCase findTargetsByEventIdUseCase;

    private ProgramaCentralSnapshotService service;

    @BeforeEach
    void setUp() {
        service = new ProgramaCentralSnapshotService(findPendingByEventIdUseCase, findTargetsByEventIdUseCase);
    }

    @Test
    void shouldReturnSnapshotWithRawPayloadAndTargets() {
        UUID eventId = UUID.fromString("e91cf06a-b3ff-45af-a7cb-f7d3430a2304");
        UUID correlationId = UUID.fromString("12f2c9bb-8fb2-47fe-b94d-6557092d00a7");
        UUID patientId = UUID.fromString("51b1d210-e727-470a-a147-f5756caa6693");
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 2, 15, 45);
        String payloadSnapshot = "{\"patientId\":\"51b1d210-e727-470a-a147-f5756caa6693\"}";

        PendingProgramaAtendimento pending = PendingProgramaAtendimento.builder()
                .eventId(eventId)
                .correlationId(correlationId)
                .patientId(patientId)
                .programaAtendimentoId(UUID.fromString("fe03931c-262b-4699-8bbf-ab9f0ac0e77c"))
                .operationType(OperationType.CREATE)
                .payloadSnapshot(payloadSnapshot)
                .createdAt(createdAt)
                .status(Status.PENDING)
                .build();

        when(findPendingByEventIdUseCase.execute(eventId)).thenReturn(Optional.of(pending));
        when(findTargetsByEventIdUseCase.execute(eventId)).thenReturn(List.of(
                new PendingTargetStatus(
                        UUID.fromString("8e9f8ff8-6d1f-4d42-b78c-7fdf7e373c28"),
                        eventId,
                        "nucleo-relacionamento-service",
                        Status.PENDING)));

        Optional<PendingCentralSnapshotDTO> response = service.execute(eventId);

        assertTrue(response.isPresent());
        assertEquals(eventId, response.get().eventId());
        assertEquals(correlationId, response.get().correlationId());
        assertEquals(patientId, response.get().patientId());
        assertEquals("CREATE", response.get().operationType());
        assertEquals("PENDING", response.get().status());
        assertEquals(createdAt, response.get().createdAt());
        assertEquals(payloadSnapshot, response.get().payloadSnapshot());
        assertEquals(1, response.get().targets().size());
        assertEquals("nucleo-relacionamento-service", response.get().targets().get(0).targetService());
        assertEquals("PENDING", response.get().targets().get(0).status());

        verify(findTargetsByEventIdUseCase).execute(eventId);
    }

    @Test
    void shouldReturnEmptyWhenEventDoesNotExist() {
        UUID eventId = UUID.fromString("5cc0d770-a62a-4da0-b2aa-9312ab1d2434");

        when(findPendingByEventIdUseCase.execute(eventId)).thenReturn(Optional.empty());

        Optional<PendingCentralSnapshotDTO> response = service.execute(eventId);

        assertFalse(response.isPresent());
    }
}
