package com.humanizar.programaatendimento.infrastructure.messaging.inbound.rabbit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.programaatendimento.application.catalog.RoutingKeyCatalog;
import com.humanizar.programaatendimento.application.inbound.messaging.handler.EventOutcome;
import com.humanizar.programaatendimento.application.outbound.publisher.ProcessingResultPublisher;
import com.humanizar.programaatendimento.infrastructure.config.rabbit.RabbitAcknowledgementConfig;
import com.rabbitmq.client.Channel;

@ExtendWith(MockitoExtension.class)
class DeadLetterConsumerTest {

    @Mock
    private ProcessingResultPublisher processingResultPublisher;

    @Mock
    private Channel channel;

    @Test
    void shouldPublishRejectedAndAckForAcolhimentoDeadLetter() throws IOException {
        long deliveryTag = 21L;
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        DeadLetterConsumer consumer = new DeadLetterConsumer(
                objectMapper,
                processingResultPublisher,
                new RabbitAcknowledgementConfig());

        Message message = message(
                objectMapper.writeValueAsBytes(envelopePayload()),
                RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1,
                deliveryTag);

        consumer.onDeadLetter(message, channel);

        verify(processingResultPublisher).publishRejected(
                any(),
                eq(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1),
                any(EventOutcome.class));
        verify(channel).basicAck(deliveryTag, false);
    }

    @Test
    void shouldAckOnlyWhenDeadLetterPayloadCannotBeParsed() throws IOException {
        long deliveryTag = 22L;
        DeadLetterConsumer consumer = new DeadLetterConsumer(
                new ObjectMapper().findAndRegisterModules(),
                processingResultPublisher,
                new RabbitAcknowledgementConfig());

        Message message = message(
                "{not-json".getBytes(),
                RoutingKeyCatalog.ACOLHIMENTO_UPDATED_V1,
                deliveryTag);

        consumer.onDeadLetter(message, channel);

        verify(processingResultPublisher, never()).publishRejected(any(), any(), any());
        verify(channel).basicAck(deliveryTag, false);
    }

    private Map<String, Object> envelopePayload() {
        return Map.ofEntries(
                Map.entry("eventId", UUID.randomUUID().toString()),
                Map.entry("correlationId", UUID.randomUUID().toString()),
                Map.entry("producerService", "humanizar-acolhimento"),
                Map.entry("exchangeName", "humanizar.acolhimento.command"),
                Map.entry("routingKey", RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1),
                Map.entry("aggregateType", "acolhimento"),
                Map.entry("aggregateId", UUID.randomUUID().toString()),
                Map.entry("eventVersion", 1),
                Map.entry("occurredAt", LocalDateTime.now().toString()),
                Map.entry("actorId", UUID.randomUUID().toString()),
                Map.entry("userAgent", "JUnit"),
                Map.entry("originIp", "127.0.0.1"),
                Map.entry("payload", Map.of("patientId", UUID.randomUUID().toString())));
    }

    private Message message(byte[] payload, String originalRoutingKey, long deliveryTag) {
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(deliveryTag);
        properties.setMessageId(UUID.randomUUID().toString());
        properties.setConsumerQueue("humanizar-programa.acolhimento.dlq");
        properties.setReceivedRoutingKey(originalRoutingKey);
        properties.setHeader("x-death", List.of(Map.of("routing-keys", List.of(originalRoutingKey))));
        return new Message(payload, properties);
    }
}
