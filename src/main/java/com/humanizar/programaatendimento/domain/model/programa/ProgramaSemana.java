package com.humanizar.programaatendimento.domain.model.programa;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.humanizar.programaatendimento.domain.model.enums.Semana;

public class ProgramaSemana {

    private UUID id;
    private UUID programaAtendimentoId;
    private Semana diaSemana;
    private List<ProgramaSemanaSchedule> programaSemanaSchedule = new ArrayList<>();

    public ProgramaSemana() {
    }

    public ProgramaSemana(UUID id, UUID programaAtendimentoId, Semana diaSemana,
            List<ProgramaSemanaSchedule> programaSemanaSchedule) {
        this.id = id;
        this.programaAtendimentoId = programaAtendimentoId;
        this.diaSemana = diaSemana;
        this.programaSemanaSchedule = programaSemanaSchedule != null ? programaSemanaSchedule : new ArrayList<>();
    }

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

    public List<ProgramaSemanaSchedule> getProgramaSemanaSchedule() {
        return programaSemanaSchedule;
    }

    public void setProgramaSemanaSchedule(List<ProgramaSemanaSchedule> programaSemanaSchedule) {
        this.programaSemanaSchedule = programaSemanaSchedule;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID programaAtendimentoId;
        private Semana diaSemana;
        private List<ProgramaSemanaSchedule> programaSemanaSchedule = new ArrayList<>();

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder programaAtendimentoId(UUID programaAtendimentoId) {
            this.programaAtendimentoId = programaAtendimentoId;
            return this;
        }

        public Builder diaSemana(Semana diaSemana) {
            this.diaSemana = diaSemana;
            return this;
        }

        public Builder programaSemanaSchedule(List<ProgramaSemanaSchedule> programaSemanaSchedule) {
            this.programaSemanaSchedule = programaSemanaSchedule;
            return this;
        }

        public ProgramaSemana build() {
            return new ProgramaSemana(id, programaAtendimentoId, diaSemana, programaSemanaSchedule);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProgramaSemana that))
            return false;
        return Objects.equals(id, that.id)
                && Objects.equals(programaAtendimentoId, that.programaAtendimentoId)
                && diaSemana == that.diaSemana
                && Objects.equals(programaSemanaSchedule, that.programaSemanaSchedule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, programaAtendimentoId, diaSemana, programaSemanaSchedule);
    }
}
