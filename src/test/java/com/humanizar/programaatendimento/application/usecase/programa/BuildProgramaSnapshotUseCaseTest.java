package com.humanizar.programaatendimento.application.usecase.programa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoResponsavelDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.AtEscolaSemanaScheduleDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaSemanaScheduleDTO;
import com.humanizar.programaatendimento.domain.model.enums.ResponsavelRole;
import com.humanizar.programaatendimento.domain.model.enums.Semana;
import com.humanizar.programaatendimento.domain.model.enums.SimNao;
import com.humanizar.programaatendimento.domain.model.nucleo.AbordagemPatient;
import com.humanizar.programaatendimento.domain.model.nucleo.NucleoPatient;
import com.humanizar.programaatendimento.domain.model.nucleo.NucleoPatientResponsavel;
import com.humanizar.programaatendimento.domain.model.programa.AtEscolaSemana;
import com.humanizar.programaatendimento.domain.model.programa.AtEscolaSemanaSchedule;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaAtendimento;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaEscola;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaSemana;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaSemanaSchedule;
import com.humanizar.programaatendimento.domain.port.nucleo.AbordagemPatientPort;
import com.humanizar.programaatendimento.domain.port.nucleo.NucleoPatientPort;
import com.humanizar.programaatendimento.domain.port.nucleo.NucleoPatientResponsavelPort;
import com.humanizar.programaatendimento.domain.port.programa.AtEscolaSemanaPort;
import com.humanizar.programaatendimento.domain.port.programa.AtEscolaSemanaSchedulePort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaEscolaPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaSemanaPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaSemanaSchedulePort;

@ExtendWith(MockitoExtension.class)
class BuildProgramaSnapshotUseCaseTest {

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
    @Mock
    private NucleoPatientPort nucleoPatientPort;
    @Mock
    private NucleoPatientResponsavelPort responsavelPort;
    @Mock
    private AbordagemPatientPort abordagemPatientPort;

    private BuildProgramaSnapshotUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new BuildProgramaSnapshotUseCase(
                programaSemanaPort,
                programaSemanaSchedulePort,
                programaEscolaPort,
                atEscolaSemanaPort,
                atEscolaSemanaSchedulePort,
                nucleoPatientPort,
                responsavelPort,
                abordagemPatientPort);
    }

    @Test
    void shouldBuildDeterministicSnapshotWithConsolidatedDaysAndStableOrdering() {
        Scenario scenario = arrangeScenario(false);

        ProgramaAtendimentoDTO snapshot = useCase.buildSnapshot(scenario.programa(), scenario.patientId());

        assertEquals(List.of("SEGUNDA", "TERCA"),
                snapshot.programasSemana().stream().map(s -> s.diaSemana()).toList());
        assertEquals(List.of(
                "07:00|08:00|NOITE|00000000-0000-0000-0000-000000000120",
                "08:00|10:00|MANHA|00000000-0000-0000-0000-000000000110",
                "08:00|10:00|MANHA|00000000-0000-0000-0000-000000000130",
                "08:00|10:00|TARDE|00000000-0000-0000-0000-000000000125"),
                snapshot.programasSemana().get(0).programaSemanaSchedule().stream()
                        .map(this::semanaScheduleKey)
                        .toList());

        assertEquals(List.of("Alpha|Ana", "alpha|Carlos", "Zeta|Bruno"),
                snapshot.programasEscola().stream()
                        .map(e -> e.nomeEscola() + "|" + e.nomeProfissional())
                        .toList());
        assertEquals(1, snapshot.programasEscola().get(0).atEscolaSemana().size());
        assertEquals("SEGUNDA", snapshot.programasEscola().get(0).atEscolaSemana().get(0).diaSemana());
        assertEquals(List.of(
                "08:00|09:00|MANHA|00000000-0000-0000-0000-000000000210",
                "09:00|10:00|MANHA|00000000-0000-0000-0000-000000000220",
                "09:00|10:00|TARDE|00000000-0000-0000-0000-000000000230"),
                snapshot.programasEscola().get(0).atEscolaSemana().get(0).atEscolaSemanaSchedule().stream()
                        .map(this::atEscolaScheduleKey)
                        .toList());

        assertEquals(List.of(
                uuid(3001), uuid(3002), uuid(3003)),
                snapshot.nucleoPatient().stream().map(NucleoPatientDTO::nucleoPatientId).toList());
        assertEquals(List.of(
                uuid(4001), uuid(4002)),
                snapshot.nucleoPatient().get(0).nucleoPatientResponsavel().stream()
                        .map(NucleoResponsavelDTO::responsavelId)
                        .toList());
        assertEquals(List.of(
                uuid(5001), uuid(5002)),
                snapshot.nucleoPatient().get(0).abordagens().stream()
                        .map(a -> a.abordagemId())
                        .toList());
    }

    @Test
    void shouldReturnSameSnapshotForDifferentInputOrders() {
        Scenario scenarioA = arrangeScenario(false);
        ProgramaAtendimentoDTO snapshotA = useCase.buildSnapshot(scenarioA.programa(), scenarioA.patientId());

        reset(programaSemanaPort, programaSemanaSchedulePort, programaEscolaPort, atEscolaSemanaPort,
                atEscolaSemanaSchedulePort, nucleoPatientPort, responsavelPort, abordagemPatientPort);
        setUp();

        Scenario scenarioB = arrangeScenario(true);
        ProgramaAtendimentoDTO snapshotB = useCase.buildSnapshot(scenarioB.programa(), scenarioB.patientId());

        assertEquals(snapshotA, snapshotB);
    }

    private Scenario arrangeScenario(boolean reverseOrder) {
        UUID programaId = uuid(1000);
        UUID patientId = uuid(2000);

        ProgramaAtendimento programa = ProgramaAtendimento.builder()
                .id(programaId)
                .patientId(patientId)
                .dataInicio(LocalDateTime.of(2026, 3, 27, 10, 0))
                .cadastroApp(SimNao.SIM)
                .atEscolar(SimNao.SIM)
                .observacao("snapshot")
                .build();

        ProgramaSemana semanaSegundaA = ProgramaSemana.builder()
                .id(uuid(1010))
                .programaAtendimentoId(programaId)
                .diaSemana(Semana.SEGUNDA)
                .build();
        ProgramaSemana semanaSegundaB = ProgramaSemana.builder()
                .id(uuid(1011))
                .programaAtendimentoId(programaId)
                .diaSemana(Semana.SEGUNDA)
                .build();
        ProgramaSemana semanaTerca = ProgramaSemana.builder()
                .id(uuid(1012))
                .programaAtendimentoId(programaId)
                .diaSemana(Semana.TERCA)
                .build();

        when(programaSemanaPort.findByProgramaAtendimentoId(programaId))
                .thenReturn(ordered(reverseOrder, List.of(semanaTerca, semanaSegundaB, semanaSegundaA)));

        List<ProgramaSemanaSchedule> segundaASchedules = ordered(reverseOrder, List.of(
                semanaSchedule(uuid(1111), semanaSegundaA.getId(), uuid(125), "08:00", "10:00", "TARDE"),
                semanaSchedule(uuid(1112), semanaSegundaA.getId(), uuid(130), "08:00", "10:00", "MANHA"),
                semanaSchedule(uuid(1113), semanaSegundaA.getId(), uuid(110), "08:00", "10:00", "MANHA")));
        List<ProgramaSemanaSchedule> segundaBSchedules = ordered(reverseOrder, List.of(
                semanaSchedule(uuid(1114), semanaSegundaB.getId(), uuid(120), "07:00", "08:00", "NOITE")));
        List<ProgramaSemanaSchedule> tercaSchedules = ordered(reverseOrder, List.of(
                semanaSchedule(uuid(1115), semanaTerca.getId(), uuid(140), "06:00", "07:00", "MANHA")));

        when(programaSemanaSchedulePort.findByProgramaSemanaId(semanaSegundaA.getId())).thenReturn(segundaASchedules);
        when(programaSemanaSchedulePort.findByProgramaSemanaId(semanaSegundaB.getId())).thenReturn(segundaBSchedules);
        when(programaSemanaSchedulePort.findByProgramaSemanaId(semanaTerca.getId())).thenReturn(tercaSchedules);

        ProgramaEscola escolaZeta = escola(uuid(2001), programaId, "Bruno", "Zeta");
        ProgramaEscola escolaAlphaCarlos = escola(uuid(2002), programaId, "Carlos", "alpha");
        ProgramaEscola escolaAlphaAna = escola(uuid(2003), programaId, "Ana", "Alpha");

        when(programaEscolaPort.findByProgramaAtendimentoId(programaId))
                .thenReturn(ordered(reverseOrder, List.of(escolaZeta, escolaAlphaCarlos, escolaAlphaAna)));

        AtEscolaSemana atSegundaA = atSemana(uuid(2101), escolaAlphaAna.getId(), Semana.SEGUNDA);
        AtEscolaSemana atSegundaB = atSemana(uuid(2102), escolaAlphaAna.getId(), Semana.SEGUNDA);
        when(atEscolaSemanaPort.findByProgramaEscolaId(escolaAlphaAna.getId()))
                .thenReturn(ordered(reverseOrder, List.of(atSegundaB, atSegundaA)));
        when(atEscolaSemanaPort.findByProgramaEscolaId(escolaAlphaCarlos.getId())).thenReturn(List.of());
        when(atEscolaSemanaPort.findByProgramaEscolaId(escolaZeta.getId())).thenReturn(List.of());

        List<AtEscolaSemanaSchedule> atSegundaASchedules = ordered(reverseOrder, List.of(
                atEscolaSchedule(uuid(2201), atSegundaA.getId(), uuid(230), "09:00", "10:00", "TARDE"),
                atEscolaSchedule(uuid(2202), atSegundaA.getId(), uuid(220), "09:00", "10:00", "MANHA")));
        List<AtEscolaSemanaSchedule> atSegundaBSchedules = ordered(reverseOrder, List.of(
                atEscolaSchedule(uuid(2203), atSegundaB.getId(), uuid(210), "08:00", "09:00", "MANHA")));
        when(atEscolaSemanaSchedulePort.findByAtEscolaSemanaId(atSegundaA.getId())).thenReturn(atSegundaASchedules);
        when(atEscolaSemanaSchedulePort.findByAtEscolaSemanaId(atSegundaB.getId())).thenReturn(atSegundaBSchedules);

        NucleoPatient nucleoA1 = nucleo(uuid(3001), patientId, uuid(3100));
        NucleoPatient nucleoA2 = nucleo(uuid(3002), patientId, uuid(3100));
        NucleoPatient nucleoB = nucleo(uuid(3003), patientId, uuid(3200));
        when(nucleoPatientPort.findAllByPatientId(patientId))
                .thenReturn(ordered(reverseOrder, List.of(nucleoB, nucleoA2, nucleoA1)));

        when(responsavelPort.findByNucleoPatientId(nucleoA1.getId()))
                .thenReturn(ordered(reverseOrder, List.of(
                        responsavel(uuid(4002), nucleoA1.getId(), ResponsavelRole.ADMINISTRADOR),
                        responsavel(uuid(4001), nucleoA1.getId(), ResponsavelRole.COORDENADOR))));
        when(responsavelPort.findByNucleoPatientId(nucleoA2.getId())).thenReturn(List.of());
        when(responsavelPort.findByNucleoPatientId(nucleoB.getId())).thenReturn(List.of());

        when(abordagemPatientPort.findByNucleoPatientId(nucleoA1.getId()))
                .thenReturn(ordered(reverseOrder, List.of(
                        abordagem(uuid(5002), nucleoA1.getId()),
                        abordagem(uuid(5001), nucleoA1.getId()))));
        when(abordagemPatientPort.findByNucleoPatientId(nucleoA2.getId())).thenReturn(List.of());
        when(abordagemPatientPort.findByNucleoPatientId(nucleoB.getId())).thenReturn(List.of());

        return new Scenario(programa, patientId);
    }

    private String semanaScheduleKey(ProgramaSemanaScheduleDTO dto) {
        return dto.horarioInicio() + "|" + dto.horarioTermino() + "|" + dto.turno() + "|" + dto.nucleoId();
    }

    private String atEscolaScheduleKey(AtEscolaSemanaScheduleDTO dto) {
        return dto.horarioInicio() + "|" + dto.horarioTermino() + "|" + dto.turno() + "|" + dto.nucleoId();
    }

    private static ProgramaSemanaSchedule semanaSchedule(
            UUID id, UUID programaSemanaId, UUID nucleoId, String inicio, String termino, String turno) {
        return ProgramaSemanaSchedule.builder()
                .id(id)
                .programaSemanaId(programaSemanaId)
                .nucleoId(nucleoId)
                .horarioInicio(inicio)
                .horarioTermino(termino)
                .turno(turno)
                .build();
    }

    private static ProgramaEscola escola(UUID id, UUID programaId, String profissional, String nomeEscola) {
        return ProgramaEscola.builder()
                .id(id)
                .programaAtendimentoId(programaId)
                .nomeProfissional(profissional)
                .nomeEscola(nomeEscola)
                .build();
    }

    private static AtEscolaSemana atSemana(UUID id, UUID programaEscolaId, Semana dia) {
        return AtEscolaSemana.builder()
                .id(id)
                .programaEscolaId(programaEscolaId)
                .diaSemana(dia)
                .build();
    }

    private static AtEscolaSemanaSchedule atEscolaSchedule(
            UUID id, UUID atEscolaSemanaId, UUID nucleoId, String inicio, String termino, String turno) {
        return AtEscolaSemanaSchedule.builder()
                .id(id)
                .atEscolaSemanaId(atEscolaSemanaId)
                .nucleoId(nucleoId)
                .horarioInicio(inicio)
                .horarioTermino(termino)
                .turno(turno)
                .build();
    }

    private static NucleoPatient nucleo(UUID nucleoPatientId, UUID patientId, UUID nucleoId) {
        return NucleoPatient.builder()
                .id(nucleoPatientId)
                .patientId(patientId)
                .nucleoId(nucleoId)
                .build();
    }

    private static NucleoPatientResponsavel responsavel(UUID responsavelId, UUID nucleoPatientId,
            ResponsavelRole role) {
        return NucleoPatientResponsavel.builder()
                .id(uuid(9000))
                .nucleoPatientId(nucleoPatientId)
                .responsavelId(responsavelId)
                .role(role)
                .build();
    }

    private static AbordagemPatient abordagem(UUID abordagemId, UUID nucleoPatientId) {
        return AbordagemPatient.builder()
                .id(uuid(9100))
                .nucleoPatientId(nucleoPatientId)
                .abordagemId(abordagemId)
                .build();
    }

    private static <T> List<T> ordered(boolean reverse, List<T> items) {
        if (!reverse) {
            return items;
        }
        List<T> copy = new ArrayList<>(items);
        Collections.reverse(copy);
        return copy;
    }

    private static UUID uuid(int value) {
        return UUID.fromString(String.format("00000000-0000-0000-0000-%012d", value));
    }

    private record Scenario(ProgramaAtendimento programa, UUID patientId) {
    }
}
