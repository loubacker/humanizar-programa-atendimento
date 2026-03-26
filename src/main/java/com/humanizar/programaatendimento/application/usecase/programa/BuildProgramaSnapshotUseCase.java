package com.humanizar.programaatendimento.application.usecase.programa;

import java.util.List;
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
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtEscolaPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtSemanaPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaSemanaSchedulePort;

@Service
public class BuildProgramaSnapshotUseCase {

    private final ProgramaAtSemanaPort programaAtSemanaPort;
    private final ProgramaSemanaSchedulePort programaSemanaSchedulePort;
    private final ProgramaAtEscolaPort programaAtEscolaPort;
    private final AtEscolaSemanaPort atEscolaSemanaPort;
    private final AtEscolaSemanaSchedulePort atEscolaSemanaSchedulePort;
    private final NucleoPatientPort nucleoPatientPort;
    private final NucleoPatientResponsavelPort responsavelPort;
    private final AbordagemPatientPort abordagemPatientPort;

    public BuildProgramaSnapshotUseCase(
            ProgramaAtSemanaPort programaAtSemanaPort,
            ProgramaSemanaSchedulePort programaSemanaSchedulePort,
            ProgramaAtEscolaPort programaAtEscolaPort,
            AtEscolaSemanaPort atEscolaSemanaPort,
            AtEscolaSemanaSchedulePort atEscolaSemanaSchedulePort,
            NucleoPatientPort nucleoPatientPort,
            NucleoPatientResponsavelPort responsavelPort,
            AbordagemPatientPort abordagemPatientPort) {
        this.programaAtSemanaPort = programaAtSemanaPort;
        this.programaSemanaSchedulePort = programaSemanaSchedulePort;
        this.programaAtEscolaPort = programaAtEscolaPort;
        this.atEscolaSemanaPort = atEscolaSemanaPort;
        this.atEscolaSemanaSchedulePort = atEscolaSemanaSchedulePort;
        this.nucleoPatientPort = nucleoPatientPort;
        this.responsavelPort = responsavelPort;
        this.abordagemPatientPort = abordagemPatientPort;
    }

    public ProgramaAtendimentoDTO buildSnapshot(ProgramaAtendimento programa, UUID patientId) {
        List<ProgramaSemana> semanas = programaAtSemanaPort.findByProgramaAtendimentoId(programa.getId());
        List<ProgramaSemanaDTO> semanaDTOs = semanas.stream().map(this::toProgramaSemanaDTO).toList();

        List<ProgramaEscola> escolas = programaAtEscolaPort.findByProgramaAtendimentoId(programa.getId());
        List<ProgramaEscolaDTO> escolaDTOs = escolas.stream().map(this::toProgramaEscolaDTO).toList();

        List<NucleoPatient> nucleos = nucleoPatientPort.findAllByPatientId(patientId);
        List<NucleoPatientDTO> nucleoDTOs = nucleos.stream().map(this::toNucleoPatientDTO).toList();

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

    private ProgramaSemanaDTO toProgramaSemanaDTO(ProgramaSemana semana) {
        List<ProgramaSemanaSchedule> schedules = programaSemanaSchedulePort
                .findByProgramaSemanaId(semana.getId());
        return new ProgramaSemanaDTO(
                semana.getDiaSemana() != null ? semana.getDiaSemana().name() : null,
                schedules.stream().map(s -> new ProgramaSemanaScheduleDTO(
                        s.getNucleoId(), s.getHorarioInicio(), s.getHorarioTermino(), s.getTurno()))
                        .toList());
    }

    private ProgramaEscolaDTO toProgramaEscolaDTO(ProgramaEscola escola) {
        List<AtEscolaSemana> atSemanas = atEscolaSemanaPort.findByProgramaAtEscolaId(escola.getId());
        return new ProgramaEscolaDTO(
                escola.getNomeProfissional(),
                escola.getNomeEscola(),
                atSemanas.stream().map(this::toAtEscolaSemanaDTO).toList());
    }

    private AtEscolaSemanaDTO toAtEscolaSemanaDTO(AtEscolaSemana atSemana) {
        List<AtEscolaSemanaSchedule> schedules = atEscolaSemanaSchedulePort
                .findByAtEscolaSemanaId(atSemana.getId());
        return new AtEscolaSemanaDTO(
                atSemana.getDiaSemana() != null ? atSemana.getDiaSemana().name() : null,
                schedules.stream().map(s -> new AtEscolaSemanaScheduleDTO(
                        s.getNucleoId(), s.getHorarioInicio(), s.getHorarioTermino(), s.getTurno()))
                        .toList());
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
                        .toList(),
                abordagens.stream().map(a -> new AbordagemPatientDTO(
                        a.getNucleoPatientId(), a.getAbordagemId()))
                        .toList());
    }
}
