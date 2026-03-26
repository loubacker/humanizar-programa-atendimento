package com.humanizar.programaatendimento.infrastructure.config.rabbit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.humanizar.programaatendimento.application.catalog.ExchangeCatalog;
import com.humanizar.programaatendimento.application.catalog.QueueCatalog;
import com.humanizar.programaatendimento.application.catalog.RoutingKeyCatalog;
import com.humanizar.programaatendimento.infrastructure.config.rabbit.binding.RabbitAcolhimentoBindingConfig;

@SpringBootTest(classes = {
        RabbitExchangeConfig.class,
        RabbitQueueConfig.class,
        RabbitAcolhimentoBindingConfig.class
})
class RabbitTopologyConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("acolhimentoExchange")
    private TopicExchange acolhimentoExchange;

    @Autowired
    @Qualifier("acolhimentoEventExchange")
    private TopicExchange acolhimentoEventExchange;

    @Autowired
    @Qualifier("programaExchange")
    private TopicExchange programaExchange;

    @Autowired
    @Qualifier("programaEventExchange")
    private TopicExchange programaEventExchange;

    @Autowired
    @Qualifier("programaAcolhimentoQueue")
    private Queue programaAcolhimentoQueue;

    @Autowired
    @Qualifier("programaAcolhimentoDlq")
    private Queue programaAcolhimentoDlq;

    @Autowired
    private Binding bindAcolhimentoCreated;

    @Autowired
    private Binding bindAcolhimentoUpdated;

    @Autowired
    private Binding bindAcolhimentoDeleted;

    @Test
    void shouldDeclareExpectedTopicExchanges() {
        assertEquals(ExchangeCatalog.ACOLHIMENTO_COMMAND, acolhimentoExchange.getName());
        assertEquals(ExchangeCatalog.ACOLHIMENTO_EVENT, acolhimentoEventExchange.getName());
        assertEquals(ExchangeCatalog.PROGRAMA_COMMAND, programaExchange.getName());
        assertEquals(ExchangeCatalog.PROGRAMA_EVENT, programaEventExchange.getName());
    }

    @Test
    void shouldDeclareExpectedQueuesAndDlqStrategy() {
        assertEquals(QueueCatalog.PROGRAMA_ATENDIMENTO_ACOLHIMENTO, programaAcolhimentoQueue.getName());
        assertEquals(QueueCatalog.PROGRAMA_ATENDIMENTO_ACOLHIMENTO_DLQ, programaAcolhimentoDlq.getName());

        assertQueueHasDeliveryLimitAndDlq(programaAcolhimentoQueue, QueueCatalog.PROGRAMA_ATENDIMENTO_ACOLHIMENTO_DLQ);
    }

    @Test
    void shouldDeclareExpectedBindings() {
        assertBinding(bindAcolhimentoCreated, QueueCatalog.PROGRAMA_ATENDIMENTO_ACOLHIMENTO, ExchangeCatalog.ACOLHIMENTO_COMMAND,
                RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1);
        assertBinding(bindAcolhimentoUpdated, QueueCatalog.PROGRAMA_ATENDIMENTO_ACOLHIMENTO, ExchangeCatalog.ACOLHIMENTO_COMMAND,
                RoutingKeyCatalog.ACOLHIMENTO_UPDATED_V1);
        assertBinding(bindAcolhimentoDeleted, QueueCatalog.PROGRAMA_ATENDIMENTO_ACOLHIMENTO, ExchangeCatalog.ACOLHIMENTO_COMMAND,
                RoutingKeyCatalog.ACOLHIMENTO_DELETED_V1);

        Map<String, Binding> bindings = applicationContext.getBeansOfType(Binding.class);
        assertNotNull(bindings);
        assertEquals(3, bindings.size());
    }

    private void assertQueueHasDeliveryLimitAndDlq(Queue queue, String expectedDlqRoutingKey) {
        Map<String, Object> arguments = queue.getArguments();
        assertNotNull(arguments);
        assertEquals(3, arguments.get("x-delivery-limit"));
        assertEquals("", arguments.get("x-dead-letter-exchange"));
        assertEquals(expectedDlqRoutingKey, arguments.get("x-dead-letter-routing-key"));
        assertEquals("quorum", arguments.get("x-queue-type"));
    }

    private void assertBinding(Binding binding, String expectedDestination, String expectedExchange,
            String expectedRoutingKey) {
        assertEquals(expectedDestination, binding.getDestination());
        assertEquals(expectedExchange, binding.getExchange());
        assertEquals(expectedRoutingKey, binding.getRoutingKey());
        assertEquals(DestinationType.QUEUE, binding.getDestinationType());
    }
}
