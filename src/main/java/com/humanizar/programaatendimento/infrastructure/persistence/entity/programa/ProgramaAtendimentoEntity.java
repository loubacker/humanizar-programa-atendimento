package com.humanizar.programaatendimento.infrastructure.persistence.entity.programa;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.humanizar.programaatendimento.domain.model.enums.SimNao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "programa_atendimento")
public class ProgramaAtendimentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "patient_id")
    private UUID patientId;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Enumerated(EnumType.STRING)
    @Column(name = "cadastro_app")
    private SimNao cadastroApp;

    @Enumerated(EnumType.STRING)
    @Column(name = "at_escolar")
    private SimNao atEscolar;

    @Column(columnDefinition = "text")
    private String observacao;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Construtores
    public ProgramaAtendimentoEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public ProgramaAtendimentoEntity(UUID id, UUID patientId, LocalDateTime dataInicio, SimNao cadastroApp,
            SimNao atEscolar, String observacao, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.patientId = patientId;
        this.dataInicio = dataInicio;
        this.cadastroApp = cadastroApp;
        this.atEscolar = atEscolar;
        this.observacao = observacao;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public SimNao getCadastroApp() {
        return cadastroApp;
    }

    public void setCadastroApp(SimNao cadastroApp) {
        this.cadastroApp = cadastroApp;
    }

    public SimNao getAtEscolar() {
        return atEscolar;
    }

    public void setAtEscolar(SimNao atEscolar) {
        this.atEscolar = atEscolar;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProgramaAtendimentoEntity that = (ProgramaAtendimentoEntity) o;
        return Objects.equals(id, that.id)
                && Objects.equals(patientId, that.patientId)
                && Objects.equals(dataInicio, that.dataInicio)
                && cadastroApp == that.cadastroApp
                && atEscolar == that.atEscolar
                && Objects.equals(observacao, that.observacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, patientId, dataInicio, cadastroApp, atEscolar, observacao);
    }

    @Override
    public String toString() {
        return "ProgramaAtendimentoEntity{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", dataInicio=" + dataInicio +
                ", cadastroApp=" + cadastroApp +
                ", atEscolar=" + atEscolar +
                ", observacao='" + observacao + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
