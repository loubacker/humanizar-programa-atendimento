package com.humanizar.programaatendimento.domain.model.programa;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.humanizar.programaatendimento.domain.model.enums.Semana;

public class AtEscolaSemana {

    private UUID id;
    private UUID programaAtEscolaId;
    private Semana diaSemana;
    private List<AtEscolaSemanaSchedule> atEscolaSemanaSchedule = new ArrayList<>();

    public AtEscolaSemana() {
    }

    public AtEscolaSemana(UUID id, UUID programaAtEscolaId, Semana diaSemana,
            List<AtEscolaSemanaSchedule> atEscolaSemanaSchedule) {
        this.id = id;
        this.programaAtEscolaId = programaAtEscolaId;
        this.diaSemana = diaSemana;
        this.atEscolaSemanaSchedule = atEscolaSemanaSchedule != null ? atEscolaSemanaSchedule : new ArrayList<>();
    }

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

    public List<AtEscolaSemanaSchedule> getAtEscolaSemanaSchedule() {
        return atEscolaSemanaSchedule;
    }

    public void setAtEscolaSemanaSchedule(List<AtEscolaSemanaSchedule> atEscolaSemanaSchedule) {
        this.atEscolaSemanaSchedule = atEscolaSemanaSchedule;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID programaAtEscolaId;
        private Semana diaSemana;
        private List<AtEscolaSemanaSchedule> atEscolaSemanaSchedule = new ArrayList<>();

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder programaAtEscolaId(UUID programaAtEscolaId) {
            this.programaAtEscolaId = programaAtEscolaId;
            return this;
        }

        public Builder diaSemana(Semana diaSemana) {
            this.diaSemana = diaSemana;
            return this;
        }

        public Builder atEscolaSemanaSchedule(List<AtEscolaSemanaSchedule> atEscolaSemanaSchedule) {
            this.atEscolaSemanaSchedule = atEscolaSemanaSchedule;
            return this;
        }

        public AtEscolaSemana build() {
            return new AtEscolaSemana(id, programaAtEscolaId, diaSemana, atEscolaSemanaSchedule);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AtEscolaSemana that))
            return false;
        return Objects.equals(id, that.id)
                && Objects.equals(programaAtEscolaId, that.programaAtEscolaId)
                && diaSemana == that.diaSemana
                && Objects.equals(atEscolaSemanaSchedule, that.atEscolaSemanaSchedule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, programaAtEscolaId, diaSemana, atEscolaSemanaSchedule);
    }
}
