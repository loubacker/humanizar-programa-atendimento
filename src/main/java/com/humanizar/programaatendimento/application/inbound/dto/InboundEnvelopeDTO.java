package com.humanizar.programaatendimento.application.inbound.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record InboundEnvelopeDTO<T>(
        UUID correlationId,
        String producerService,
        LocalDateTime occurredAt,
        UUID actorId,
        String userAgent,
        String originIp,
        T payload) {
}
