package com.humanizar.programaatendimento.application.messaging.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.humanizar.programaatendimento.application.catalog.TargetCatalog;

class TargetCatalogTest {

    @Test
    void shouldExposeExpectedTargetLiterals() {
        assertEquals("humanizar-nucleo-relacionamento", TargetCatalog.TARGET_NUCLEO_RELACIONAMENTO);
    }
}
