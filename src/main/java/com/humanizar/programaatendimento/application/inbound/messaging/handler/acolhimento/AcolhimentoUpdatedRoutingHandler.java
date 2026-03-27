package com.humanizar.programaatendimento.application.inbound.messaging.handler.acolhimento;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.application.catalog.ConsumerCatalog;
import com.humanizar.programaatendimento.application.catalog.RoutingKeyCatalog;
import com.humanizar.programaatendimento.application.inbound.dto.messaging.AcolhimentoUpsertDTO;
import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.messaging.handler.EventOutcome;
import com.humanizar.programaatendimento.application.inbound.messaging.mapper.acolhimento.InboundAcolhimentoUpsertMapper;
import com.humanizar.programaatendimento.application.usecase.acolhimento.AcolhimentoUpsertUseCase;

@Component
public class AcolhimentoUpdatedRoutingHandler implements AcolhimentoRoutingHandler {

    private final InboundAcolhimentoUpsertMapper inboundAcolhimentoUpsertMapper;
    private final AcolhimentoUpsertUseCase acolhimentoUpsertUseCase;

    public AcolhimentoUpdatedRoutingHandler(
            InboundAcolhimentoUpsertMapper inboundAcolhimentoUpsertMapper,
            AcolhimentoUpsertUseCase acolhimentoUpsertUseCase) {
        this.inboundAcolhimentoUpsertMapper = inboundAcolhimentoUpsertMapper;
        this.acolhimentoUpsertUseCase = acolhimentoUpsertUseCase;
    }

    @Override
    public String routingKey() {
        return RoutingKeyCatalog.ACOLHIMENTO_UPDATED_V1;
    }

    @Override
    public EventOutcome handle(OutboundEnvelopeDTO<Object> envelope, String sourceExchange) {
        AcolhimentoUpsertDTO payload = inboundAcolhimentoUpsertMapper.toPayload(envelope);
        return acolhimentoUpsertUseCase.execute(
                ConsumerCatalog.ACOLHIMENTO_CONSUMER,
                routingKey(),
                envelope,
                payload,
                sourceExchange);
    }
}
