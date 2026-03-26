package com.humanizar.programaatendimento.application.inbound.messaging.handler.acolhimento;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.application.catalog.ConsumerCatalog;
import com.humanizar.programaatendimento.application.catalog.RoutingKeyCatalog;
import com.humanizar.programaatendimento.application.inbound.dto.messaging.AcolhimentoDeletedDTO;
import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.messaging.handler.EventOutcome;
import com.humanizar.programaatendimento.application.inbound.messaging.mapper.acolhimento.InboundAcolhimentoDeleteMapper;
import com.humanizar.programaatendimento.application.usecase.acolhimento.AcolhimentoDeletedUseCase;

@Component
public class AcolhimentoDeletedRoutingHandler implements AcolhimentoRoutingHandler {

    private final InboundAcolhimentoDeleteMapper inboundAcolhimentoDeleteMapper;
    private final AcolhimentoDeletedUseCase acolhimentoDeletedUseCase;

    public AcolhimentoDeletedRoutingHandler(
            InboundAcolhimentoDeleteMapper inboundAcolhimentoDeleteMapper,
            AcolhimentoDeletedUseCase acolhimentoDeletedUseCase) {
        this.inboundAcolhimentoDeleteMapper = inboundAcolhimentoDeleteMapper;
        this.acolhimentoDeletedUseCase = acolhimentoDeletedUseCase;
    }

    @Override
    public String routingKey() {
        return RoutingKeyCatalog.ACOLHIMENTO_DELETED_V1;
    }

    @Override
    public EventOutcome handle(OutboundEnvelopeDTO<Object> envelope, String sourceExchange) {
        AcolhimentoDeletedDTO payload = inboundAcolhimentoDeleteMapper.toPayload(envelope);
        return acolhimentoDeletedUseCase.execute(
                ConsumerCatalog.ACOLHIMENTO_CONSUMER,
                routingKey(),
                envelope,
                payload,
                sourceExchange);
    }
}
