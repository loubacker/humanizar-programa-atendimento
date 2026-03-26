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

@Entity
@Table(name = "at_escola_semana")
public class AtEscolaSemanaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "programa_at_escola_id", nullable = false)
    private UUID programaAtEscolaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana")
    private Semana diaSemana;

    // Construtores
    public AtEscolaSemanaEntity() {
    }

    public AtEscolaSemanaEntity(UUID id, UUID programaAtEscolaId, Semana diaSemana) {
        this.id = id;
        this.programaAtEscolaId = programaAtEscolaId;
        this.diaSemana = diaSemana;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProgramaAtEscolaId() {
        return programaAtEscolaId;
    }

    public void setProgramaAtEscolaId(UUID programaAtEscolaId) {
        this.programaAtEscolaId = programaAtEscolaId;
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
        AtEscolaSemanaEntity that = (AtEscolaSemanaEntity) o;
        return Objects.equals(id, that.id)
                && Objects.equals(programaAtEscolaId, that.programaAtEscolaId)
                && diaSemana == that.diaSemana;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, programaAtEscolaId, diaSemana);
    }

    @Override
    public String toString() {
        return "AtEscolaSemanaEntity{" +
                "id=" + id +
                ", programaAtEscolaId=" + programaAtEscolaId +
                ", diaSemana=" + diaSemana +
                '}';
    }
}
