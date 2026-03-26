package com.humanizar.programaatendimento.infrastructure.persistence.repository.nucleo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.humanizar.programaatendimento.infrastructure.persistence.entity.nucleo.NucleoPatientEntity;

@Repository
public interface NucleoPatientRepository extends JpaRepository<NucleoPatientEntity, UUID> {

    List<NucleoPatientEntity> findAllByPatientId(UUID patientId);

    Optional<NucleoPatientEntity> findByPatientIdAndNucleoId(UUID patientId, UUID nucleoId);

    void deleteByPatientIdAndNucleoId(UUID patientId, UUID nucleoId);
}
