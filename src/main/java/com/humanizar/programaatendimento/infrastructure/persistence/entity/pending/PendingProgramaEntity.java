package com.humanizar.programaatendimento.infrastructure.persistence.entity.pending;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.humanizar.programaatendimento.domain.model.enums.OperationType;
import com.humanizar.programaatendimento.domain.model.enums.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "pending_programa_atendimento", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pending_correlation_event", columnNames = { "correlation_id", "event_id" })
}, indexes = {
        @Index(name = "idx_pending_patient_created_at", columnList = "patient_id, created_at")
})
public class PendingProgramaEntity {

    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "programa_atendimento_id")
    private UUID programaAtendimentoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private OperationType operationType;

    @Column(name = "payload_snapshot", nullable = false, columnDefinition = "text")
    private String payloadSnapshot;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    // Construtores
    public PendingProgramaEntity() {
        this.eventId = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.status = Status.PENDING;
    }

    public PendingProgramaEntity(UUID eventId, UUID correlationId, UUID patientId, UUID programaAtendimentoId,
            OperationType operationType, String payloadSnapshot, LocalDateTime createdAt,
            Status status, String errorMessage) {
        this.eventId = eventId != null ? eventId : UUID.randomUUID();
        this.correlationId = correlationId;
        this.patientId = patientId;
        this.programaAtendimentoId = programaAtendimentoId;
        this.operationType = operationType;
        this.payloadSnapshot = payloadSnapshot;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.status = status != null ? status : Status.PENDING;
        this.errorMessage = errorMessage;
    }

    @PrePersist
    protected void onCreate() {
        if (this.eventId == null) {
            this.eventId = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = Status.PENDING;
        }
    }

    // Getters e Setters
    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getProgramaAtendimentoId() {
        return programaAtendimentoId;
    }

    public void setProgramaAtendimentoId(UUID programaAtendimentoId) {
        this.programaAtendimentoId = programaAtendimentoId;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public String getPayloadSnapshot() {
        return payloadSnapshot;
    }

    public void setPayloadSnapshot(String payloadSnapshot) {
        this.payloadSnapshot = payloadSnapshot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PendingProgramaEntity that = (PendingProgramaEntity) o;
        return Objects.equals(eventId, that.eventId)
                && Objects.equals(correlationId, that.correlationId)
                && Objects.equals(patientId, that.patientId)
                && Objects.equals(programaAtendimentoId, that.programaAtendimentoId)
                && operationType == that.operationType
                && Objects.equals(payloadSnapshot, that.payloadSnapshot)
                && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, correlationId, patientId, programaAtendimentoId, operationType, payloadSnapshot,
                status);
    }

    @Override
    public String toString() {
        return "PendingProgramaEntity{" +
                "eventId=" + eventId +
                ", correlationId=" + correlationId +
                ", patientId=" + patientId +
                ", programaAtendimentoId=" + programaAtendimentoId +
                ", operationType=" + operationType +
                ", payloadSnapshot='" + payloadSnapshot + '\'' +
                ", createdAt=" + createdAt +
                ", status=" + status +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
