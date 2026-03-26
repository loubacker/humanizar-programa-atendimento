package com.humanizar.programaatendimento.application.inbound.dto;

public record InboundContextDTO<T>(
        InboundEnvelopeDTO<T> envelop,
        T payload) {

    public InboundContextDTO {
        if (payload == null && envelop != null) {
            payload = envelop.payload();
        }
    }

    public static <T> InboundContextDTO<T> fromEnvelop(InboundEnvelopeDTO<T> envelop) {
        return new InboundContextDTO<>(envelop, envelop != null ? envelop.payload() : null);
    }
}
