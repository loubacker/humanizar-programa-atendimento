package com.humanizar.programaatendimento.application.usecase.programa;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.humanizar.programaatendimento.domain.model.programa.AtEscolaSemana;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaEscola;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaSemana;
import com.humanizar.programaatendimento.domain.port.programa.AtEscolaSemanaPort;
import com.humanizar.programaatendimento.domain.port.programa.AtEscolaSemanaSchedulePort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtEscolaPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtSemanaPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaSemanaSchedulePort;

@Service
public class DeleteProgramaTreeUseCase {

    private final ProgramaAtSemanaPort programaAtSemanaPort;
    private final ProgramaSemanaSchedulePort programaSemanaSchedulePort;
    private final ProgramaAtEscolaPort programaAtEscolaPort;
    private final AtEscolaSemanaPort atEscolaSemanaPort;
    private final AtEscolaSemanaSchedulePort atEscolaSemanaSchedulePort;

    public DeleteProgramaTreeUseCase(
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

    public void execute(UUID programaId) {
        List<ProgramaSemana> semanas = programaAtSemanaPort.findByProgramaAtendimentoId(programaId);
        for (ProgramaSemana s : semanas) {
            programaSemanaSchedulePort.deleteByProgramaSemanaId(s.getId());
        }
        programaAtSemanaPort.deleteByProgramaAtendimentoId(programaId);

        List<ProgramaEscola> escolas = programaAtEscolaPort.findByProgramaAtendimentoId(programaId);
        for (ProgramaEscola e : escolas) {
            List<AtEscolaSemana> atSemanas = atEscolaSemanaPort.findByProgramaAtEscolaId(e.getId());
            for (AtEscolaSemana as : atSemanas) {
                atEscolaSemanaSchedulePort.deleteByAtEscolaSemanaId(as.getId());
            }
            atEscolaSemanaPort.deleteByProgramaAtEscolaId(e.getId());
        }
        programaAtEscolaPort.deleteByProgramaAtendimentoId(programaId);
    }
}
