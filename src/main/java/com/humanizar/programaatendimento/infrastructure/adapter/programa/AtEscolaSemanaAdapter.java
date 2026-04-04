package com.humanizar.programaatendimento.infrastructure.adapter.programa;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.domain.model.programa.AtEscolaSemana;
import com.humanizar.programaatendimento.domain.port.programa.AtEscolaSemanaPort;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.AtEscolaSemanaEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.AtEscolaSemanaRepository;

@Component
public class AtEscolaSemanaAdapter implements AtEscolaSemanaPort {

    private final AtEscolaSemanaRepository atEscolaSemanaRepository;

    public AtEscolaSemanaAdapter(AtEscolaSemanaRepository atEscolaSemanaRepository) {
        this.atEscolaSemanaRepository = atEscolaSemanaRepository;
    }

    @Override
    public AtEscolaSemana save(AtEscolaSemana atEscolaSemana) {
        AtEscolaSemanaEntity entity = toEntity(atEscolaSemana);
        AtEscolaSemanaEntity saved = atEscolaSemanaRepository.save(entity);
        return toDomain(Objects.requireNonNull(saved, "Erro ao salvar at escola semana"));
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
    public void deleteById(UUID id) {
        atEscolaSemanaRepository.deleteById(id);
    }

    @Override
    public void deleteByProgramaEscolaId(UUID programaEscolaId) {
        atEscolaSemanaRepository.deleteByProgramaEscolaId(programaEscolaId);
    }

    private AtEscolaSemana toDomain(AtEscolaSemanaEntity entity) {
        return new AtEscolaSemana(
                entity.getId(),
                entity.getProgramaEscolaId(),
                entity.getDiaSemana(),
                null);
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
}
