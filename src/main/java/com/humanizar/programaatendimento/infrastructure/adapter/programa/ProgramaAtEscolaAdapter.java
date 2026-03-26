package com.humanizar.programaatendimento.infrastructure.adapter.programa;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.domain.model.programa.ProgramaEscola;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtEscolaPort;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaAtEscolaEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.ProgramaAtEscolaRepository;

@Component
public class ProgramaAtEscolaAdapter implements ProgramaAtEscolaPort {

    private final ProgramaAtEscolaRepository programaAtEscolaRepository;

    public ProgramaAtEscolaAdapter(ProgramaAtEscolaRepository programaAtEscolaRepository) {
        this.programaAtEscolaRepository = programaAtEscolaRepository;
    }

    @Override
    public ProgramaEscola save(ProgramaEscola programaAtEscola) {
        ProgramaAtEscolaEntity entity = toEntity(programaAtEscola);
        ProgramaAtEscolaEntity saved = programaAtEscolaRepository.save(entity);
        return toDomain(Objects.requireNonNull(saved, "Erro ao salvar programa at escola"));
    }

    @Override
    public List<ProgramaEscola> saveAll(List<ProgramaEscola> programasAtEscola) {
        List<ProgramaAtEscolaEntity> entities = programasAtEscola.stream()
                .map(this::toEntity)
                .toList();
        return programaAtEscolaRepository.saveAll(entities).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<ProgramaEscola> findByProgramaAtendimentoId(UUID programaAtendimentoId) {
        return programaAtEscolaRepository.findByProgramaAtendimentoId(programaAtendimentoId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteByProgramaAtendimentoId(UUID programaAtendimentoId) {
        programaAtEscolaRepository.deleteByProgramaAtendimentoId(programaAtendimentoId);
    }

    private ProgramaEscola toDomain(ProgramaAtEscolaEntity entity) {
        return new ProgramaEscola(
                entity.getId(),
                entity.getProgramaAtendimentoId(),
                entity.getNomeProfissional(),
                entity.getNomeEscola(),
                null);
    }

    private ProgramaAtEscolaEntity toEntity(ProgramaEscola domain) {
        ProgramaAtEscolaEntity entity = new ProgramaAtEscolaEntity();
        entity.setId(domain.getId());
        entity.setProgramaAtendimentoId(Objects.requireNonNull(
                domain.getProgramaAtendimentoId(),
                "programaAtendimentoId e obrigatorio para persistir programa escola"));
        entity.setNomeProfissional(domain.getNomeProfissional());
        entity.setNomeEscola(domain.getNomeEscola());
        return entity;
    }
}
