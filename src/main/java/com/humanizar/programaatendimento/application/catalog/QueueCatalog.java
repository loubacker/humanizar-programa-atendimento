package com.humanizar.programaatendimento.application.catalog;

public final class QueueCatalog {

    // Fila que consome as binding keys de acolhimento create/update/delete
    public static final String PROGRAMA_ATENDIMENTO_ACOLHIMENTO = "humanizar.programa-atendimento.acolhimento";
    public static final String PROGRAMA_ATENDIMENTO_ACOLHIMENTO_DLQ = "humanizar.programa-atendimento.acolhimento.dlq";

    // Fila de callback do nucleo-relacionamento
    public static final String CALLBACK_PROGRAMA_NUCLEO_RELACIONAMENTO = "callback.programa.nucleo-relacionamento";
    public static final String CALLBACK_PROGRAMA_NUCLEO_RELACIONAMENTO_DLQ = "callback.programa.nucleo-relacionamento.dlq";

    private QueueCatalog() {
    }
}
