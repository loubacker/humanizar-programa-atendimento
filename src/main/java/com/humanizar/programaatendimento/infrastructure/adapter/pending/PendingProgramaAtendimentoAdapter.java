package com.humanizar.programaatendimento.infrastructure.adapter.pending;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.humanizar.programaatendimento.domain.model.pending.PendingProgramaAtendimento;
import com.humanizar.programaatendimento.domain.port.pending.PendingProgramaAtendimentoPort;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.pending.PendingProgramaEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.pending.PendingProgramaRepository;

@Component
public class PendingProgramaAtendimentoAdapter implements PendingProgramaAtendimentoPort {

    private final PendingProgramaRepository pendingProgramaRepository;

    public PendingProgramaAtendimentoAdapter(PendingProgramaRepository pendingProgramaRepository) {
        this.pendingProgramaRepository = pendingProgramaRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public PendingProgramaAtendimento save(PendingProgramaAtendimento pending) {
        if (pending.getEventId() == null) {
            pending.setEventId(UUID.randomUUID());
        }
        PendingProgramaEntity entity = toEntity(pending);
        PendingProgramaEntity saved = pendingProgramaRepository.save(entity);
        return toDomain(Objects.requireNonNull(saved, "Erro ao salvar pending programa atendimento"));
    }

    @Override
    public Optional<PendingProgramaAtendimento> findByEventId(UUID eventId) {
        return pendingProgramaRepository.findByEventId(eventId)
                .map(this::toDomain);
    }

    @Override
    public List<PendingProgramaAtendimento> findByPatientId(UUID patientId) {
        return pendingProgramaRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::toDomain)
                .toList();
    }

    private PendingProgramaAtendimento toDomain(PendingProgramaEntity entity) {
        return new PendingProgramaAtendimento(
                entity.getEventId(),
                entity.getCorrelationId(),
                entity.getPatientId(),
                entity.getProgramaAtendimentoId(),
                entity.getOperationType(),
                entity.getPayloadSnapshot(),
                entity.getCreatedAt(),
                entity.getStatus(),
                entity.getErrorMessage());
    }

    private PendingProgramaEntity toEntity(PendingProgramaAtendimento domain) {
        PendingProgramaEntity entity = new PendingProgramaEntity();
        entity.setEventId(domain.getEventId());
        entity.setCorrelationId(domain.getCorrelationId());
        entity.setPatientId(domain.getPatientId());
        entity.setProgramaAtendimentoId(domain.getProgramaAtendimentoId());
        entity.setOperationType(domain.getOperationType());
        entity.setPayloadSnapshot(domain.getPayloadSnapshot());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setStatus(domain.getStatus());
        entity.setErrorMessage(domain.getErrorMessage());
        return entity;
    }
}
