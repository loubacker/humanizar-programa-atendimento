package com.humanizar.programaatendimento.application.inbound.dto;

import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaDeleteDTO;

public record InboundDeleteContextDTO(
        InboundEnvelopeDTO<ProgramaDeleteDTO> envelop,
        ProgramaDeleteDTO payload) {

    public InboundDeleteContextDTO {
        if (payload == null && envelop != null) {
            payload = envelop.payload();
        }
    }
}
