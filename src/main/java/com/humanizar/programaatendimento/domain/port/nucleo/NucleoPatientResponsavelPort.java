package com.humanizar.programaatendimento.domain.port.nucleo;

import java.util.List;
import java.util.UUID;

import com.humanizar.programaatendimento.domain.model.nucleo.NucleoPatientResponsavel;

public interface NucleoPatientResponsavelPort {

    NucleoPatientResponsavel save(NucleoPatientResponsavel responsavel);

    List<NucleoPatientResponsavel> saveAll(List<NucleoPatientResponsavel> responsaveis);

    List<NucleoPatientResponsavel> findByNucleoPatientId(UUID nucleoPatientId);

    void deleteById(UUID id);

    void deleteByNucleoPatientId(UUID nucleoPatientId);
}
