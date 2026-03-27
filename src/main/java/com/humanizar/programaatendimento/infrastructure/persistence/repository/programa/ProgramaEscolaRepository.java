package com.humanizar.programaatendimento.infrastructure.persistence.repository.programa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaEscolaEntity;

@Repository
public interface ProgramaEscolaRepository extends JpaRepository<ProgramaEscolaEntity, UUID> {

    List<ProgramaEscolaEntity> findByProgramaAtendimentoId(UUID programaAtendimentoId);

    void deleteByProgramaAtendimentoId(UUID programaAtendimentoId);
}
