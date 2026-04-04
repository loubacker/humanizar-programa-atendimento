package com.humanizar.programaatendimento.infrastructure.messaging.inbound.rabbit;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.programaatendimento.application.catalog.ConsumerCatalog;
import com.humanizar.programaatendimento.application.catalog.QueueCatalog;
import com.humanizar.programaatendimento.application.catalog.TargetCatalog;
import com.humanizar.programaatendimento.application.outbound.dto.CallbackDTO;
import com.humanizar.programaatendimento.application.service.ProgramaCallbackService;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.infrastructure.config.rabbit.RabbitAcknowledgementConfig;
import com.rabbitmq.client.Channel;

@Component
public class NucleoRelacionamentoCallbackConsumer {

    private static final Logger log = LoggerFactory.getLogger(NucleoRelacionamentoCallbackConsumer.class);

    private final ObjectMapper objectMapper;
    private final ProgramaCallbackService programaCallbackService;
    private final RabbitAcknowledgementConfig rabbitAcknowledgementConfig;

    public NucleoRelacionamentoCallbackConsumer(
            ObjectMapper objectMapper,
            ProgramaCallbackService programaCallbackService,
            RabbitAcknowledgementConfig rabbitAcknowledgementConfig) {
        this.objectMapper = objectMapper;
        this.programaCallbackService = programaCallbackService;
        this.rabbitAcknowledgementConfig = rabbitAcknowledgementConfig;
    }

    @RabbitListener(queues = QueueCatalog.CALLBACK_PROGRAMA_NUCLEO_RELACIONAMENTO, containerFactory = "rabbitListenerContainerFactory")
    public void onNucleoRelacionamentoCallback(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String queue = message.getMessageProperties().getConsumerQueue();
        String messageId = message.getMessageProperties().getMessageId();
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

        CallbackDTO callback;
        try {
            callback = objectMapper.readValue(message.getBody(), CallbackDTO.class);
        } catch (IOException ex) {
            rabbitAcknowledgementConfig.nackDeadLetter(
                    channel,
                    deliveryTag,
                    buildContext(queue, messageId, routingKey, null, null));
            return;
        }

        String eventId = callback.eventId() != null ? callback.eventId().toString() : null;
        String correlationId = callback.correlationId() != null ? callback.correlationId().toString() : null;
        String context = buildContext(queue, messageId, routingKey, eventId, correlationId);

        try {
            programaCallbackService.processCallback(
                    ConsumerCatalog.CALLBACK_NUCLEO_RELACIONAMENTO_CONSUMER,
                    TargetCatalog.TARGET_NUCLEO_RELACIONAMENTO,
                    callback);
            rabbitAcknowledgementConfig.ack(channel, deliveryTag, context);
        } catch (ProgramaAtendimentoException ex) {
            if (ex.getReasonCode() == ReasonCode.DUPLICATE_EVENT) {
                log.info("Callback duplicado confirmado. {}", context);
                rabbitAcknowledgementConfig.ack(channel, deliveryTag, context);
                return;
            }
            log.error(
                    "Falha ao processar callback do nucleo-relacionamento. reasonCode={}, retryable={}, {}",
                    ex.getReasonCode(),
                    ex.isRetryable(),
                    context,
                    ex);
            if (ex.isRetryable()) {
                rabbitAcknowledgementConfig.nackRetry(channel, deliveryTag, context);
                return;
            }
            rabbitAcknowledgementConfig.nackDeadLetter(channel, deliveryTag, context);
        } catch (RuntimeException ex) {
            log.error("Falha inesperada ao processar callback do nucleo-relacionamento. {}", context, ex);
            rabbitAcknowledgementConfig.nackRetry(channel, deliveryTag, context);
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
