package com.humanizar.programaatendimento.application.messaging.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.humanizar.programaatendimento.application.catalog.QueueCatalog;
import org.junit.jupiter.api.Test;

class QueueCatalogTest {

    @Test
    void shouldExposeExpectedQueueLiterals() {
        assertEquals("humanizar-programa.acolhimento", QueueCatalog.PROGRAMA_ATENDIMENTO_ACOLHIMENTO);
        assertEquals("humanizar-programa.acolhimento.dlq", QueueCatalog.PROGRAMA_ATENDIMENTO_ACOLHIMENTO_DLQ);
    }
}
