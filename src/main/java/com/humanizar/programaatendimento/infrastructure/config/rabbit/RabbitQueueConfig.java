package com.humanizar.programaatendimento.infrastructure.config.rabbit;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.humanizar.programaatendimento.application.catalog.QueueCatalog;

@Configuration
public class RabbitQueueConfig {

    private static final int INBOUND_DELIVERY_LIMIT = 3;

    @Bean
    public Queue programaAcolhimentoDlq() {
        return QueueBuilder.durable(QueueCatalog.PROGRAMA_ATENDIMENTO_ACOLHIMENTO_DLQ)
                .quorum()
                .build();
    }

    @Bean
    public Queue programaAcolhimentoQueue() {
        return QueueBuilder.durable(QueueCatalog.PROGRAMA_ATENDIMENTO_ACOLHIMENTO)
                .quorum()
                .withArgument("x-delivery-limit", INBOUND_DELIVERY_LIMIT)
                .deadLetterExchange("")
                .deadLetterRoutingKey(QueueCatalog.PROGRAMA_ATENDIMENTO_ACOLHIMENTO_DLQ)
                .build();
    }
}
