package com.humanizar.programaatendimento.application.messaging.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.humanizar.programaatendimento.application.catalog.ConsumerCatalog;
import org.junit.jupiter.api.Test;

class ConsumerCatalogTest {

    @Test
    void shouldExposeExpectedConsumerLiterals() {
        assertEquals("acolhimento.consumer", ConsumerCatalog.ACOLHIMENTO_CONSUMER);
    }
}
