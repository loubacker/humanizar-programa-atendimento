package com.humanizar.programaatendimento.domain.port.nucleo;

import java.util.List;
import java.util.UUID;

import com.humanizar.programaatendimento.domain.model.nucleo.AbordagemPatient;

public interface AbordagemPatientPort {

    AbordagemPatient save(AbordagemPatient abordagemPatient);

    List<AbordagemPatient> saveAll(List<AbordagemPatient> abordagens);

    List<AbordagemPatient> findByNucleoPatientId(UUID nucleoPatientId);

    void deleteByNucleoPatientId(UUID nucleoPatientId);
}
