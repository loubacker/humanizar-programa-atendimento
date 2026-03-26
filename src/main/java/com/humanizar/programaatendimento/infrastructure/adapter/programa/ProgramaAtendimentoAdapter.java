package com.humanizar.programaatendimento.infrastructure.adapter.programa;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.domain.model.programa.ProgramaAtendimento;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtendimentoPort;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaAtendimentoEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.ProgramaAtendimentoRepository;

@Component
public class ProgramaAtendimentoAdapter implements ProgramaAtendimentoPort {

    private final ProgramaAtendimentoRepository programaAtendimentoRepository;

    public ProgramaAtendimentoAdapter(ProgramaAtendimentoRepository programaAtendimentoRepository) {
        this.programaAtendimentoRepository = programaAtendimentoRepository;
    }

    @Override
    public ProgramaAtendimento save(ProgramaAtendimento programaAtendimento) {
        ProgramaAtendimentoEntity entity = toEntity(programaAtendimento);
        ProgramaAtendimentoEntity saved = programaAtendimentoRepository.save(entity);
        return toDomain(Objects.requireNonNull(saved, "Erro ao salvar programa de atendimento"));
    }

    @Override
    public Optional<ProgramaAtendimento> findByPatientId(UUID patientId) {
        return programaAtendimentoRepository.findByPatientId(patientId)
                .map(this::toDomain);
    }

    @Override
    public void deleteByPatientId(UUID patientId) {
        programaAtendimentoRepository.deleteByPatientId(patientId);
    }

    private ProgramaAtendimento toDomain(ProgramaAtendimentoEntity entity) {
        return new ProgramaAtendimento(
                entity.getId(),
                entity.getPatientId(),
                entity.getDataInicio(),
                entity.getCadastroApp(),
                entity.getAtEscolar(),
                null,
                null,
                entity.getObservacao(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private ProgramaAtendimentoEntity toEntity(ProgramaAtendimento domain) {
        ProgramaAtendimentoEntity entity = new ProgramaAtendimentoEntity();
        entity.setId(domain.getId());
        entity.setPatientId(domain.getPatientId());
        entity.setDataInicio(domain.getDataInicio());
        entity.setCadastroApp(domain.getCadastroApp());
        entity.setAtEscolar(domain.getAtEscolar());
        entity.setObservacao(domain.getObservacao());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
