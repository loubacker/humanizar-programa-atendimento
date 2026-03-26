package com.humanizar.programaatendimento.application.inbound.messaging.handler.acolhimento;

import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.messaging.handler.EventOutcome;

public interface AcolhimentoRoutingHandler {

    String routingKey();

    EventOutcome handle(OutboundEnvelopeDTO<Object> envelope, String sourceExchange);
}
