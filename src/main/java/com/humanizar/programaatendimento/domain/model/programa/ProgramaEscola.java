package com.humanizar.programaatendimento.domain.model.programa;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProgramaEscola {

    private UUID id;
    private UUID programaAtendimentoId;
    private String nomeProfissional;
    private String nomeEscola;
    private List<AtEscolaSemana> atEscolaSemana = new ArrayList<>();

    public ProgramaEscola() {
    }

    public ProgramaEscola(UUID id, UUID programaAtendimentoId, String nomeProfissional, String nomeEscola,
            List<AtEscolaSemana> atEscolaSemana) {
        this.id = id;
        this.programaAtendimentoId = programaAtendimentoId;
        this.nomeProfissional = nomeProfissional;
        this.nomeEscola = nomeEscola;
        this.atEscolaSemana = atEscolaSemana != null ? atEscolaSemana : new ArrayList<>();
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

    public String getNomeProfissional() {
        return nomeProfissional;
    }

    public void setNomeProfissional(String nomeProfissional) {
        this.nomeProfissional = nomeProfissional;
    }

    public String getNomeEscola() {
        return nomeEscola;
    }

    public void setNomeEscola(String nomeEscola) {
        this.nomeEscola = nomeEscola;
    }

    public List<AtEscolaSemana> getAtEscolaSemana() {
        return atEscolaSemana;
    }

    public void setAtEscolaSemana(List<AtEscolaSemana> atEscolaSemana) {
        this.atEscolaSemana = atEscolaSemana;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID programaAtendimentoId;
        private String nomeProfissional;
        private String nomeEscola;
        private List<AtEscolaSemana> atEscolaSemana = new ArrayList<>();

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder programaAtendimentoId(UUID programaAtendimentoId) {
            this.programaAtendimentoId = programaAtendimentoId;
            return this;
        }

        public Builder nomeProfissional(String nomeProfissional) {
            this.nomeProfissional = nomeProfissional;
            return this;
        }

        public Builder nomeEscola(String nomeEscola) {
            this.nomeEscola = nomeEscola;
            return this;
        }

        public Builder atEscolaSemana(List<AtEscolaSemana> atEscolaSemana) {
            this.atEscolaSemana = atEscolaSemana;
            return this;
        }

        public ProgramaEscola build() {
            return new ProgramaEscola(id, programaAtendimentoId, nomeProfissional, nomeEscola, atEscolaSemana);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProgramaEscola that))
            return false;
        return Objects.equals(id, that.id)
                && Objects.equals(programaAtendimentoId, that.programaAtendimentoId)
                && Objects.equals(nomeProfissional, that.nomeProfissional)
                && Objects.equals(nomeEscola, that.nomeEscola)
                && Objects.equals(atEscolaSemana, that.atEscolaSemana);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, programaAtendimentoId, nomeProfissional, nomeEscola, atEscolaSemana);
    }
}
