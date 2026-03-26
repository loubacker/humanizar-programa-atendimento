package com.humanizar.programaatendimento.application.inbound.messaging.handler.acolhimento;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.application.catalog.ConsumerCatalog;
import com.humanizar.programaatendimento.application.catalog.RoutingKeyCatalog;
import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.messaging.AcolhimentoUpdatedDTO;
import com.humanizar.programaatendimento.application.inbound.messaging.handler.EventOutcome;
import com.humanizar.programaatendimento.application.inbound.messaging.mapper.acolhimento.InboundAcolhimentoUpdateMapper;
import com.humanizar.programaatendimento.application.usecase.acolhimento.AcolhimentoUpdatedUseCase;

@Component
public class AcolhimentoUpdatedRoutingHandler implements AcolhimentoRoutingHandler {

    private final InboundAcolhimentoUpdateMapper inboundAcolhimentoUpdateMapper;
    private final AcolhimentoUpdatedUseCase acolhimentoUpdatedUseCase;

    public AcolhimentoUpdatedRoutingHandler(
            InboundAcolhimentoUpdateMapper inboundAcolhimentoUpdateMapper,
            AcolhimentoUpdatedUseCase acolhimentoUpdatedUseCase) {
        this.inboundAcolhimentoUpdateMapper = inboundAcolhimentoUpdateMapper;
        this.acolhimentoUpdatedUseCase = acolhimentoUpdatedUseCase;
    }

    @Override
    public String routingKey() {
        return RoutingKeyCatalog.ACOLHIMENTO_UPDATED_V1;
    }

    @Override
    public EventOutcome handle(OutboundEnvelopeDTO<Object> envelope, String sourceExchange) {
        AcolhimentoUpdatedDTO payload = inboundAcolhimentoUpdateMapper.toPayload(envelope);
        return acolhimentoUpdatedUseCase.execute(
                ConsumerCatalog.ACOLHIMENTO_CONSUMER,
                routingKey(),
                envelope,
                payload,
                sourceExchange);
    }
}
