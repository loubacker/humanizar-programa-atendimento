package com.humanizar.programaatendimento.application.usecase.programa;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.humanizar.programaatendimento.application.inbound.dto.programa.AtEscolaSemanaDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaEscolaDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaSemanaDTO;
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
        for (ProgramaSemanaDTO semanaDTO : semanas) {
            UUID semanaId = UUID.randomUUID();
            ProgramaSemana semana = ProgramaSemana.builder()
                    .id(semanaId)
                    .programaAtendimentoId(programaId)
                    .diaSemana(parseSemana(semanaDTO.diaSemana(), correlationId))
                    .build();
            programaSemanaPort.save(semana);

            List<ProgramaSemanaSchedule> schedules = semanaDTO.programaSemanaSchedule().stream()
                    .map(s -> ProgramaSemanaSchedule.builder()
                            .id(UUID.randomUUID())
                            .programaSemanaId(semanaId)
                            .nucleoId(s.nucleoId())
                            .horarioInicio(s.horarioInicio())
                            .horarioTermino(s.horarioTermino())
                            .turno(s.turno())
                            .build())
                    .toList();
            if (!schedules.isEmpty()) {
                programaSemanaSchedulePort.saveAll(schedules);
            }
        }
    }

    public void saveProgramasEscola(
            UUID programaId, List<ProgramaEscolaDTO> escolas, String correlationId) {
        for (ProgramaEscolaDTO escolaDTO : escolas) {
            UUID escolaId = UUID.randomUUID();
            ProgramaEscola escola = ProgramaEscola.builder()
                    .id(escolaId)
                    .programaAtendimentoId(programaId)
                    .nomeProfissional(escolaDTO.nomeProfissional())
                    .nomeEscola(escolaDTO.nomeEscola())
                    .build();
            programaEscolaPort.save(escola);

            validateNoDuplicateDiaSemana(
                    escolaDTO.atEscolaSemana().stream().map(AtEscolaSemanaDTO::diaSemana).toList(),
                    correlationId);
            for (AtEscolaSemanaDTO atSemanaDTO : escolaDTO.atEscolaSemana()) {
                UUID atSemanaId = UUID.randomUUID();
                AtEscolaSemana atSemana = AtEscolaSemana.builder()
                        .id(atSemanaId)
                        .programaEscolaId(escolaId)
                        .diaSemana(parseSemana(atSemanaDTO.diaSemana(), correlationId))
                        .build();
                atEscolaSemanaPort.save(atSemana);

                List<AtEscolaSemanaSchedule> schedules = atSemanaDTO.atEscolaSemanaSchedule().stream()
                        .map(s -> AtEscolaSemanaSchedule.builder()
                                .id(UUID.randomUUID())
                                .atEscolaSemanaId(atSemanaId)
                                .nucleoId(s.nucleoId())
                                .horarioInicio(s.horarioInicio())
                                .horarioTermino(s.horarioTermino())
                                .turno(s.turno())
                                .build())
                        .toList();
                if (!schedules.isEmpty()) {
                    atEscolaSemanaSchedulePort.saveAll(schedules);
                }
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
}

