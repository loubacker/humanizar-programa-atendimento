package com.humanizar.programaatendimento.application.usecase.programa;

import java.util.List;
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
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtEscolaPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtSemanaPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaSemanaSchedulePort;

@Service
public class SaveProgramaTreeUseCase {

    private final ProgramaAtSemanaPort programaAtSemanaPort;
    private final ProgramaSemanaSchedulePort programaSemanaSchedulePort;
    private final ProgramaAtEscolaPort programaAtEscolaPort;
    private final AtEscolaSemanaPort atEscolaSemanaPort;
    private final AtEscolaSemanaSchedulePort atEscolaSemanaSchedulePort;

    public SaveProgramaTreeUseCase(
            ProgramaAtSemanaPort programaAtSemanaPort,
            ProgramaSemanaSchedulePort programaSemanaSchedulePort,
            ProgramaAtEscolaPort programaAtEscolaPort,
            AtEscolaSemanaPort atEscolaSemanaPort,
            AtEscolaSemanaSchedulePort atEscolaSemanaSchedulePort) {
        this.programaAtSemanaPort = programaAtSemanaPort;
        this.programaSemanaSchedulePort = programaSemanaSchedulePort;
        this.programaAtEscolaPort = programaAtEscolaPort;
        this.atEscolaSemanaPort = atEscolaSemanaPort;
        this.atEscolaSemanaSchedulePort = atEscolaSemanaSchedulePort;
    }

    public void saveProgramasSemana(
            UUID programaId, List<ProgramaSemanaDTO> semanas, String correlationId) {
        for (ProgramaSemanaDTO semanaDTO : semanas) {
            UUID semanaId = UUID.randomUUID();
            ProgramaSemana semana = ProgramaSemana.builder()
                    .id(semanaId)
                    .programaAtendimentoId(programaId)
                    .diaSemana(parseSemana(semanaDTO.diaSemana(), correlationId))
                    .build();
            programaAtSemanaPort.save(semana);

            List<ProgramaSemanaSchedule> schedules = semanaDTO.programaSemanaSchedule().stream()
                    .map(s -> ProgramaSemanaSchedule.builder()
                            .id(UUID.randomUUID())
                            .programaAtSemanaId(semanaId)
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
            programaAtEscolaPort.save(escola);

            for (AtEscolaSemanaDTO atSemanaDTO : escolaDTO.atEscolaSemana()) {
                UUID atSemanaId = UUID.randomUUID();
                AtEscolaSemana atSemana = AtEscolaSemana.builder()
                        .id(atSemanaId)
                        .programaAtEscolaId(escolaId)
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
