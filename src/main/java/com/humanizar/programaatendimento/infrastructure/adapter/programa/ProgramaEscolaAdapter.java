package com.humanizar.programaatendimento.infrastructure.adapter.programa;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaEscolaEntity;
import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.domain.model.programa.ProgramaEscola;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaEscolaPort;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.ProgramaEscolaRepository;

@Component
public class ProgramaEscolaAdapter implements ProgramaEscolaPort {

    private final ProgramaEscolaRepository programaEscolaRepository;

    public ProgramaEscolaAdapter(ProgramaEscolaRepository programaEscolaRepository) {
        this.programaEscolaRepository = programaEscolaRepository;
    }

    @Override
    public ProgramaEscola save(ProgramaEscola programaEscola) {
        ProgramaEscolaEntity entity = toEntity(programaEscola);
        ProgramaEscolaEntity saved = programaEscolaRepository.save(entity);
        return toDomain(Objects.requireNonNull(saved, "Erro ao salvar programa at escola"));
    }

    @Override
    public List<ProgramaEscola> saveAll(List<ProgramaEscola> programasEscola) {
        List<ProgramaEscolaEntity> entities = programasEscola.stream()
                .map(this::toEntity)
                .toList();
        return programaEscolaRepository.saveAll(entities).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<ProgramaEscola> findByProgramaAtendimentoId(UUID programaAtendimentoId) {
        return programaEscolaRepository.findByProgramaAtendimentoId(programaAtendimentoId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteByProgramaAtendimentoId(UUID programaAtendimentoId) {
        programaEscolaRepository.deleteByProgramaAtendimentoId(programaAtendimentoId);
    }

    private ProgramaEscola toDomain(ProgramaEscolaEntity entity) {
        return new ProgramaEscola(
                entity.getId(),
                entity.getProgramaAtendimentoId(),
                entity.getNomeProfissional(),
                entity.getNomeEscola(),
                null);
    }

    private ProgramaEscolaEntity toEntity(ProgramaEscola domain) {
        ProgramaEscolaEntity entity = new ProgramaEscolaEntity();
        entity.setId(domain.getId());
        entity.setProgramaAtendimentoId(Objects.requireNonNull(
                domain.getProgramaAtendimentoId(),
                "programaAtendimentoId e obrigatorio para persistir programa escola"));
        entity.setNomeProfissional(domain.getNomeProfissional());
        entity.setNomeEscola(domain.getNomeEscola());
        return entity;
    }
}
