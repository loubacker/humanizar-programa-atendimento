package com.humanizar.programaatendimento.infrastructure.adapter.nucleo;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.domain.model.nucleo.NucleoPatient;
import com.humanizar.programaatendimento.domain.port.nucleo.NucleoPatientPort;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.nucleo.NucleoPatientEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.nucleo.NucleoPatientRepository;

@Component
public class NucleoPatientAdapter implements NucleoPatientPort {

    private final NucleoPatientRepository nucleoPatientRepository;

    public NucleoPatientAdapter(NucleoPatientRepository nucleoPatientRepository) {
        this.nucleoPatientRepository = nucleoPatientRepository;
    }

    @Override
    public boolean existsById(UUID id) {
        return nucleoPatientRepository.existsById(id);
    }

    @Override
    public NucleoPatient save(NucleoPatient nucleoPatient) {
        NucleoPatientEntity entity = toEntity(nucleoPatient);
        NucleoPatientEntity saved = nucleoPatientRepository.save(entity);
        return toDomain(Objects.requireNonNull(saved, "Erro ao salvar nucleo patient"));
    }

    @Override
    public List<NucleoPatient> findAllByPatientId(UUID patientId) {
        return nucleoPatientRepository.findAllByPatientId(patientId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<NucleoPatient> findByPatientIdAndNucleoId(UUID patientId, UUID nucleoId) {
        return nucleoPatientRepository.findByPatientIdAndNucleoId(patientId, nucleoId)
                .map(this::toDomain);
    }

    @Override
    public void deleteByPatientIdAndNucleoId(UUID patientId, UUID nucleoId) {
        nucleoPatientRepository.deleteByPatientIdAndNucleoId(patientId, nucleoId);
    }

    private NucleoPatient toDomain(NucleoPatientEntity entity) {
        return new NucleoPatient(
                entity.getId(),
                entity.getPatientId(),
                entity.getNucleoId(),
                null);
    }

    private NucleoPatientEntity toEntity(NucleoPatient domain) {
        UUID id = Objects.requireNonNull(
                domain.getId(),
                "id (nucleoPatientId) e obrigatorio para persistir NucleoPatient");

        NucleoPatientEntity entity = new NucleoPatientEntity();
        entity.setId(id);
        entity.setPatientId(domain.getPatientId());
        entity.setNucleoId(domain.getNucleoId());
        return entity;
    }
}
