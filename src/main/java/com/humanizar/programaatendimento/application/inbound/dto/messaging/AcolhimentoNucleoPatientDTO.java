package com.humanizar.programaatendimento.application.inbound.dto.messaging;

import java.util.List;
import java.util.UUID;

import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoResponsavelDTO;

public record AcolhimentoNucleoPatientDTO(
        UUID nucleoPatientId,
        UUID nucleoId,
        List<NucleoResponsavelDTO> nucleoPatientResponsavel) {
}
