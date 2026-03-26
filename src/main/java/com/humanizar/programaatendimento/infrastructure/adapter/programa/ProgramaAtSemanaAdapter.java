package com.humanizar.programaatendimento.infrastructure.adapter.programa;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.domain.model.programa.ProgramaSemana;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaSemanaSchedule;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtSemanaPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaSemanaSchedulePort;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaAtSemanaEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.ProgramaAtSemanaRepository;

@Component
public class ProgramaAtSemanaAdapter implements ProgramaAtSemanaPort {

    private final ProgramaAtSemanaRepository programaAtSemanaRepository;
    private final ProgramaSemanaSchedulePort programaSemanaSchedulePort;

    public ProgramaAtSemanaAdapter(ProgramaAtSemanaRepository programaAtSemanaRepository,
            ProgramaSemanaSchedulePort programaSemanaSchedulePort) {
        this.programaAtSemanaRepository = programaAtSemanaRepository;
        this.programaSemanaSchedulePort = programaSemanaSchedulePort;
    }

    @Override
    public ProgramaSemana save(ProgramaSemana programaAtSemana) {
        ProgramaAtSemanaEntity entity = toEntity(programaAtSemana);
        ProgramaAtSemanaEntity saved = programaAtSemanaRepository.save(entity);
        ProgramaAtSemanaEntity safe = Objects.requireNonNull(saved, "Erro ao salvar programa semana");
        syncSchedules(safe.getId(), programaAtSemana.getProgramaSemanaSchedule());
        return toDomain(safe);
    }

    @Override
    public List<ProgramaSemana> saveAll(List<ProgramaSemana> programasAtSemana) {
        return programasAtSemana.stream()
                .map(this::save)
                .toList();
    }

    @Override
    public List<ProgramaSemana> findByProgramaAtendimentoId(UUID programaAtendimentoId) {
        return programaAtSemanaRepository.findByProgramaAtendimentoId(programaAtendimentoId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteByProgramaAtendimentoId(UUID programaAtendimentoId) {
        List<ProgramaAtSemanaEntity> semanaEntities = programaAtSemanaRepository
                .findByProgramaAtendimentoId(programaAtendimentoId);
        for (ProgramaAtSemanaEntity semanaEntity : semanaEntities) {
            programaSemanaSchedulePort.deleteByProgramaSemanaId(semanaEntity.getId());
        }
        programaAtSemanaRepository.deleteByProgramaAtendimentoId(programaAtendimentoId);
    }

    private ProgramaSemana toDomain(ProgramaAtSemanaEntity entity) {
        List<ProgramaSemanaSchedule> schedules = programaSemanaSchedulePort.findByProgramaSemanaId(entity.getId());
        return new ProgramaSemana(
                entity.getId(),
                entity.getProgramaAtendimentoId(),
                entity.getDiaSemana(),
                schedules);
    }

    private ProgramaAtSemanaEntity toEntity(ProgramaSemana domain) {
        ProgramaAtSemanaEntity entity = new ProgramaAtSemanaEntity();
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
                        .programaAtSemanaId(programaSemanaId)
                        .nucleoId(schedule.getNucleoId())
                        .horarioInicio(schedule.getHorarioInicio())
                        .horarioTermino(schedule.getHorarioTermino())
                        .turno(schedule.getTurno())
                        .build())
                .toList();

        programaSemanaSchedulePort.saveAll(normalized);
    }
}
