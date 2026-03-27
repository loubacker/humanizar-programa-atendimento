package com.humanizar.programaatendimento.infrastructure.adapter.programa;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.domain.model.programa.AtEscolaSemana;
import com.humanizar.programaatendimento.domain.model.programa.AtEscolaSemanaSchedule;
import com.humanizar.programaatendimento.domain.port.programa.AtEscolaSemanaPort;
import com.humanizar.programaatendimento.domain.port.programa.AtEscolaSemanaSchedulePort;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.AtEscolaSemanaEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.AtEscolaSemanaRepository;

@Component
public class AtEscolaSemanaAdapter implements AtEscolaSemanaPort {

    private final AtEscolaSemanaRepository atEscolaSemanaRepository;
    private final AtEscolaSemanaSchedulePort atEscolaSemanaSchedulePort;

    public AtEscolaSemanaAdapter(AtEscolaSemanaRepository atEscolaSemanaRepository,
            AtEscolaSemanaSchedulePort atEscolaSemanaSchedulePort) {
        this.atEscolaSemanaRepository = atEscolaSemanaRepository;
        this.atEscolaSemanaSchedulePort = atEscolaSemanaSchedulePort;
    }

    @Override
    public AtEscolaSemana save(AtEscolaSemana atEscolaSemana) {
        AtEscolaSemanaEntity entity = toEntity(atEscolaSemana);
        AtEscolaSemanaEntity saved = atEscolaSemanaRepository.save(entity);
        AtEscolaSemanaEntity safe = Objects.requireNonNull(saved, "Erro ao salvar at escola semana");
        syncSchedules(safe.getId(), atEscolaSemana.getAtEscolaSemanaSchedule());
        return toDomain(safe);
    }

    @Override
    public List<AtEscolaSemana> saveAll(List<AtEscolaSemana> atEscolaSemana) {
        return atEscolaSemana.stream()
                .map(this::save)
                .toList();
    }

    @Override
    public List<AtEscolaSemana> findByProgramaEscolaId(UUID programaEscolaId) {
        return atEscolaSemanaRepository.findByProgramaEscolaId(programaEscolaId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteByProgramaEscolaId(UUID programaEscolaId) {
        List<AtEscolaSemanaEntity> semanaEntities = atEscolaSemanaRepository
                .findByProgramaEscolaId(programaEscolaId);
        for (AtEscolaSemanaEntity semanaEntity : semanaEntities) {
            atEscolaSemanaSchedulePort.deleteByAtEscolaSemanaId(semanaEntity.getId());
        }
        atEscolaSemanaRepository.deleteByProgramaEscolaId(programaEscolaId);
    }

    private AtEscolaSemana toDomain(AtEscolaSemanaEntity entity) {
        List<AtEscolaSemanaSchedule> schedules = atEscolaSemanaSchedulePort.findByAtEscolaSemanaId(entity.getId());
        return new AtEscolaSemana(
                entity.getId(),
                entity.getProgramaEscolaId(),
                entity.getDiaSemana(),
                schedules);
    }

    private AtEscolaSemanaEntity toEntity(AtEscolaSemana domain) {
        AtEscolaSemanaEntity entity = new AtEscolaSemanaEntity();
        entity.setId(domain.getId());
        entity.setProgramaEscolaId(Objects.requireNonNull(
                domain.getProgramaEscolaId(),
                "programaEscolaId e obrigatorio para persistir at escola semana"));
        entity.setDiaSemana(domain.getDiaSemana());
        return entity;
    }

    private void syncSchedules(UUID atEscolaSemanaId, List<AtEscolaSemanaSchedule> schedules) {
        atEscolaSemanaSchedulePort.deleteByAtEscolaSemanaId(atEscolaSemanaId);

        if (schedules == null || schedules.isEmpty()) {
            return;
        }

        List<AtEscolaSemanaSchedule> normalized = schedules.stream()
                .map(schedule -> AtEscolaSemanaSchedule.builder()
                        .id(schedule.getId())
                        .atEscolaSemanaId(atEscolaSemanaId)
                        .nucleoId(schedule.getNucleoId())
                        .horarioInicio(schedule.getHorarioInicio())
                        .horarioTermino(schedule.getHorarioTermino())
                        .turno(schedule.getTurno())
                        .build())
                .toList();

        atEscolaSemanaSchedulePort.saveAll(normalized);
    }
}
