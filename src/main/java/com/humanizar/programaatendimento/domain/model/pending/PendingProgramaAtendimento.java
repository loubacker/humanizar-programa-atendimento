package com.humanizar.programaatendimento.domain.model.pending;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.humanizar.programaatendimento.domain.model.enums.OperationType;
import com.humanizar.programaatendimento.domain.model.enums.Status;

public class PendingProgramaAtendimento {

    private UUID eventId;
    private UUID correlationId;
    private UUID patientId;
    private UUID programaAtendimentoId;
    private OperationType operationType;
    private String payloadSnapshot;
    private LocalDateTime createdAt;
    private Status status;
    private String errorMessage;

    // Construtores
    public PendingProgramaAtendimento() {
        this.eventId = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.status = Status.PENDING;
    }

    public PendingProgramaAtendimento(UUID eventId, UUID correlationId, UUID patientId, UUID programaAtendimentoId,
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

    // Builder Estático
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID eventId;
        private UUID correlationId;
        private UUID patientId;
        private UUID programaAtendimentoId;
        private OperationType operationType;
        private String payloadSnapshot;
        private LocalDateTime createdAt;
        private Status status;
        private String errorMessage;

        public Builder eventId(UUID eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder correlationId(UUID correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder patientId(UUID patientId) {
            this.patientId = patientId;
            return this;
        }

        public Builder programaAtendimentoId(UUID programaAtendimentoId) {
            this.programaAtendimentoId = programaAtendimentoId;
            return this;
        }

        public Builder operationType(OperationType operationType) {
            this.operationType = operationType;
            return this;
        }

        public Builder payloadSnapshot(String payloadSnapshot) {
            this.payloadSnapshot = payloadSnapshot;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public PendingProgramaAtendimento build() {
            return new PendingProgramaAtendimento(eventId, correlationId, patientId, programaAtendimentoId,
                    operationType, payloadSnapshot, createdAt, status, errorMessage);
        }
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PendingProgramaAtendimento that = (PendingProgramaAtendimento) o;
        return Objects.equals(eventId, that.eventId)
                && Objects.equals(correlationId, that.correlationId)
                && Objects.equals(patientId, that.patientId)
                && Objects.equals(programaAtendimentoId, that.programaAtendimentoId)
                && operationType == that.operationType
                && Objects.equals(payloadSnapshot, that.payloadSnapshot)
                && Objects.equals(createdAt, that.createdAt)
                && status == that.status
                && Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, correlationId, patientId, programaAtendimentoId, operationType, payloadSnapshot,
                createdAt, status, errorMessage);
    }

    @Override
    public String toString() {
        return "PendingProgramaAtendimento{" +
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
