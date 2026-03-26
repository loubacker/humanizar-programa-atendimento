package com.humanizar.programaatendimento.infrastructure.config.rabbit.binding;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.humanizar.programaatendimento.application.catalog.QueueCatalog;
import com.humanizar.programaatendimento.application.catalog.RoutingKeyCatalog;

@Configuration
public class RabbitCallbackBindingConfig {

    private static final int INBOUND_DELIVERY_LIMIT = 3;

    @Bean
    public Queue callbackNucleoRelacionamentoDlq() {
        return QueueBuilder.durable(QueueCatalog.CALLBACK_PROGRAMA_NUCLEO_RELACIONAMENTO_DLQ)
                .quorum()
                .build();
    }

    @Bean
    public Queue callbackNucleoRelacionamentoQueue() {
        return QueueBuilder.durable(QueueCatalog.CALLBACK_PROGRAMA_NUCLEO_RELACIONAMENTO)
                .quorum()
                .withArgument("x-delivery-limit", INBOUND_DELIVERY_LIMIT)
                .deadLetterExchange("")
                .deadLetterRoutingKey(QueueCatalog.CALLBACK_PROGRAMA_NUCLEO_RELACIONAMENTO_DLQ)
                .build();
    }

    @Bean
    public Binding bindCallbackNucleoProcessed(
            @Qualifier("callbackNucleoRelacionamentoQueue") Queue callbackQueue,
            @Qualifier("programaEventExchange") TopicExchange programaEventExchange) {
        return BindingBuilder.bind(callbackQueue)
                .to(programaEventExchange)
                .with(RoutingKeyCatalog.PROGRAMA_NUCLEO_RELACIONAMENTO_PROCESSED_V1);
    }

    @Bean
    public Binding bindCallbackNucleoRejected(
            @Qualifier("callbackNucleoRelacionamentoQueue") Queue callbackQueue,
            @Qualifier("programaEventExchange") TopicExchange programaEventExchange) {
        return BindingBuilder.bind(callbackQueue)
                .to(programaEventExchange)
                .with(RoutingKeyCatalog.PROGRAMA_NUCLEO_RELACIONAMENTO_REJECTED_V1);
    }
}
