package com.humanizar.programaatendimento.infrastructure.persistence.repository.programa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.AtEscolaSemanaEntity;

@Repository
public interface AtEscolaSemanaRepository extends JpaRepository<AtEscolaSemanaEntity, UUID> {

    List<AtEscolaSemanaEntity> findByProgramaAtEscolaId(UUID programaAtEscolaId);

    void deleteByProgramaAtEscolaId(UUID programaAtEscolaId);
}
