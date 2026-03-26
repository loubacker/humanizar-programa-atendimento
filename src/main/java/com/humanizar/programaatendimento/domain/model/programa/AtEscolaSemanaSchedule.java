package com.humanizar.programaatendimento.domain.model.programa;

import java.util.Objects;
import java.util.UUID;

public class AtEscolaSemanaSchedule {

    private UUID id;
    private UUID atEscolaSemanaId;
    private UUID nucleoId;
    private String horarioInicio;
    private String horarioTermino;
    private String turno;

    // Construtores
    public AtEscolaSemanaSchedule() {
    }

    public AtEscolaSemanaSchedule(UUID id, UUID atEscolaSemanaId, UUID nucleoId, String horarioInicio,
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

    // Builder Estático
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID atEscolaSemanaId;
        private UUID nucleoId;
        private String horarioInicio;
        private String horarioTermino;
        private String turno;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder atEscolaSemanaId(UUID atEscolaSemanaId) {
            this.atEscolaSemanaId = atEscolaSemanaId;
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

        public AtEscolaSemanaSchedule build() {
            return new AtEscolaSemanaSchedule(id, atEscolaSemanaId, nucleoId, horarioInicio, horarioTermino, turno);
        }
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AtEscolaSemanaSchedule that = (AtEscolaSemanaSchedule) o;
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
        return "AtEscolaSemanaSchedule{" +
                "id=" + id +
                ", atEscolaSemanaId=" + atEscolaSemanaId +
                ", nucleoId=" + nucleoId +
                ", horarioInicio='" + horarioInicio + '\'' +
                ", horarioTermino='" + horarioTermino + '\'' +
                ", turno='" + turno + '\'' +
                '}';
    }
}
