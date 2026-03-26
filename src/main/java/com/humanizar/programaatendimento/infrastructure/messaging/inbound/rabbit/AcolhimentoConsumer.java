package com.humanizar.programaatendimento.infrastructure.messaging.inbound.rabbit;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.application.catalog.ConsumerCatalog;
import com.humanizar.programaatendimento.application.catalog.QueueCatalog;
import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.messaging.handler.EventOutcome;
import com.humanizar.programaatendimento.application.inbound.messaging.handler.acolhimento.AcolhimentoRoutingHandler;
import com.humanizar.programaatendimento.application.inbound.messaging.mapper.EnvelopeInboundMapper;
import com.humanizar.programaatendimento.application.outbound.publisher.ProcessingResultPublisher;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ProcessedResult;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.infrastructure.config.rabbit.RabbitAcknowledgementConfig;
import com.humanizar.programaatendimento.infrastructure.messaging.inbound.idempotency.ProcessedEventGuard;
import com.rabbitmq.client.Channel;

@Component
public class AcolhimentoConsumer {

    private static final Logger log = LoggerFactory.getLogger(AcolhimentoConsumer.class);

    private final EnvelopeInboundMapper envelopeInboundMapper;
    private final ProcessedEventGuard processedEventGuard;
    private final ProcessingResultPublisher processingResultPublisher;
    private final Map<String, AcolhimentoRoutingHandler> routingHandlers;
    private final RabbitAcknowledgementConfig rabbitAcknowledgementConfig;

    public AcolhimentoConsumer(
            EnvelopeInboundMapper envelopeInboundMapper,
            ProcessedEventGuard processedEventGuard,
            ProcessingResultPublisher processingResultPublisher,
            List<AcolhimentoRoutingHandler> routingHandlers,
            RabbitAcknowledgementConfig rabbitAcknowledgementConfig) {
        this.envelopeInboundMapper = envelopeInboundMapper;
        this.processedEventGuard = processedEventGuard;
        this.processingResultPublisher = processingResultPublisher;
        this.routingHandlers = routingHandlers.stream()
                .collect(Collectors.toUnmodifiableMap(AcolhimentoRoutingHandler::routingKey, Function.identity()));
        this.rabbitAcknowledgementConfig = rabbitAcknowledgementConfig;
    }

    @RabbitListener(queues = QueueCatalog.PROGRAMA_ATENDIMENTO_ACOLHIMENTO, containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String queue = message.getMessageProperties().getConsumerQueue();
        String messageId = message.getMessageProperties().getMessageId();
        OutboundEnvelopeDTO<Object> envelope = null;
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        String sourceExchange = message.getMessageProperties().getReceivedExchange();
        String context = buildContext(queue, messageId, routingKey, null, null);

        try {
            envelope = envelopeInboundMapper.parseEnvelope(message.getBody());
            envelopeInboundMapper.validate(envelope);

            String correlationId = envelope.correlationId() != null
                    ? envelope.correlationId().toString()
                    : null;
            String eventId = envelope.eventId() != null ? envelope.eventId().toString() : null;
            context = buildContext(queue, messageId, routingKey, eventId, correlationId);

            processedEventGuard.ensureNotProcessed(ConsumerCatalog.ACOLHIMENTO_CONSUMER, envelope.eventId(),
                    correlationId);

            EventOutcome outcome = dispatchByRoutingKey(routingKey, envelope, sourceExchange);
            publishProcessingResult(routingKey, envelope, outcome);

            if (outcome.retryable()) {
                rabbitAcknowledgementConfig.nackRetry(channel, deliveryTag, context);
            } else {
                rabbitAcknowledgementConfig.ack(channel, deliveryTag, context);
            }

        } catch (ProgramaAtendimentoException ex) {
            if (ex.getReasonCode() == ReasonCode.INBOUND_PARSE_ERROR) {
                log.error("Falha de parse no consumer acolhimento. {}", context, ex);
                rabbitAcknowledgementConfig.nackDeadLetter(channel, deliveryTag, context);
                return;
            }

            if (ex.isRetryable()) {
                log.error("Erro retentavel no consumer acolhimento. reason={}", ex.getReasonCode(), ex);
                rabbitAcknowledgementConfig.nackRetry(channel, deliveryTag, context);
            } else {
                log.warn(
                        "Erro nao retentavel no consumer acolhimento. reason={}, message={}, {}",
                        ex.getReasonCode(),
                        ex.getMessage(),
                        context);
                tryPublishEarlyRejection(envelope, routingKey, ex);
                rabbitAcknowledgementConfig.ack(channel, deliveryTag, context);
            }
        } catch (RuntimeException ex) {
            log.error("Erro inesperado no consumer acolhimento. {}", context, ex);
            rabbitAcknowledgementConfig.nackRetry(channel, deliveryTag, context);
        }
    }

    private void tryPublishEarlyRejection(
            OutboundEnvelopeDTO<Object> envelope,
            String routingKey,
            ProgramaAtendimentoException ex) {
        if (envelope == null || routingKey == null || ex.getReasonCode() == ReasonCode.DUPLICATE_EVENT) {
            return;
        }
        try {
            EventOutcome rejection = EventOutcome.failed(ex.getReasonCode());
            publishProcessingResult(routingKey, envelope, rejection);
        } catch (Exception nested) {
            log.error("Falha ao publicar rejeicao pre-dispatch no consumer acolhimento.", nested);
        }
    }

    private EventOutcome dispatchByRoutingKey(
            String routingKey,
            OutboundEnvelopeDTO<Object> envelope,
            String sourceExchange) {
        AcolhimentoRoutingHandler handler = routingHandlers.get(routingKey);
        if (handler == null) {
            String correlationId = envelope.correlationId() != null
                    ? envelope.correlationId().toString()
                    : null;
            log.warn("Routing key nao suportada: {}", routingKey);
            throw new ProgramaAtendimentoException(
                    ReasonCode.UNSUPPORTED_ROUTING_KEY, correlationId);
        }
        return handler.handle(envelope, sourceExchange);
    }

    private void publishProcessingResult(
            String upstreamRoutingKey,
            OutboundEnvelopeDTO<Object> inboundEnvelope,
            EventOutcome eventOutcome) {
        if (eventOutcome.result() == ProcessedResult.SUCCESS) {
            processingResultPublisher.publishProcessed(inboundEnvelope, upstreamRoutingKey);
            return;
        }
        if (eventOutcome.result() == ProcessedResult.FAILED && !eventOutcome.retryable()) {
            processingResultPublisher.publishRejected(inboundEnvelope, upstreamRoutingKey, eventOutcome);
        }
    }

    private String buildContext(
            String queue,
            String messageId,
            String routingKey,
            String eventId,
            String correlationId) {
        return "queue=" + queue
                + ",messageId=" + messageId
                + ",routingKey=" + routingKey
                + ",eventId=" + eventId
                + ",correlationId=" + correlationId;
    }
}
