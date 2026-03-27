package com.humanizar.programaatendimento.application.outbound.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.application.catalog.ExchangeCatalog;
import com.humanizar.programaatendimento.application.catalog.RoutingKeyCatalog;
import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.messaging.handler.EventOutcome;
import com.humanizar.programaatendimento.application.outbound.dto.CallbackDTO;
import com.humanizar.programaatendimento.application.outbound.mapper.OutboundCallbackMapper;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;

@Component
public class ProcessingResultPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProcessingResultPublisher.class);

    private final OutboxEventPublisher outboxEventPublisher;
    private final OutboundCallbackMapper outboundCallbackMapper;

    public ProcessingResultPublisher(
            OutboxEventPublisher outboxEventPublisher,
            OutboundCallbackMapper outboundCallbackMapper) {
        this.outboxEventPublisher = outboxEventPublisher;
        this.outboundCallbackMapper = outboundCallbackMapper;
    }

    public void publishProcessed(OutboundEnvelopeDTO<?> inboundEnvelope, String upStreamRoutingKey) {
        String processedRoutingKey = resolveProcessedRoutingKey(upStreamRoutingKey, inboundEnvelope);
        String callbackExchange = resolveCallbackExchange(upStreamRoutingKey, inboundEnvelope);
        CallbackDTO callback = outboundCallbackMapper.toProcessedCallback(
                inboundEnvelope,
                upStreamRoutingKey,
                callbackExchange,
                processedRoutingKey);

        outboxEventPublisher.publish(
                callbackExchange,
                processedRoutingKey,
                inboundEnvelope.aggregateType(),
                inboundEnvelope.aggregateId(),
                inboundEnvelope.eventId(),
                inboundEnvelope.correlationId(),
                callback,
                inboundEnvelope.actorId(),
                inboundEnvelope.userAgent(),
                inboundEnvelope.originIp());

        log.info(
                "Confirmacao PROCESSED enfileirada no outbox. upStream={}, routingKey={}, eventId={}, correlationId={}",
                upStreamRoutingKey, processedRoutingKey, inboundEnvelope.eventId(),
                inboundEnvelope.correlationId());
    }

    public void publishRejected(OutboundEnvelopeDTO<?> inboundEnvelope,
            String upStreamRoutingKey,
            EventOutcome eventOutcome) {
        String rejectedRoutingKey = resolveRejectedRoutingKey(upStreamRoutingKey, inboundEnvelope);
        String callbackExchange = resolveCallbackExchange(upStreamRoutingKey, inboundEnvelope);
        String reasonCode = eventOutcome.reasonCode() != null ? eventOutcome.reasonCode().name() : null;
        CallbackDTO callback = outboundCallbackMapper.toRejectedCallback(
                inboundEnvelope,
                upStreamRoutingKey,
                callbackExchange,
                rejectedRoutingKey,
                reasonCode,
                eventOutcome.errorMessage());

        outboxEventPublisher.publish(
                callbackExchange,
                rejectedRoutingKey,
                inboundEnvelope.aggregateType(),
                inboundEnvelope.aggregateId(),
                inboundEnvelope.eventId(),
                inboundEnvelope.correlationId(),
                callback,
                inboundEnvelope.actorId(),
                inboundEnvelope.userAgent(),
                inboundEnvelope.originIp());

        log.warn(
                "Confirmacao REJECTED enfileirada no outbox. upStream={}, routingKey={}, reasonCode={}, eventId={}, correlationId={}",
                upStreamRoutingKey, rejectedRoutingKey, reasonCode, inboundEnvelope.eventId(),
                inboundEnvelope.correlationId());
    }

    private String resolveProcessedRoutingKey(
            String upStreamRoutingKey,
            OutboundEnvelopeDTO<?> inboundEnvelope) {
        if (RoutingKeyCatalog.isAcolhimentoInbound(upStreamRoutingKey)) {
            return RoutingKeyCatalog.ACOLHIMENTO_PROGRAMA_PROCESSED_V1;
        }

        String correlationId = inboundEnvelope.correlationId() != null
                ? inboundEnvelope.correlationId().toString()
                : null;
        throw new ProgramaAtendimentoException(
                ReasonCode.UNSUPPORTED_ROUTING_KEY,
                correlationId,
                "Routing key de upstream nao suportada para confirmacao processed: " + upStreamRoutingKey);
    }

    private String resolveRejectedRoutingKey(
            String upStreamRoutingKey,
            OutboundEnvelopeDTO<?> inboundEnvelope) {
        if (RoutingKeyCatalog.isAcolhimentoInbound(upStreamRoutingKey)) {
            return RoutingKeyCatalog.ACOLHIMENTO_PROGRAMA_REJECTED_V1;
        }

        String correlationId = inboundEnvelope.correlationId() != null
                ? inboundEnvelope.correlationId().toString()
                : null;
        throw new ProgramaAtendimentoException(
                ReasonCode.UNSUPPORTED_ROUTING_KEY,
                correlationId,
                "Routing key de upstream nao suportada para confirmacao rejected: " + upStreamRoutingKey);
    }

    private String resolveCallbackExchange(
            String upStreamRoutingKey,
            OutboundEnvelopeDTO<?> inboundEnvelope) {
        if (RoutingKeyCatalog.isAcolhimentoInbound(upStreamRoutingKey)) {
            return ExchangeCatalog.ACOLHIMENTO_EVENT;
        }

        String correlationId = inboundEnvelope.correlationId() != null
                ? inboundEnvelope.correlationId().toString()
                : null;
        throw new ProgramaAtendimentoException(
                ReasonCode.UNSUPPORTED_ROUTING_KEY,
                correlationId,
                "Routing key de upstream nao suportada para resolucao de exchange de callback: " + upStreamRoutingKey);
    }
}
