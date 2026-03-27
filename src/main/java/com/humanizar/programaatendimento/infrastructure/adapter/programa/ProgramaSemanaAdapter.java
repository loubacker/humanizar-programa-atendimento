package com.humanizar.programaatendimento.infrastructure.adapter.programa;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaSemanaEntity;
import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.domain.model.programa.ProgramaSemana;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaSemanaSchedule;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaSemanaPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaSemanaSchedulePort;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.ProgramaSemanaRepository;

@Component
public class ProgramaSemanaAdapter implements ProgramaSemanaPort {

    private final ProgramaSemanaRepository programaSemanaRepository;
    private final ProgramaSemanaSchedulePort programaSemanaSchedulePort;

    public ProgramaSemanaAdapter(ProgramaSemanaRepository programaSemanaRepository,
            ProgramaSemanaSchedulePort programaSemanaSchedulePort) {
        this.programaSemanaRepository = programaSemanaRepository;
        this.programaSemanaSchedulePort = programaSemanaSchedulePort;
    }

    @Override
    public ProgramaSemana save(ProgramaSemana programaSemana) {
        ProgramaSemanaEntity entity = toEntity(programaSemana);
        ProgramaSemanaEntity saved = programaSemanaRepository.save(entity);
        ProgramaSemanaEntity safe = Objects.requireNonNull(saved, "Erro ao salvar programa semana");
        syncSchedules(safe.getId(), programaSemana.getProgramaSemanaSchedule());
        return toDomain(safe);
    }

    @Override
    public List<ProgramaSemana> saveAll(List<ProgramaSemana> programasSemana) {
        return programasSemana.stream()
                .map(this::save)
                .toList();
    }

    @Override
    public List<ProgramaSemana> findByProgramaAtendimentoId(UUID programaAtendimentoId) {
        return programaSemanaRepository.findByProgramaAtendimentoId(programaAtendimentoId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteByProgramaAtendimentoId(UUID programaAtendimentoId) {
        List<ProgramaSemanaEntity> semanaEntities = programaSemanaRepository
                .findByProgramaAtendimentoId(programaAtendimentoId);
        for (ProgramaSemanaEntity semanaEntity : semanaEntities) {
            programaSemanaSchedulePort.deleteByProgramaSemanaId(semanaEntity.getId());
        }
        programaSemanaRepository.deleteByProgramaAtendimentoId(programaAtendimentoId);
    }

    private ProgramaSemana toDomain(ProgramaSemanaEntity entity) {
        List<ProgramaSemanaSchedule> schedules = programaSemanaSchedulePort.findByProgramaSemanaId(entity.getId());
        return new ProgramaSemana(
                entity.getId(),
                entity.getProgramaAtendimentoId(),
                entity.getDiaSemana(),
                schedules);
    }

    private ProgramaSemanaEntity toEntity(ProgramaSemana domain) {
        ProgramaSemanaEntity entity = new ProgramaSemanaEntity();
        entity.setId(domain.getId());
        entity.setProgramaAtendimentoId(Objects.requireNonNull(
                domain.getProgramaAtendimentoId(),
                "programaAtendimentoId e obrigatorio para persistir programa semana"));
        entity.setDiaSemana(domain.getDiaSemana());
        return entity;
    }

    private void syncSchedules(UUID programaSemanaId, List<ProgramaSemanaSchedule> schedules) {
        programaSemanaSchedulePort.deleteByProgramaSemanaId(programaSemanaId);

        if (schedules == null || schedules.isEmpty()) {
            return;
        }

        List<ProgramaSemanaSchedule> normalized = schedules.stream()
                .map(schedule -> ProgramaSemanaSchedule.builder()
                        .id(schedule.getId())
                        .programaSemanaId(programaSemanaId)
                        .nucleoId(schedule.getNucleoId())
                        .horarioInicio(schedule.getHorarioInicio())
                        .horarioTermino(schedule.getHorarioTermino())
                        .turno(schedule.getTurno())
                        .build())
                .toList();

        programaSemanaSchedulePort.saveAll(normalized);
    }
}
