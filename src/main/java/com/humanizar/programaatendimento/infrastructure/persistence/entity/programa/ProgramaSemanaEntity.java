package com.humanizar.programaatendimento.infrastructure.persistence.entity.programa;

import java.util.Objects;
import java.util.UUID;

import com.humanizar.programaatendimento.domain.model.enums.Semana;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "programa_semana", uniqueConstraints = {
        @UniqueConstraint(name = "uk_programa_semana_dia", columnNames = { "programa_atendimento_id", "dia_semana" })
})
public class ProgramaSemanaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "programa_atendimento_id", nullable = false)
    private UUID programaAtendimentoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana")
    private Semana diaSemana;

    // Construtores
    public ProgramaSemanaEntity() {
    }

    public ProgramaSemanaEntity(UUID id, UUID programaAtendimentoId, Semana diaSemana) {
        this.id = id;
        this.programaAtendimentoId = programaAtendimentoId;
        this.diaSemana = diaSemana;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProgramaAtendimentoId() {
        return programaAtendimentoId;
    }

    public void setProgramaAtendimentoId(UUID programaAtendimentoId) {
        this.programaAtendimentoId = programaAtendimentoId;
    }

    public Semana getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(Semana diaSemana) {
        this.diaSemana = diaSemana;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProgramaSemanaEntity that = (ProgramaSemanaEntity) o;
        return Objects.equals(id, that.id)
                && Objects.equals(programaAtendimentoId, that.programaAtendimentoId)
                && diaSemana == that.diaSemana;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, programaAtendimentoId, diaSemana);
    }

    @Override
    public String toString() {
        return "ProgramaSemanaEntity{" +
                "id=" + id +
                ", programaAtendimentoId=" + programaAtendimentoId +
                ", diaSemana=" + diaSemana +
                '}';
    }
}
