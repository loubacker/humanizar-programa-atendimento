package com.humanizar.programaatendimento.domain.port.pending;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.humanizar.programaatendimento.domain.model.pending.PendingProgramaAtendimento;

public interface PendingProgramaAtendimentoPort {

    PendingProgramaAtendimento save(PendingProgramaAtendimento pending);

    Optional<PendingProgramaAtendimento> findByEventId(UUID eventId);

    List<PendingProgramaAtendimento> findByPatientId(UUID patientId);
}
