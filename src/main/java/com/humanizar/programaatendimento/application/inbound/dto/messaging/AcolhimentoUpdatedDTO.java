package com.humanizar.programaatendimento.application.inbound.dto.messaging;

import java.util.List;
import java.util.UUID;

public record AcolhimentoUpdatedDTO(
        UUID patientId,
        List<AcolhimentoNucleoPatientDTO> nucleoPatient) {
}
