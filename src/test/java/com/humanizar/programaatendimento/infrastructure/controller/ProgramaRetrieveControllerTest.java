package com.humanizar.programaatendimento.infrastructure.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.outbound.dto.central.PendingCentralListDTO;
import com.humanizar.programaatendimento.application.outbound.dto.central.PendingCentralPageDTO;
import com.humanizar.programaatendimento.application.outbound.dto.central.PendingCentralSnapshotDTO;
import com.humanizar.programaatendimento.application.outbound.dto.central.PendingTargetStatusDTO;
import com.humanizar.programaatendimento.application.service.ProgramaRetrieveService;
import com.humanizar.programaatendimento.application.service.central.ProgramaCentralListService;
import com.humanizar.programaatendimento.application.service.central.ProgramaCentralSnapshotService;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.enums.SimNao;
import com.humanizar.programaatendimento.infrastructure.controller.handler.ProgramaAtendimentoExceptionHandler;

@ExtendWith(MockitoExtension.class)
class ProgramaRetrieveControllerTest {

    @Mock
    private ProgramaRetrieveService programaRetrieveService;

    @Mock
    private ProgramaCentralListService programaCentralListService;

    @Mock
    private ProgramaCentralSnapshotService programaCentralSnapshotService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProgramaRetrieveController controller = new ProgramaRetrieveController(
                programaRetrieveService,
                programaCentralListService,
                programaCentralSnapshotService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ProgramaAtendimentoExceptionHandler())
                .build();
    }

    @Test
    void shouldReturn200WithProgramaPayload() throws Exception {
        UUID patientId = UUID.fromString("c7c81c70-2c72-4640-bb95-dba92f4a9d00");
        ProgramaAtendimentoDTO payload = new ProgramaAtendimentoDTO(
                patientId,
                "2026-04-01",
                SimNao.SIM,
                SimNao.NAO,
                "Observacao",
                List.of(),
                List.of(),
                List.of());

        when(programaRetrieveService.findByPatientId(patientId)).thenReturn(payload);

        mockMvc.perform(get("/api/v1/programa-atendimento/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.dataInicio").value("2026-04-01"))
                .andExpect(jsonPath("$.observacao").value("Observacao"));

        verify(programaRetrieveService).findByPatientId(patientId);
    }

    @Test
    void shouldReturn404WhenProgramaDoesNotExist() throws Exception {
        UUID patientId = UUID.fromString("f45c0b88-1770-4fa4-a63a-b56f0fd40cde");
        String correlationId = "corr-retrieve-404";

        when(programaRetrieveService.findByPatientId(patientId))
                .thenThrow(new ProgramaAtendimentoException(
                        ReasonCode.PATIENT_NOT_FOUND,
                        correlationId,
                        "Programa nao encontrado"));

        mockMvc.perform(get("/api/v1/programa-atendimento/{patientId}", patientId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.reasonCode").value("PATIENT_NOT_FOUND"))
                .andExpect(jsonPath("$.correlationId").value(correlationId))
                .andExpect(jsonPath("$.path").value("/api/v1/programa-atendimento/" + patientId));

        verify(programaRetrieveService).findByPatientId(patientId);
    }

    @Test
    void shouldReturn200WithCentralPagePayload() throws Exception {
        UUID patientId = UUID.fromString("c19fdb68-21fe-485c-a2f6-ef94b3e34587");
        UUID eventId = UUID.fromString("d9d7ca7f-c79b-44d5-8c51-7d5dcb990ec7");
        UUID correlationId = UUID.fromString("90ac3f53-48c0-4f65-a0d2-36946935e3ca");

        PendingCentralPageDTO payload = new PendingCentralPageDTO(
                List.of(new PendingCentralListDTO(
                        eventId,
                        correlationId,
                        patientId,
                        "UPDATE",
                        "SUCCESS",
                        LocalDateTime.of(2026, 4, 2, 16, 15),
                        List.of(new PendingTargetStatusDTO("nucleo-relacionamento-service", "SUCCESS")))),
                1,
                5,
                3,
                11);

        when(programaCentralListService.execute(patientId, 1, 5)).thenReturn(payload);

        mockMvc.perform(get("/api/v1/programa-atendimento/central/{patientId}", patientId)
                .param("page", "1")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.totalElements").value(11))
                .andExpect(jsonPath("$.data[0].eventId").value(eventId.toString()))
                .andExpect(jsonPath("$.data[0].targets[0].targetService").value("nucleo-relacionamento-service"));

        verify(programaCentralListService).execute(patientId, 1, 5);
    }

    @Test
    void shouldReturn200WithSnapshotPayload() throws Exception {
        UUID eventId = UUID.fromString("0bff2932-3447-420b-a850-a70ff06c7b02");
        UUID correlationId = UUID.fromString("241012e1-b719-49e9-8640-c5f09f8204d0");
        UUID patientId = UUID.fromString("e91caf4d-411f-4708-af09-1e0cf8d53fbd");

        PendingCentralSnapshotDTO payload = new PendingCentralSnapshotDTO(
                eventId,
                correlationId,
                patientId,
                "CREATE",
                "PENDING",
                LocalDateTime.of(2026, 4, 2, 17, 0),
                "{\"patientId\":\"e91caf4d-411f-4708-af09-1e0cf8d53fbd\"}",
                List.of(new PendingTargetStatusDTO("nucleo-relacionamento-service", "PENDING")));

        when(programaCentralSnapshotService.execute(eventId)).thenReturn(Optional.of(payload));

        mockMvc.perform(get("/api/v1/programa-atendimento/central/snapshot/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(eventId.toString()))
                .andExpect(jsonPath("$.correlationId").value(correlationId.toString()))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.payloadSnapshot.patientId").value(patientId.toString()));

        verify(programaCentralSnapshotService).execute(eventId);
    }

    @Test
    void shouldReturn404WhenSnapshotDoesNotExist() throws Exception {
        UUID eventId = UUID.fromString("6a7bbf48-68d1-42b4-b3b7-9fca7b8aa4bf");

        when(programaCentralSnapshotService.execute(eventId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/programa-atendimento/central/snapshot/{eventId}", eventId))
                .andExpect(status().isNotFound());

        verify(programaCentralSnapshotService).execute(eventId);
    }
}
