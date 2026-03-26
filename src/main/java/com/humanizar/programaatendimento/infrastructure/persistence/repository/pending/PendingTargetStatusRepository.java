package com.humanizar.programaatendimento.infrastructure.persistence.repository.pending;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.humanizar.programaatendimento.domain.model.enums.Status;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.pending.PendingTargetStatusEntity;

@Repository
public interface PendingTargetStatusRepository extends JpaRepository<PendingTargetStatusEntity, UUID> {

    Optional<PendingTargetStatusEntity> findByEventIdAndTargetService(UUID eventId, String targetService);

    List<PendingTargetStatusEntity> findByEventId(UUID eventId);

    List<PendingTargetStatusEntity> findByTargetServiceAndStatus(String targetService, Status status);

    void deleteByEventId(UUID eventId);
}
