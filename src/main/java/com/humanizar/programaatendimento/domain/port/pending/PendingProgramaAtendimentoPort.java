package com.humanizar.programaatendimento.domain.port.pending;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.humanizar.programaatendimento.domain.model.enums.OperationType;
import com.humanizar.programaatendimento.domain.model.enums.Status;
import com.humanizar.programaatendimento.domain.model.pending.PendingProgramaAtendimento;

public interface PendingProgramaAtendimentoPort {

    PendingProgramaAtendimento save(PendingProgramaAtendimento pending);

    Optional<PendingProgramaAtendimento> findByEventId(UUID eventId);

    List<PendingProgramaAtendimento> findByPatientId(UUID patientId);

    Page<PendingProgramaAtendimento> findByPatientId(UUID patientId, Pageable pageable);

    boolean checkDeleteStatusByPatientId(UUID patientId, OperationType operationType, Status status);
}
