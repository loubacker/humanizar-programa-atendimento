package com.humanizar.programaatendimento.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaSnapshotUseCase;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.enums.SimNao;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaAtendimento;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtendimentoPort;

@ExtendWith(MockitoExtension.class)
class ProgramaRetrieveServiceTest {

    @Mock
    private ProgramaAtendimentoPort programaAtendimentoPort;
    @Mock
    private BuildProgramaSnapshotUseCase buildProgramaSnapshotUseCase;

    private ProgramaRetrieveService service;

    @BeforeEach
    void setUp() {
        service = new ProgramaRetrieveService(programaAtendimentoPort, buildProgramaSnapshotUseCase);
    }

    @Test
    void shouldReturnSnapshotWhenProgramaExists() {
        UUID patientId = UUID.randomUUID();
        ProgramaAtendimento programa = ProgramaAtendimento.builder()
                .id(UUID.randomUUID())
                .patientId(patientId)
                .dataInicio(LocalDateTime.now())
                .cadastroApp(SimNao.SIM)
                .atEscolar(SimNao.NAO)
                .build();
        ProgramaAtendimentoDTO snapshot = new ProgramaAtendimentoDTO(
                patientId,
                "2026-03-27T13:00:00",
                SimNao.SIM,
                SimNao.NAO,
                "ok",
                List.of(),
                List.of(),
                List.of());

        when(programaAtendimentoPort.findByPatientId(patientId)).thenReturn(Optional.of(programa));
        when(buildProgramaSnapshotUseCase.buildSnapshot(programa, patientId)).thenReturn(snapshot);

        ProgramaAtendimentoDTO response = service.findByPatientId(patientId);

        assertSame(snapshot, response);
        verify(buildProgramaSnapshotUseCase).buildSnapshot(programa, patientId);
    }

    @Test
    void shouldThrowWhenProgramaDoesNotExist() {
        UUID patientId = UUID.randomUUID();
        when(programaAtendimentoPort.findByPatientId(patientId)).thenReturn(Optional.empty());

        ProgramaAtendimentoException ex = assertThrows(
                ProgramaAtendimentoException.class,
                () -> service.findByPatientId(patientId));

        assertEquals(ReasonCode.PATIENT_NOT_FOUND, ex.getReasonCode());
    }
}
