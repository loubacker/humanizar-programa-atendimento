package com.humanizar.programaatendimento.application.usecase.programa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.humanizar.programaatendimento.application.inbound.dto.nucleo.AbordagemPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoResponsavelDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.AtEscolaSemanaDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.AtEscolaSemanaScheduleDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaEscolaDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaSemanaDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaSemanaScheduleDTO;
import com.humanizar.programaatendimento.domain.model.enums.Semana;
import com.humanizar.programaatendimento.domain.model.nucleo.AbordagemPatient;
import com.humanizar.programaatendimento.domain.model.nucleo.NucleoPatient;
import com.humanizar.programaatendimento.domain.model.nucleo.NucleoPatientResponsavel;
import com.humanizar.programaatendimento.domain.model.programa.AtEscolaSemana;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaAtendimento;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaEscola;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaSemana;
import com.humanizar.programaatendimento.domain.port.nucleo.AbordagemPatientPort;
import com.humanizar.programaatendimento.domain.port.nucleo.NucleoPatientPort;
import com.humanizar.programaatendimento.domain.port.nucleo.NucleoPatientResponsavelPort;
import com.humanizar.programaatendimento.domain.port.programa.AtEscolaSemanaPort;
import com.humanizar.programaatendimento.domain.port.programa.AtEscolaSemanaSchedulePort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaEscolaPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaSemanaPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaSemanaSchedulePort;

@Service
public class BuildProgramaSnapshotUseCase {
    private static final Comparator<String> STRING_COMPARATOR = Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER);

    private static final Comparator<String> TIME_COMPARATOR = Comparator.nullsLast(Comparator.naturalOrder());

    private static final Comparator<String> UUID_TEXT_COMPARATOR = Comparator.nullsLast(Comparator.naturalOrder());

    private static final Comparator<ProgramaSemanaScheduleDTO> PROGRAMA_SEMANA_SCHEDULE_COMPARATOR = Comparator
            .comparing(ProgramaSemanaScheduleDTO::horarioInicio, TIME_COMPARATOR)
            .thenComparing(ProgramaSemanaScheduleDTO::horarioTermino, TIME_COMPARATOR)
            .thenComparing(ProgramaSemanaScheduleDTO::turno, STRING_COMPARATOR)
            .thenComparing(s -> uuidText(s.nucleoId()), UUID_TEXT_COMPARATOR);

    private static final Comparator<AtEscolaSemanaScheduleDTO> AT_ESCOLA_SEMANA_SCHEDULE_COMPARATOR = Comparator
            .comparing(AtEscolaSemanaScheduleDTO::horarioInicio, TIME_COMPARATOR)
            .thenComparing(AtEscolaSemanaScheduleDTO::horarioTermino, TIME_COMPARATOR)
            .thenComparing(AtEscolaSemanaScheduleDTO::turno, STRING_COMPARATOR)
            .thenComparing(s -> uuidText(s.nucleoId()), UUID_TEXT_COMPARATOR);

    private static final Comparator<ProgramaEscolaDTO> PROGRAMA_ESCOLA_COMPARATOR = Comparator
            .comparing(ProgramaEscolaDTO::nomeEscola, STRING_COMPARATOR)
            .thenComparing(ProgramaEscolaDTO::nomeProfissional, STRING_COMPARATOR);

    private static final Comparator<NucleoPatientDTO> NUCLEO_PATIENT_COMPARATOR = Comparator
            .comparing((NucleoPatientDTO n) -> uuidText(n.nucleoId()), UUID_TEXT_COMPARATOR)
            .thenComparing(n -> uuidText(n.nucleoPatientId()), UUID_TEXT_COMPARATOR);

    private static final Comparator<NucleoResponsavelDTO> NUCLEO_RESPONSAVEL_COMPARATOR = Comparator
            .comparing((NucleoResponsavelDTO r) -> uuidText(r.responsavelId()), UUID_TEXT_COMPARATOR)
            .thenComparing(NucleoResponsavelDTO::role, STRING_COMPARATOR);

    private static final Comparator<AbordagemPatientDTO> ABORDAGEM_COMPARATOR = Comparator
            .comparing((AbordagemPatientDTO a) -> uuidText(a.abordagemId()), UUID_TEXT_COMPARATOR)
            .thenComparing(a -> uuidText(a.nucleoPatientId()), UUID_TEXT_COMPARATOR);

    private final ProgramaSemanaPort programaSemanaPort;
    private final ProgramaSemanaSchedulePort programaSemanaSchedulePort;
    private final ProgramaEscolaPort programaEscolaPort;
    private final AtEscolaSemanaPort atEscolaSemanaPort;
    private final AtEscolaSemanaSchedulePort atEscolaSemanaSchedulePort;
    private final NucleoPatientPort nucleoPatientPort;
    private final NucleoPatientResponsavelPort responsavelPort;
    private final AbordagemPatientPort abordagemPatientPort;

    public BuildProgramaSnapshotUseCase(
            ProgramaSemanaPort programaSemanaPort,
            ProgramaSemanaSchedulePort programaSemanaSchedulePort,
            ProgramaEscolaPort programaEscolaPort,
            AtEscolaSemanaPort atEscolaSemanaPort,
            AtEscolaSemanaSchedulePort atEscolaSemanaSchedulePort,
            NucleoPatientPort nucleoPatientPort,
            NucleoPatientResponsavelPort responsavelPort,
            AbordagemPatientPort abordagemPatientPort) {
        this.programaSemanaPort = programaSemanaPort;
        this.programaSemanaSchedulePort = programaSemanaSchedulePort;
        this.programaEscolaPort = programaEscolaPort;
        this.atEscolaSemanaPort = atEscolaSemanaPort;
        this.atEscolaSemanaSchedulePort = atEscolaSemanaSchedulePort;
        this.nucleoPatientPort = nucleoPatientPort;
        this.responsavelPort = responsavelPort;
        this.abordagemPatientPort = abordagemPatientPort;
    }

    public ProgramaAtendimentoDTO buildSnapshot(ProgramaAtendimento programa, UUID patientId) {
        List<ProgramaSemana> semanas = programaSemanaPort.findByProgramaAtendimentoId(programa.getId());
        List<ProgramaSemanaDTO> semanaDTOs = buildSemanas(semanas);

        List<ProgramaEscola> escolas = programaEscolaPort.findByProgramaAtendimentoId(programa.getId());
        List<ProgramaEscolaDTO> escolaDTOs = escolas.stream()
                .map(this::toProgramaEscolaDTO)
                .sorted(PROGRAMA_ESCOLA_COMPARATOR)
                .toList();

        List<NucleoPatient> nucleos = nucleoPatientPort.findAllByPatientId(patientId);
        List<NucleoPatientDTO> nucleoDTOs = nucleos.stream()
                .map(this::toNucleoPatientDTO)
                .sorted(NUCLEO_PATIENT_COMPARATOR)
                .toList();

        return new ProgramaAtendimentoDTO(
                programa.getPatientId(),
                programa.getDataInicio() != null ? programa.getDataInicio().toString() : null,
                programa.getCadastroApp(),
                programa.getAtEscolar(),
                programa.getObservacao(),
                semanaDTOs,
                escolaDTOs,
                nucleoDTOs);
    }

    private List<ProgramaSemanaDTO> buildSemanas(List<ProgramaSemana> semanas) {
        Map<Semana, List<ProgramaSemanaScheduleDTO>> grouped = new TreeMap<>();
        for (ProgramaSemana semana : semanas) {
            List<ProgramaSemanaScheduleDTO> scheduleDTOs = programaSemanaSchedulePort
                    .findByProgramaSemanaId(semana.getId()).stream()
                    .map(s -> new ProgramaSemanaScheduleDTO(
                            s.getNucleoId(), s.getHorarioInicio(),
                            s.getHorarioTermino(), s.getTurno()))
                    .toList();
            grouped.merge(semana.getDiaSemana(), new ArrayList<>(scheduleDTOs), (a, b) -> {
                a.addAll(b);
                return a;
            });
        }
        return grouped.entrySet().stream()
                .map(e -> new ProgramaSemanaDTO(e.getKey().name(),
                        e.getValue().stream()
                                .sorted(PROGRAMA_SEMANA_SCHEDULE_COMPARATOR)
                                .toList()))
                .toList();
    }

    private ProgramaEscolaDTO toProgramaEscolaDTO(ProgramaEscola escola) {
        List<AtEscolaSemana> atSemanas = atEscolaSemanaPort.findByProgramaEscolaId(escola.getId());
        return new ProgramaEscolaDTO(
                escola.getNomeProfissional(),
                escola.getNomeEscola(),
                buildAtEscolaSemanas(atSemanas));
    }

    private List<AtEscolaSemanaDTO> buildAtEscolaSemanas(List<AtEscolaSemana> atSemanas) {
        Map<Semana, List<AtEscolaSemanaScheduleDTO>> grouped = new TreeMap<>();
        for (AtEscolaSemana atSemana : atSemanas) {
            List<AtEscolaSemanaScheduleDTO> scheduleDTOs = atEscolaSemanaSchedulePort
                    .findByAtEscolaSemanaId(atSemana.getId()).stream()
                    .map(s -> new AtEscolaSemanaScheduleDTO(
                            s.getNucleoId(), s.getHorarioInicio(),
                            s.getHorarioTermino(), s.getTurno()))
                    .toList();
            grouped.merge(atSemana.getDiaSemana(), new ArrayList<>(scheduleDTOs), (a, b) -> {
                a.addAll(b);
                return a;
            });
        }
        return grouped.entrySet().stream()
                .map(e -> new AtEscolaSemanaDTO(e.getKey().name(),
                        e.getValue().stream()
                                .sorted(AT_ESCOLA_SEMANA_SCHEDULE_COMPARATOR)
                                .toList()))
                .toList();
    }

    private NucleoPatientDTO toNucleoPatientDTO(NucleoPatient nucleo) {
        List<NucleoPatientResponsavel> responsaveis = responsavelPort
                .findByNucleoPatientId(nucleo.getId());
        List<AbordagemPatient> abordagens = abordagemPatientPort
                .findByNucleoPatientId(nucleo.getId());
        return new NucleoPatientDTO(
                nucleo.getId(),
                nucleo.getPatientId(),
                nucleo.getNucleoId(),
                responsaveis.stream().map(r -> new NucleoResponsavelDTO(
                        r.getResponsavelId(),
                        r.getRole() != null ? r.getRole().name() : null))
                        .sorted(NUCLEO_RESPONSAVEL_COMPARATOR)
                        .toList(),
                abordagens.stream().map(a -> new AbordagemPatientDTO(
                        a.getNucleoPatientId(), a.getAbordagemId()))
                        .sorted(ABORDAGEM_COMPARATOR)
                        .toList());
    }

    private static String uuidText(UUID value) {
        return value == null ? null : value.toString();
    }
}
