package com.humanizar.programaatendimento.infrastructure.persistence.repository.nucleo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.humanizar.programaatendimento.infrastructure.persistence.entity.nucleo.AbordagemPatientEntity;

@Repository
public interface AbordagemPatientRepository extends JpaRepository<AbordagemPatientEntity, UUID> {

    List<AbordagemPatientEntity> findByNucleoPatientId(UUID nucleoPatientId);

    void deleteByNucleoPatientId(UUID nucleoPatientId);
}
