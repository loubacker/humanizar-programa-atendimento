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
@Table(name = "programa_at_escola")
public class ProgramaAtEscolaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "programa_atendimento_id", nullable = false)
    private UUID programaAtendimentoId;

    @Column(name = "nome_profissional")
    private String nomeProfissional;

    @Column(name = "nome_escola")
    private String nomeEscola;

    // Construtores
    public ProgramaAtEscolaEntity() {
    }

    public ProgramaAtEscolaEntity(UUID id, UUID programaAtendimentoId, String nomeProfissional, String nomeEscola) {
        this.id = id;
        this.programaAtendimentoId = programaAtendimentoId;
        this.nomeProfissional = nomeProfissional;
        this.nomeEscola = nomeEscola;
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

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProgramaAtEscolaEntity that = (ProgramaAtEscolaEntity) o;
        return Objects.equals(id, that.id)
                && Objects.equals(programaAtendimentoId, that.programaAtendimentoId)
                && Objects.equals(nomeProfissional, that.nomeProfissional)
                && Objects.equals(nomeEscola, that.nomeEscola);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, programaAtendimentoId, nomeProfissional, nomeEscola);
    }

    @Override
    public String toString() {
        return "ProgramaAtEscolaEntity{" +
                "id=" + id +
                ", programaAtendimentoId=" + programaAtendimentoId +
                ", nomeProfissional='" + nomeProfissional + '\'' +
                ", nomeEscola='" + nomeEscola + '\'' +
                '}';
    }
}
