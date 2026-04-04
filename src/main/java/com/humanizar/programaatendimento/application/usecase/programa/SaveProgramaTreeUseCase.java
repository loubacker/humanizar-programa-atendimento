package com.humanizar.programaatendimento.application.usecase.programa;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.humanizar.programaatendimento.application.inbound.dto.programa.AtEscolaSemanaDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.AtEscolaSemanaScheduleDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaEscolaDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaSemanaDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaSemanaScheduleDTO;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
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

@Service
public class SaveProgramaTreeUseCase {

    private final ProgramaSemanaPort programaSemanaPort;
    private final ProgramaSemanaSchedulePort programaSemanaSchedulePort;
    private final ProgramaEscolaPort programaEscolaPort;
    private final AtEscolaSemanaPort atEscolaSemanaPort;
    private final AtEscolaSemanaSchedulePort atEscolaSemanaSchedulePort;

    public SaveProgramaTreeUseCase(
            ProgramaSemanaPort programaSemanaPort,
            ProgramaSemanaSchedulePort programaSemanaSchedulePort,
            ProgramaEscolaPort programaEscolaPort,
            AtEscolaSemanaPort atEscolaSemanaPort,
            AtEscolaSemanaSchedulePort atEscolaSemanaSchedulePort) {
        this.programaSemanaPort = programaSemanaPort;
        this.programaSemanaSchedulePort = programaSemanaSchedulePort;
        this.programaEscolaPort = programaEscolaPort;
        this.atEscolaSemanaPort = atEscolaSemanaPort;
        this.atEscolaSemanaSchedulePort = atEscolaSemanaSchedulePort;
    }

    public void saveProgramasSemana(
            UUID programaId, List<ProgramaSemanaDTO> semanas, String correlationId) {
        validateNoDuplicateDiaSemana(
                semanas.stream().map(ProgramaSemanaDTO::diaSemana).toList(), correlationId);

        List<ProgramaSemana> currentSemanas = programaSemanaPort.findByProgramaAtendimentoId(programaId);
        Map<Semana, ProgramaSemana> currentByDiaSemana = currentSemanas.stream()
                .collect(Collectors.toMap(ProgramaSemana::getDiaSemana, Function.identity()));
        Set<Semana> incomingDias = new HashSet<>();

        for (ProgramaSemanaDTO semanaDTO : semanas) {
            Semana diaSemana = parseSemana(semanaDTO.diaSemana(), correlationId);
            incomingDias.add(diaSemana);

            ProgramaSemana currentSemana = currentByDiaSemana.get(diaSemana);
            ProgramaSemana savedSemana = programaSemanaPort.save(ProgramaSemana.builder()
                    .id(currentSemana != null ? currentSemana.getId() : null)
                    .programaAtendimentoId(programaId)
                    .diaSemana(diaSemana)
                    .build());

            updateProgramaSemanaSchedules(savedSemana.getId(), semanaDTO.programaSemanaSchedule());
        }

        for (ProgramaSemana currentSemana : currentSemanas) {
            if (!incomingDias.contains(currentSemana.getDiaSemana())) {
                programaSemanaSchedulePort.deleteByProgramaSemanaId(currentSemana.getId());
                programaSemanaPort.deleteById(currentSemana.getId());
            }
        }
    }

    public void saveProgramasEscola(
            UUID programaId, List<ProgramaEscolaDTO> escolas, String correlationId) {
        Set<String> escolasSeen = new HashSet<>();
        for (ProgramaEscolaDTO escolaDTO : escolas) {
            String escolaKey = Objects.toString(escolaDTO.nomeProfissional(), "")
                    + "|" + Objects.toString(escolaDTO.nomeEscola(), "");
            if (!escolasSeen.add(escolaKey)) {
                throw new ProgramaAtendimentoException(
                        ReasonCode.INBOUND_DUPLICATE_ITEM, correlationId,
                        "ProgramaEscola duplicado no payload: " + escolaKey);
            }
        }

        List<ProgramaEscola> currentEscolas = programaEscolaPort.findByProgramaAtendimentoId(programaId);
        Map<String, ProgramaEscola> currentByKey = currentEscolas.stream()
                .collect(Collectors.toMap(
                        escola -> Objects.toString(escola.getNomeProfissional(), "")
                                + "|" + Objects.toString(escola.getNomeEscola(), ""),
                        Function.identity()));
        Set<String> incomingKeys = new HashSet<>();

        for (ProgramaEscolaDTO escolaDTO : escolas) {
            String escolaKey = Objects.toString(escolaDTO.nomeProfissional(), "")
                    + "|" + Objects.toString(escolaDTO.nomeEscola(), "");
            incomingKeys.add(escolaKey);

            ProgramaEscola currentEscola = currentByKey.get(escolaKey);
            ProgramaEscola savedEscola = programaEscolaPort.save(ProgramaEscola.builder()
                    .id(currentEscola != null ? currentEscola.getId() : null)
                    .programaAtendimentoId(programaId)
                    .nomeProfissional(escolaDTO.nomeProfissional())
                    .nomeEscola(escolaDTO.nomeEscola())
                    .build());

            validateNoDuplicateDiaSemana(
                    escolaDTO.atEscolaSemana().stream().map(AtEscolaSemanaDTO::diaSemana).toList(),
                    correlationId);

            List<AtEscolaSemana> currentAtSemanas = atEscolaSemanaPort.findByProgramaEscolaId(savedEscola.getId());
            Map<Semana, AtEscolaSemana> currentAtSemanasByDia = currentAtSemanas.stream()
                    .collect(Collectors.toMap(AtEscolaSemana::getDiaSemana, Function.identity()));
            Set<Semana> incomingDias = new HashSet<>();

            for (AtEscolaSemanaDTO atSemanaDTO : escolaDTO.atEscolaSemana()) {
                Semana diaSemana = parseSemana(atSemanaDTO.diaSemana(), correlationId);
                incomingDias.add(diaSemana);

                AtEscolaSemana currentAtSemana = currentAtSemanasByDia.get(diaSemana);
                AtEscolaSemana savedAtSemana = atEscolaSemanaPort.save(AtEscolaSemana.builder()
                        .id(currentAtSemana != null ? currentAtSemana.getId() : null)
                        .programaEscolaId(savedEscola.getId())
                        .diaSemana(diaSemana)
                        .build());

                updateAtEscolaSemanaSchedules(savedAtSemana.getId(), atSemanaDTO.atEscolaSemanaSchedule());
            }

            for (AtEscolaSemana currentAtSemana : currentAtSemanas) {
                if (!incomingDias.contains(currentAtSemana.getDiaSemana())) {
                    atEscolaSemanaSchedulePort.deleteByAtEscolaSemanaId(currentAtSemana.getId());
                    atEscolaSemanaPort.deleteById(currentAtSemana.getId());
                }
            }
        }

        for (ProgramaEscola currentEscola : currentEscolas) {
            String escolaKey = Objects.toString(currentEscola.getNomeProfissional(), "")
                    + "|" + Objects.toString(currentEscola.getNomeEscola(), "");
            if (!incomingKeys.contains(escolaKey)) {
                List<AtEscolaSemana> currentAtSemanas = atEscolaSemanaPort.findByProgramaEscolaId(currentEscola.getId());
                for (AtEscolaSemana currentAtSemana : currentAtSemanas) {
                    atEscolaSemanaSchedulePort.deleteByAtEscolaSemanaId(currentAtSemana.getId());
                    atEscolaSemanaPort.deleteById(currentAtSemana.getId());
                }
                programaEscolaPort.deleteById(currentEscola.getId());
            }
        }
    }

    private void validateNoDuplicateDiaSemana(List<String> diasSemana, String correlationId) {
        Set<String> seen = new HashSet<>();
        for (String dia : diasSemana) {
            if (!seen.add(dia)) {
                throw new ProgramaAtendimentoException(
                        ReasonCode.INBOUND_DUPLICATE_ITEM, correlationId,
                        "diaSemana duplicado no payload: " + dia);
            }
        }
    }

    private Semana parseSemana(String value, String correlationId) {
        try {
            return Semana.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.INBOUND_INVALID_ENUM, correlationId,
                    "Valor de diaSemana invalido: " + value);
        }
    }

    private void updateProgramaSemanaSchedules(
            UUID programaSemanaId,
            List<ProgramaSemanaScheduleDTO> incomingSchedules) {
        programaSemanaSchedulePort.deleteByProgramaSemanaId(programaSemanaId);

        if (incomingSchedules == null || incomingSchedules.isEmpty()) {
            return;
        }

        List<ProgramaSemanaSchedule> schedules = incomingSchedules.stream()
                .map(schedule -> ProgramaSemanaSchedule.builder()
                        .id(null)
                        .programaSemanaId(programaSemanaId)
                        .nucleoId(schedule.nucleoId())
                        .horarioInicio(schedule.horarioInicio())
                        .horarioTermino(schedule.horarioTermino())
                        .turno(schedule.turno())
                        .build())
                .toList();
        programaSemanaSchedulePort.saveAll(schedules);
    }

    private void updateAtEscolaSemanaSchedules(
            UUID atEscolaSemanaId,
            List<AtEscolaSemanaScheduleDTO> incomingSchedules) {
        atEscolaSemanaSchedulePort.deleteByAtEscolaSemanaId(atEscolaSemanaId);

        if (incomingSchedules == null || incomingSchedules.isEmpty()) {
            return;
        }

        List<AtEscolaSemanaSchedule> schedules = incomingSchedules.stream()
                .map(schedule -> AtEscolaSemanaSchedule.builder()
                        .id(null)
                        .atEscolaSemanaId(atEscolaSemanaId)
                        .nucleoId(schedule.nucleoId())
                        .horarioInicio(schedule.horarioInicio())
                        .horarioTermino(schedule.horarioTermino())
                        .turno(schedule.turno())
                        .build())
                .toList();
        atEscolaSemanaSchedulePort.saveAll(schedules);
    }
}
