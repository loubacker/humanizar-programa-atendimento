package com.humanizar.programaatendimento.domain.model.programa;

import java.util.Objects;
import java.util.UUID;

public class ProgramaSemanaSchedule {

    private UUID id;
    private UUID programaSemanaId;
    private UUID nucleoId;
    private String horarioInicio;
    private String horarioTermino;
    private String turno;

    // Construtores
    public ProgramaSemanaSchedule() {
    }

    public ProgramaSemanaSchedule(UUID id, UUID programaSemanaId, UUID nucleoId, String horarioInicio,
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

    // Builder estatico
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID programaSemanaId;
        private UUID nucleoId;
        private String horarioInicio;
        private String horarioTermino;
        private String turno;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder programaSemanaId(UUID programaSemanaId) {
            this.programaSemanaId = programaSemanaId;
            return this;
        }

        public Builder nucleoId(UUID nucleoId) {
            this.nucleoId = nucleoId;
            return this;
        }

        public Builder horarioInicio(String horarioInicio) {
            this.horarioInicio = horarioInicio;
            return this;
        }

        public Builder horarioTermino(String horarioTermino) {
            this.horarioTermino = horarioTermino;
            return this;
        }

        public Builder turno(String turno) {
            this.turno = turno;
            return this;
        }

        public ProgramaSemanaSchedule build() {
            return new ProgramaSemanaSchedule(id, programaSemanaId, nucleoId, horarioInicio, horarioTermino,
                    turno);
        }
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProgramaSemanaSchedule that = (ProgramaSemanaSchedule) o;
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
        return "ProgramaSemanaSchedule{" +
                "id=" + id +
                ", programaSemanaId=" + programaSemanaId +
                ", nucleoId=" + nucleoId +
                ", horarioInicio='" + horarioInicio + '\'' +
                ", horarioTermino='" + horarioTermino + '\'' +
                ", turno='" + turno + '\'' +
                '}';
    }
}
