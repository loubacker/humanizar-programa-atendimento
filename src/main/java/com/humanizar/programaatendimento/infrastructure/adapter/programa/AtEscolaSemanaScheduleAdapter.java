package com.humanizar.programaatendimento.infrastructure.adapter.programa;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.domain.model.programa.AtEscolaSemanaSchedule;
import com.humanizar.programaatendimento.domain.port.programa.AtEscolaSemanaSchedulePort;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.AtEscolaSemanaScheduleEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.AtEscolaSemanaScheduleRepository;

@Component
public class AtEscolaSemanaScheduleAdapter implements AtEscolaSemanaSchedulePort {

    private final AtEscolaSemanaScheduleRepository atEscolaSemanaScheduleRepository;

    public AtEscolaSemanaScheduleAdapter(AtEscolaSemanaScheduleRepository atEscolaSemanaScheduleRepository) {
        this.atEscolaSemanaScheduleRepository = atEscolaSemanaScheduleRepository;
    }

    @Override
    public AtEscolaSemanaSchedule save(AtEscolaSemanaSchedule atEscolaSemanaSchedule) {
        AtEscolaSemanaScheduleEntity entity = toEntity(atEscolaSemanaSchedule);
        AtEscolaSemanaScheduleEntity saved = atEscolaSemanaScheduleRepository.save(entity);
        return toDomain(Objects.requireNonNull(saved, "Erro ao salvar at escola semana schedule"));
    }

    @Override
    public List<AtEscolaSemanaSchedule> saveAll(List<AtEscolaSemanaSchedule> atEscolaSemanaSchedule) {
        List<AtEscolaSemanaScheduleEntity> entities = atEscolaSemanaSchedule.stream()
                .map(this::toEntity)
                .toList();
        return atEscolaSemanaScheduleRepository.saveAll(entities).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<AtEscolaSemanaSchedule> findByAtEscolaSemanaId(UUID atEscolaSemanaId) {
        return atEscolaSemanaScheduleRepository.findByAtEscolaSemanaId(atEscolaSemanaId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteByAtEscolaSemanaId(UUID atEscolaSemanaId) {
        atEscolaSemanaScheduleRepository.deleteByAtEscolaSemanaId(atEscolaSemanaId);
    }

    private AtEscolaSemanaSchedule toDomain(AtEscolaSemanaScheduleEntity entity) {
        return new AtEscolaSemanaSchedule(
                entity.getId(),
                entity.getAtEscolaSemanaId(),
                entity.getNucleoId(),
                entity.getHorarioInicio(),
                entity.getHorarioTermino(),
                entity.getTurno());
    }

    private AtEscolaSemanaScheduleEntity toEntity(AtEscolaSemanaSchedule domain) {
        AtEscolaSemanaScheduleEntity entity = new AtEscolaSemanaScheduleEntity();
        entity.setId(domain.getId());
        entity.setAtEscolaSemanaId(domain.getAtEscolaSemanaId());
        entity.setNucleoId(domain.getNucleoId());
        entity.setHorarioInicio(domain.getHorarioInicio());
        entity.setHorarioTermino(domain.getHorarioTermino());
        entity.setTurno(domain.getTurno());
        return entity;
    }
}
