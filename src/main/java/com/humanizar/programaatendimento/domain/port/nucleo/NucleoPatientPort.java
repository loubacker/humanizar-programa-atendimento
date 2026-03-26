package com.humanizar.programaatendimento.domain.port.nucleo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.humanizar.programaatendimento.domain.model.nucleo.NucleoPatient;

public interface NucleoPatientPort {

    NucleoPatient save(NucleoPatient nucleoPatient);

    boolean existsById(UUID id);

    List<NucleoPatient> findAllByPatientId(UUID patientId);

    Optional<NucleoPatient> findByPatientIdAndNucleoId(UUID patientId, UUID nucleoId);

    void deleteByPatientIdAndNucleoId(UUID patientId, UUID nucleoId);
}
