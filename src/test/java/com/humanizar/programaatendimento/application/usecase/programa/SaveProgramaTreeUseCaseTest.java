package com.humanizar.programaatendimento.application.usecase.programa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.humanizar.programaatendimento.application.inbound.dto.programa.AtEscolaSemanaDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.AtEscolaSemanaScheduleDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaEscolaDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaSemanaDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaSemanaScheduleDTO;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.Semana;
import com.humanizar.programaatendimento.domain.model.programa.AtEscolaSemana;
import com.humanizar.programaatendimento.domain.model.programa.AtEscolaSemanaSchedule;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaEscola;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaSemana;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaSemanaSchedule;
import com.humanizar.programaatendimento.domain.port.programa.AtEscolaSemanaPort;
import com.humanizar.programaatendimento.domain.port.programa.AtEscolaSemanaSchedulePort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaEscolaPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaSemanaPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaSemanaSchedulePort;

@ExtendWith(MockitoExtension.class)
class SaveProgramaTreeUseCaseTest {

    @Mock
    private ProgramaSemanaPort programaSemanaPort;
    @Mock
    private ProgramaSemanaSchedulePort programaSemanaSchedulePort;
    @Mock
    private ProgramaEscolaPort programaEscolaPort;
    @Mock
    private AtEscolaSemanaPort atEscolaSemanaPort;
    @Mock
    private AtEscolaSemanaSchedulePort atEscolaSemanaSchedulePort;

    @Captor
    private ArgumentCaptor<ProgramaSemana> programaSemanaCaptor;
    @Captor
    private ArgumentCaptor<List<ProgramaSemanaSchedule>> programaSemanaScheduleCaptor;
    @Captor
    private ArgumentCaptor<ProgramaEscola> programaEscolaCaptor;
    @Captor
    private ArgumentCaptor<AtEscolaSemana> atEscolaSemanaCaptor;
    @Captor
    private ArgumentCaptor<List<AtEscolaSemanaSchedule>> atEscolaSemanaScheduleCaptor;

    private SaveProgramaTreeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SaveProgramaTreeUseCase(
                programaSemanaPort,
                programaSemanaSchedulePort,
                programaEscolaPort,
                atEscolaSemanaPort,
                atEscolaSemanaSchedulePort);
    }

    @Test
    void shouldReuseExistingProgramaSemanaAndReplaceSchedulesForThatDay() {
        UUID programaId = UUID.randomUUID();
        UUID existingSemanaId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();

        when(programaSemanaPort.findByProgramaAtendimentoId(programaId)).thenReturn(List.of(
                ProgramaSemana.builder()
                        .id(existingSemanaId)
                        .programaAtendimentoId(programaId)
                        .diaSemana(Semana.SEGUNDA)
                        .build()));
        when(programaSemanaPort.save(any(ProgramaSemana.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.saveProgramasSemana(
                programaId,
                List.of(new ProgramaSemanaDTO(
                        "SEGUNDA",
                        List.of(new ProgramaSemanaScheduleDTO(
                                nucleoId,
                                "08:00",
                                "09:00",
                                "MANHA")))),
                "corr-1");

        verify(programaSemanaPort).save(programaSemanaCaptor.capture());
        assertEquals(existingSemanaId, programaSemanaCaptor.getValue().getId());
        verify(programaSemanaSchedulePort).deleteByProgramaSemanaId(existingSemanaId);
        verify(programaSemanaSchedulePort).saveAll(programaSemanaScheduleCaptor.capture());
        ProgramaSemanaSchedule scheduleSalvo = programaSemanaScheduleCaptor.getValue().getFirst();
        assertEquals(existingSemanaId, scheduleSalvo.getProgramaSemanaId());
        assertEquals(nucleoId, scheduleSalvo.getNucleoId());
        verify(programaSemanaPort, never()).deleteById(existingSemanaId);
    }

    @Test
    void shouldDeleteProgramaSemanaRemovedFromPayload() {
        UUID programaId = UUID.randomUUID();
        UUID existingSemanaId = UUID.randomUUID();

        when(programaSemanaPort.findByProgramaAtendimentoId(programaId)).thenReturn(List.of(
                ProgramaSemana.builder()
                        .id(existingSemanaId)
                        .programaAtendimentoId(programaId)
                        .diaSemana(Semana.SEGUNDA)
                        .build()));

        useCase.saveProgramasSemana(programaId, List.of(), "corr-2");

        verify(programaSemanaSchedulePort).deleteByProgramaSemanaId(existingSemanaId);
        verify(programaSemanaPort).deleteById(existingSemanaId);
        verify(programaSemanaPort, never()).save(any());
    }

    @Test
    void shouldReuseExistingProgramaEscolaAndReplaceSchedulesForMatchedDay() {
        UUID programaId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID atSemanaId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();

        when(programaEscolaPort.findByProgramaAtendimentoId(programaId)).thenReturn(List.of(
                ProgramaEscola.builder()
                        .id(escolaId)
                        .programaAtendimentoId(programaId)
                        .nomeProfissional("Maria")
                        .nomeEscola("Escola A")
                        .build()));
        when(programaEscolaPort.save(any(ProgramaEscola.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(atEscolaSemanaPort.findByProgramaEscolaId(escolaId)).thenReturn(List.of(
                AtEscolaSemana.builder()
                        .id(atSemanaId)
                        .programaEscolaId(escolaId)
                        .diaSemana(Semana.TERCA)
                        .build()));
        when(atEscolaSemanaPort.save(any(AtEscolaSemana.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.saveProgramasEscola(
                programaId,
                List.of(new ProgramaEscolaDTO(
                        "Maria",
                        "Escola A",
                        List.of(new AtEscolaSemanaDTO(
                                "TERCA",
                                List.of(new AtEscolaSemanaScheduleDTO(
                                        nucleoId,
                                        "10:00",
                                        "11:00",
                                        "MANHA")))))),
                "corr-3");

        verify(programaEscolaPort).save(programaEscolaCaptor.capture());
        assertEquals(escolaId, programaEscolaCaptor.getValue().getId());
        verify(atEscolaSemanaPort).save(atEscolaSemanaCaptor.capture());
        assertEquals(atSemanaId, atEscolaSemanaCaptor.getValue().getId());
        verify(atEscolaSemanaSchedulePort).deleteByAtEscolaSemanaId(atSemanaId);
        verify(atEscolaSemanaSchedulePort).saveAll(atEscolaSemanaScheduleCaptor.capture());
        assertEquals(atSemanaId, atEscolaSemanaScheduleCaptor.getValue().getFirst().getAtEscolaSemanaId());
        verify(programaEscolaPort, never()).deleteById(escolaId);
        verify(atEscolaSemanaPort, never()).deleteById(atSemanaId);
    }

    @Test
    void shouldDeleteProgramaEscolaAndNestedDaysRemovedFromPayload() {
        UUID programaId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID atSemanaId = UUID.randomUUID();

        when(programaEscolaPort.findByProgramaAtendimentoId(programaId)).thenReturn(List.of(
                ProgramaEscola.builder()
                        .id(escolaId)
                        .programaAtendimentoId(programaId)
                        .nomeProfissional("Maria")
                        .nomeEscola("Escola A")
                        .build()));
        when(atEscolaSemanaPort.findByProgramaEscolaId(escolaId)).thenReturn(List.of(
                AtEscolaSemana.builder()
                        .id(atSemanaId)
                        .programaEscolaId(escolaId)
                        .diaSemana(Semana.TERCA)
                        .build()));

        useCase.saveProgramasEscola(programaId, List.of(), "corr-4");

        verify(atEscolaSemanaSchedulePort).deleteByAtEscolaSemanaId(atSemanaId);
        verify(atEscolaSemanaPort).deleteById(atSemanaId);
        verify(programaEscolaPort).deleteById(escolaId);
        verify(programaEscolaPort, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateProgramaEscolaInPayload() {
        UUID programaId = UUID.randomUUID();

        ProgramaAtendimentoException ex = assertThrows(
                ProgramaAtendimentoException.class,
                () -> useCase.saveProgramasEscola(
                        programaId,
                        List.of(
                                new ProgramaEscolaDTO("Maria", "Escola A", List.of()),
                                new ProgramaEscolaDTO("Maria", "Escola A", List.of())),
                        "corr-5"));

        assertEquals("ProgramaEscola duplicado no payload: Maria|Escola A", ex.getMessage());
    }
}
