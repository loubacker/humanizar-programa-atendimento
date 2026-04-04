package com.humanizar.programaatendimento.infrastructure.persistence.repository.pending;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.humanizar.programaatendimento.infrastructure.persistence.entity.pending.PendingProgramaEntity;

@Repository
public interface PendingProgramaRepository extends JpaRepository<PendingProgramaEntity, UUID> {

    Optional<PendingProgramaEntity> findByEventId(UUID eventId);

    List<PendingProgramaEntity> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    Page<PendingProgramaEntity> findByPatientId(UUID patientId, Pageable pageable);
}
