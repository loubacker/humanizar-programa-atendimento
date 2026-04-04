package com.humanizar.programaatendimento.infrastructure.adapter.programa;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaSemanaEntity;
import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.domain.model.programa.ProgramaSemana;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaSemanaPort;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.ProgramaSemanaRepository;

@Component
public class ProgramaSemanaAdapter implements ProgramaSemanaPort {

    private final ProgramaSemanaRepository programaSemanaRepository;

    public ProgramaSemanaAdapter(ProgramaSemanaRepository programaSemanaRepository) {
        this.programaSemanaRepository = programaSemanaRepository;
    }

    @Override
    public ProgramaSemana save(ProgramaSemana programaSemana) {
        ProgramaSemanaEntity entity = toEntity(programaSemana);
        ProgramaSemanaEntity saved = programaSemanaRepository.save(entity);
        return toDomain(Objects.requireNonNull(saved, "Erro ao salvar programa semana"));
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
    public void deleteById(UUID id) {
        programaSemanaRepository.deleteById(id);
    }

    @Override
    public void deleteByProgramaAtendimentoId(UUID programaAtendimentoId) {
        programaSemanaRepository.deleteByProgramaAtendimentoId(programaAtendimentoId);
    }

    private ProgramaSemana toDomain(ProgramaSemanaEntity entity) {
        return new ProgramaSemana(
                entity.getId(),
                entity.getProgramaAtendimentoId(),
                entity.getDiaSemana(),
                null);
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
}
