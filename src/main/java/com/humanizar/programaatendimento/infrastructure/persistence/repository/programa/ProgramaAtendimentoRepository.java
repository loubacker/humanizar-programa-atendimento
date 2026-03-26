package com.humanizar.programaatendimento.infrastructure.persistence.repository.programa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaAtendimentoEntity;

@Repository
public interface ProgramaAtendimentoRepository extends JpaRepository<ProgramaAtendimentoEntity, UUID> {

    Optional<ProgramaAtendimentoEntity> findByPatientId(UUID patientId);

    void deleteByPatientId(UUID patientId);
}
