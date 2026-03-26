package com.humanizar.programaatendimento.application.inbound.dto.nucleo;

import java.util.List;
import java.util.UUID;

public record NucleoPatientDTO(
        UUID nucleoPatientId,
        UUID patientId,
        UUID nucleoId,
        List<NucleoResponsavelDTO> nucleoPatientResponsavel,
        List<AbordagemPatientDTO> abordagens) {

    public NucleoPatientDTO {
        nucleoPatientResponsavel = nucleoPatientResponsavel == null ? List.of() : List.copyOf(nucleoPatientResponsavel);
        abordagens = abordagens == null ? List.of() : List.copyOf(abordagens);
    }
}
