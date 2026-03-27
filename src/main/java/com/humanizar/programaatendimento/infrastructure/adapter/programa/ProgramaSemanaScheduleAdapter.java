package com.humanizar.programaatendimento.infrastructure.adapter.programa;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.domain.model.programa.ProgramaSemanaSchedule;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaSemanaSchedulePort;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaSemanaScheduleEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.ProgramaSemanaScheduleRepository;

@Component
public class ProgramaSemanaScheduleAdapter implements ProgramaSemanaSchedulePort {

    private final ProgramaSemanaScheduleRepository programaSemanaScheduleRepository;

    public ProgramaSemanaScheduleAdapter(ProgramaSemanaScheduleRepository programaSemanaScheduleRepository) {
        this.programaSemanaScheduleRepository = programaSemanaScheduleRepository;
    }

    @Override
    public ProgramaSemanaSchedule save(ProgramaSemanaSchedule programaSemanaSchedule) {
        ProgramaSemanaScheduleEntity entity = toEntity(programaSemanaSchedule);
        ProgramaSemanaScheduleEntity saved = programaSemanaScheduleRepository.save(entity);
        return toDomain(Objects.requireNonNull(saved, "Erro ao salvar programa semana schedule"));
    }

    @Override
    public List<ProgramaSemanaSchedule> saveAll(List<ProgramaSemanaSchedule> programaSemanaSchedule) {
        List<ProgramaSemanaScheduleEntity> entities = programaSemanaSchedule.stream()
                .map(this::toEntity)
                .toList();
        return programaSemanaScheduleRepository.saveAll(entities).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<ProgramaSemanaSchedule> findByProgramaSemanaId(UUID programaSemanaId) {
        return programaSemanaScheduleRepository.findByProgramaSemanaId(programaSemanaId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteByProgramaSemanaId(UUID programaSemanaId) {
        programaSemanaScheduleRepository.deleteByProgramaSemanaId(programaSemanaId);
    }

    private ProgramaSemanaSchedule toDomain(ProgramaSemanaScheduleEntity entity) {
        return new ProgramaSemanaSchedule(
                entity.getId(),
                entity.getProgramaSemanaId(),
                entity.getNucleoId(),
                entity.getHorarioInicio(),
                entity.getHorarioTermino(),
                entity.getTurno());
    }

    private ProgramaSemanaScheduleEntity toEntity(ProgramaSemanaSchedule domain) {
        ProgramaSemanaScheduleEntity entity = new ProgramaSemanaScheduleEntity();
        entity.setId(domain.getId());
        entity.setProgramaSemanaId(domain.getProgramaSemanaId());
        entity.setNucleoId(domain.getNucleoId());
        entity.setHorarioInicio(domain.getHorarioInicio());
        entity.setHorarioTermino(domain.getHorarioTermino());
        entity.setTurno(domain.getTurno());
        return entity;
    }
}
