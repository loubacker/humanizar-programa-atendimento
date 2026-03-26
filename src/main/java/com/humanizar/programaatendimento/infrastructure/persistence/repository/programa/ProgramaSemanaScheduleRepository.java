package com.humanizar.programaatendimento.infrastructure.persistence.repository.programa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaSemanaScheduleEntity;

@Repository
public interface ProgramaSemanaScheduleRepository extends JpaRepository<ProgramaSemanaScheduleEntity, UUID> {

    List<ProgramaSemanaScheduleEntity> findByProgramaSemanaId(UUID programaSemanaId);

    void deleteByProgramaSemanaId(UUID programaSemanaId);
}
