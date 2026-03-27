package com.humanizar.programaatendimento.application.usecase.programa;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.humanizar.programaatendimento.domain.model.programa.AtEscolaSemana;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaEscola;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaSemana;
import com.humanizar.programaatendimento.domain.port.programa.AtEscolaSemanaPort;
import com.humanizar.programaatendimento.domain.port.programa.AtEscolaSemanaSchedulePort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaEscolaPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaSemanaPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaSemanaSchedulePort;

@Service
public class DeleteProgramaTreeUseCase {

    private final ProgramaSemanaPort programaSemanaPort;
    private final ProgramaSemanaSchedulePort programaSemanaSchedulePort;
    private final ProgramaEscolaPort programaEscolaPort;
    private final AtEscolaSemanaPort atEscolaSemanaPort;
    private final AtEscolaSemanaSchedulePort atEscolaSemanaSchedulePort;

    public DeleteProgramaTreeUseCase(
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

    public void execute(UUID programaId) {
        List<ProgramaSemana> semanas = programaSemanaPort.findByProgramaAtendimentoId(programaId);
        for (ProgramaSemana s : semanas) {
            programaSemanaSchedulePort.deleteByProgramaSemanaId(s.getId());
        }
        programaSemanaPort.deleteByProgramaAtendimentoId(programaId);

        List<ProgramaEscola> escolas = programaEscolaPort.findByProgramaAtendimentoId(programaId);
        for (ProgramaEscola e : escolas) {
            List<AtEscolaSemana> atSemanas = atEscolaSemanaPort.findByProgramaEscolaId(e.getId());
            for (AtEscolaSemana as : atSemanas) {
                atEscolaSemanaSchedulePort.deleteByAtEscolaSemanaId(as.getId());
            }
            atEscolaSemanaPort.deleteByProgramaEscolaId(e.getId());
        }
        programaEscolaPort.deleteByProgramaAtendimentoId(programaId);
    }
}

