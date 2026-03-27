package com.humanizar.programaatendimento.infrastructure.persistence.repository.programa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaSemanaEntity;

@Repository
public interface ProgramaSemanaRepository extends JpaRepository<ProgramaSemanaEntity, UUID> {

    List<ProgramaSemanaEntity> findByProgramaAtendimentoId(UUID programaAtendimentoId);

    void deleteByProgramaAtendimentoId(UUID programaAtendimentoId);
}
