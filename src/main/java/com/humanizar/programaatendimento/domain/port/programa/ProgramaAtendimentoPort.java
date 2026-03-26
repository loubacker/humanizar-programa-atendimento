package com.humanizar.programaatendimento.domain.port.programa;

import java.util.Optional;
import java.util.UUID;

import com.humanizar.programaatendimento.domain.model.programa.ProgramaAtendimento;

public interface ProgramaAtendimentoPort {

    ProgramaAtendimento save(ProgramaAtendimento programaAtendimento);

    Optional<ProgramaAtendimento> findByPatientId(UUID patientId);

    void deleteByPatientId(UUID patientId);
}
