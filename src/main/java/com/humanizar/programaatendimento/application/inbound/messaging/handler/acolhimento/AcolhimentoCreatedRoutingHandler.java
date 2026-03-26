package com.humanizar.programaatendimento.application.inbound.messaging.handler.acolhimento;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.application.catalog.ConsumerCatalog;
import com.humanizar.programaatendimento.application.catalog.RoutingKeyCatalog;
import com.humanizar.programaatendimento.application.inbound.dto.messaging.AcolhimentoCreatedDTO;
import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.messaging.handler.EventOutcome;
import com.humanizar.programaatendimento.application.inbound.messaging.mapper.acolhimento.InboundAcolhimentoCreateMapper;
import com.humanizar.programaatendimento.application.usecase.acolhimento.AcolhimentoCreatedUseCase;

@Component
public class AcolhimentoCreatedRoutingHandler implements AcolhimentoRoutingHandler {

    private final InboundAcolhimentoCreateMapper inboundAcolhimentoCreateMapper;
    private final AcolhimentoCreatedUseCase acolhimentoCreatedUseCase;

    public AcolhimentoCreatedRoutingHandler(
            InboundAcolhimentoCreateMapper inboundAcolhimentoCreateMapper,
            AcolhimentoCreatedUseCase acolhimentoCreatedUseCase) {
        this.inboundAcolhimentoCreateMapper = inboundAcolhimentoCreateMapper;
        this.acolhimentoCreatedUseCase = acolhimentoCreatedUseCase;
    }

    @Override
    public String routingKey() {
        return RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1;
    }

    @Override
    public EventOutcome handle(OutboundEnvelopeDTO<Object> envelope, String sourceExchange) {
        AcolhimentoCreatedDTO payload = inboundAcolhimentoCreateMapper.toPayload(envelope);
        return acolhimentoCreatedUseCase.execute(
                ConsumerCatalog.ACOLHIMENTO_CONSUMER,
                routingKey(),
                envelope,
                payload,
                sourceExchange);
    }
}
