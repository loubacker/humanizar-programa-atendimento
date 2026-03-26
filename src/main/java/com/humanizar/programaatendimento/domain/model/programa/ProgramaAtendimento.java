package com.humanizar.programaatendimento.domain.model.programa;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.humanizar.programaatendimento.domain.model.enums.SimNao;

public class ProgramaAtendimento {

    private UUID id;
    private UUID patientId;
    private LocalDateTime dataInicio;
    private SimNao cadastroApp;
    private SimNao atEscolar;
    private List<ProgramaSemana> programasSemana = new ArrayList<>();
    private List<ProgramaEscola> programasEscola = new ArrayList<>();
    private String observacao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtores
    public ProgramaAtendimento() {
    }

    public ProgramaAtendimento(UUID id, UUID patientId, LocalDateTime dataInicio, SimNao cadastroApp,
            SimNao atEscolar, List<ProgramaSemana> programasSemana, List<ProgramaEscola> programasEscola,
            String observacao, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.patientId = patientId;
        this.dataInicio = dataInicio;
        this.cadastroApp = cadastroApp;
        this.atEscolar = atEscolar;
        this.programasSemana = programasSemana != null ? programasSemana : new ArrayList<>();
        this.programasEscola = programasEscola != null ? programasEscola : new ArrayList<>();
        this.observacao = observacao;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public List<ProgramaSemana> getProgramasSemana() {
        return programasSemana;
    }

    public void setProgramasSemana(List<ProgramaSemana> programasSemana) {
        this.programasSemana = programasSemana;
    }

    public List<ProgramaEscola> getProgramasEscola() {
        return programasEscola;
    }

    public void setProgramasEscola(List<ProgramaEscola> programasEscola) {
        this.programasEscola = programasEscola;
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

    // Builder Estático
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID patientId;
        private LocalDateTime dataInicio;
        private SimNao cadastroApp;
        private SimNao atEscolar;
        private List<ProgramaSemana> programasSemana = new ArrayList<>();
        private List<ProgramaEscola> programasEscola = new ArrayList<>();
        private String observacao;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder patientId(UUID patientId) {
            this.patientId = patientId;
            return this;
        }

        public Builder dataInicio(LocalDateTime dataInicio) {
            this.dataInicio = dataInicio;
            return this;
        }

        public Builder cadastroApp(SimNao cadastroApp) {
            this.cadastroApp = cadastroApp;
            return this;
        }

        public Builder atEscolar(SimNao atEscolar) {
            this.atEscolar = atEscolar;
            return this;
        }

        public Builder programasSemana(List<ProgramaSemana> programasSemana) {
            this.programasSemana = programasSemana;
            return this;
        }

        public Builder programasEscola(List<ProgramaEscola> programasEscola) {
            this.programasEscola = programasEscola;
            return this;
        }

        public Builder observacao(String observacao) {
            this.observacao = observacao;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ProgramaAtendimento build() {
            return new ProgramaAtendimento(id, patientId, dataInicio, cadastroApp, atEscolar,
                    programasSemana, programasEscola, observacao, createdAt, updatedAt);
        }
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProgramaAtendimento that = (ProgramaAtendimento) o;
        return Objects.equals(id, that.id)
                && Objects.equals(patientId, that.patientId)
                && Objects.equals(dataInicio, that.dataInicio)
                && cadastroApp == that.cadastroApp
                && atEscolar == that.atEscolar
                && Objects.equals(programasSemana, that.programasSemana)
                && Objects.equals(programasEscola, that.programasEscola)
                && Objects.equals(observacao, that.observacao)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, patientId, dataInicio, cadastroApp, atEscolar, programasSemana,
                programasEscola, observacao, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "ProgramaAtendimento{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", dataInicio=" + dataInicio +
                ", cadastroApp=" + cadastroApp +
                ", atEscolar=" + atEscolar +
                ", programasSemana=" + programasSemana +
                ", programasEscola=" + programasEscola +
                ", observacao='" + observacao + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
