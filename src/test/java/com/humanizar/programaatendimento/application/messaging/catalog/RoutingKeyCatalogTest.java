package com.humanizar.programaatendimento.application.messaging.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.humanizar.programaatendimento.application.catalog.RoutingKeyCatalog;
import org.junit.jupiter.api.Test;

class RoutingKeyCatalogTest {

    @Test
    void shouldExposeExpectedRoutingKeyLiterals() {
        assertEquals("cmd.acolhimento.created.v1", RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1);
        assertEquals("cmd.acolhimento.updated.v1", RoutingKeyCatalog.ACOLHIMENTO_UPDATED_V1);
        assertEquals("cmd.acolhimento.deleted.v1", RoutingKeyCatalog.ACOLHIMENTO_DELETED_V1);
        assertEquals("ev.acolhimento.programa.processed.v1", RoutingKeyCatalog.ACOLHIMENTO_PROGRAMA_PROCESSED_V1);
        assertEquals("ev.acolhimento.programa.rejected.v1", RoutingKeyCatalog.ACOLHIMENTO_PROGRAMA_REJECTED_V1);
        assertEquals("cmd.programa.created.v1", RoutingKeyCatalog.PROGRAMA_CREATED_V1);
        assertEquals("cmd.programa.updated.v1", RoutingKeyCatalog.PROGRAMA_UPDATED_V1);
        assertEquals("cmd.programa.deleted.v1", RoutingKeyCatalog.PROGRAMA_DELETED_V1);
        assertEquals("ev.programa.nucleo-relacionamento.processed.v1", RoutingKeyCatalog.PROGRAMA_NUCLEO_RELACIONAMENTO_PROCESSED_V1);
        assertEquals("ev.programa.nucleo-relacionamento.rejected.v1", RoutingKeyCatalog.PROGRAMA_NUCLEO_RELACIONAMENTO_REJECTED_V1);
    }

    @Test
    void shouldClassifyAcolhimentoInboundRoutingKeys() {
        assertTrue(RoutingKeyCatalog.isAcolhimentoInbound(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1));
        assertTrue(RoutingKeyCatalog.isAcolhimentoInbound(RoutingKeyCatalog.ACOLHIMENTO_UPDATED_V1));
        assertTrue(RoutingKeyCatalog.isAcolhimentoInbound(RoutingKeyCatalog.ACOLHIMENTO_DELETED_V1));
        assertFalse(RoutingKeyCatalog.isAcolhimentoInbound(RoutingKeyCatalog.PROGRAMA_CREATED_V1));
    }

    @Test
    void shouldClassifyNucleoRelacionamentoCallbackRoutingKeys() {
        assertTrue(RoutingKeyCatalog.isNucleoRelacionamentoCallback(RoutingKeyCatalog.PROGRAMA_NUCLEO_RELACIONAMENTO_PROCESSED_V1));
        assertTrue(RoutingKeyCatalog.isNucleoRelacionamentoCallback(RoutingKeyCatalog.PROGRAMA_NUCLEO_RELACIONAMENTO_REJECTED_V1));
        assertFalse(RoutingKeyCatalog.isNucleoRelacionamentoCallback(RoutingKeyCatalog.ACOLHIMENTO_CREATED_V1));
    }
}
