package com.humanizar.programaatendimento.application.outbound.publisher;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.application.outbound.mapper.OutboxEventMapper;
import com.humanizar.programaatendimento.domain.model.OutboxEvent;
import com.humanizar.programaatendimento.domain.port.OutboxEventPort;

@Component
public class OutboxEventPublisher {

    private final OutboxEventPort outboxEventPort;
    private final OutboxEventMapper outboxEventMapper;

    public OutboxEventPublisher(OutboxEventPort outboxEventPort, OutboxEventMapper outboxEventMapper) {
        this.outboxEventPort = outboxEventPort;
        this.outboxEventMapper = outboxEventMapper;
    }

    public void publish(String exchangeName,
            String routingKey,
            String aggregateType,
            UUID aggregateId,
            UUID eventId,
            UUID correlationId,
            Object payload,
            UUID actorId,
            String userAgent,
            String originIp) {
        OutboxEvent event = outboxEventMapper.toOutboxEvent(
                exchangeName,
                routingKey,
                aggregateType,
                aggregateId,
                eventId,
                correlationId,
                payload,
                actorId,
                userAgent,
                originIp);
        outboxEventPort.save(event);
    }
}
