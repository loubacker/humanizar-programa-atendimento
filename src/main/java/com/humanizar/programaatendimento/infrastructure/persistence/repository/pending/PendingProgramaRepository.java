package com.humanizar.programaatendimento.infrastructure.persistence.repository.pending;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.humanizar.programaatendimento.infrastructure.persistence.entity.pending.PendingProgramaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PendingProgramaRepository extends JpaRepository<PendingProgramaEntity, UUID> {

    Optional<PendingProgramaEntity> findByEventId(UUID eventId);

    List<PendingProgramaEntity> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
