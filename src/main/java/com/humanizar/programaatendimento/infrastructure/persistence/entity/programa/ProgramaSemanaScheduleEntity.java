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
@Table(name = "programa_semana_schedule")
public class ProgramaSemanaScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "programa_semana_id", nullable = false)
    private UUID programaSemanaId;

    @Column(name = "nucleo_id")
    private UUID nucleoId;

    @Column(name = "horario_inicio")
    private String horarioInicio;

    @Column(name = "horario_termino")
    private String horarioTermino;

    @Column(name = "turno")
    private String turno;

    // Construtores
    public ProgramaSemanaScheduleEntity() {
    }

    public ProgramaSemanaScheduleEntity(UUID id, UUID programaSemanaId, UUID nucleoId, String horarioInicio,
            String horarioTermino, String turno) {
        this.id = id;
        this.programaSemanaId = programaSemanaId;
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

    public UUID getProgramaSemanaId() {
        return programaSemanaId;
    }

    public void setProgramaSemanaId(UUID programaSemanaId) {
        this.programaSemanaId = programaSemanaId;
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
        ProgramaSemanaScheduleEntity that = (ProgramaSemanaScheduleEntity) o;
        return Objects.equals(id, that.id)
                && Objects.equals(programaSemanaId, that.programaSemanaId)
                && Objects.equals(nucleoId, that.nucleoId)
                && Objects.equals(horarioInicio, that.horarioInicio)
                && Objects.equals(horarioTermino, that.horarioTermino)
                && Objects.equals(turno, that.turno);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, programaSemanaId, nucleoId, horarioInicio, horarioTermino, turno);
    }

    @Override
    public String toString() {
        return "ProgramaSemanaScheduleEntity{" +
                "id=" + id +
                ", programaSemanaId=" + programaSemanaId +
                ", nucleoId=" + nucleoId +
                ", horarioInicio='" + horarioInicio + '\'' +
                ", horarioTermino='" + horarioTermino + '\'' +
                ", turno='" + turno + '\'' +
                '}';
    }
}
