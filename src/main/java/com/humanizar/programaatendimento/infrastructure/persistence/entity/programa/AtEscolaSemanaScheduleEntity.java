package com.humanizar.programaatendimento.infrastructure.persistence.entity.programa;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "at_escola_semana_schedule")
public class AtEscolaSemanaScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "at_escola_semana_id", nullable = false)
    private UUID atEscolaSemanaId;

    @Column(name = "nucleo_id")
    private UUID nucleoId;

    @Column(name = "horario_inicio")
    private String horarioInicio;

    @Column(name = "horario_termino")
    private String horarioTermino;

    @Column(name = "turno")
    private String turno;

    // Construtores
    public AtEscolaSemanaScheduleEntity() {
    }

    public AtEscolaSemanaScheduleEntity(UUID id, UUID atEscolaSemanaId, UUID nucleoId, String horarioInicio,
            String horarioTermino, String turno) {
        this.id = id;
        this.atEscolaSemanaId = atEscolaSemanaId;
        this.nucleoId = nucleoId;
        this.horarioInicio = horarioInicio;
        this.horarioTermino = horarioTermino;
        this.turno = turno;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAtEscolaSemanaId() {
        return atEscolaSemanaId;
    }

    public void setAtEscolaSemanaId(UUID atEscolaSemanaId) {
        this.atEscolaSemanaId = atEscolaSemanaId;
    }

    public UUID getNucleoId() {
        return nucleoId;
    }

    public void setNucleoId(UUID nucleoId) {
        this.nucleoId = nucleoId;
    }

    public String getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(String horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public String getHorarioTermino() {
        return horarioTermino;
    }

    public void setHorarioTermino(String horarioTermino) {
        this.horarioTermino = horarioTermino;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AtEscolaSemanaScheduleEntity that = (AtEscolaSemanaScheduleEntity) o;
        return Objects.equals(id, that.id)
                && Objects.equals(atEscolaSemanaId, that.atEscolaSemanaId)
                && Objects.equals(nucleoId, that.nucleoId)
                && Objects.equals(horarioInicio, that.horarioInicio)
                && Objects.equals(horarioTermino, that.horarioTermino)
                && Objects.equals(turno, that.turno);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, atEscolaSemanaId, nucleoId, horarioInicio, horarioTermino, turno);
    }

    @Override
    public String toString() {
        return "AtEscolaSemanaScheduleEntity{" +
                "id=" + id +
                ", atEscolaSemanaId=" + atEscolaSemanaId +
                ", nucleoId=" + nucleoId +
                ", horarioInicio='" + horarioInicio + '\'' +
                ", horarioTermino='" + horarioTermino + '\'' +
                ", turno='" + turno + '\'' +
                '}';
    }
}
