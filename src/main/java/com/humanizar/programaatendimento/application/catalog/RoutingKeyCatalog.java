package com.humanizar.programaatendimento.application.catalog;

public final class RoutingKeyCatalog {

    // Inbound command de acolhimento
    public static final String ACOLHIMENTO_CREATED_V1 = "cmd.acolhimento.created.v1";
    public static final String ACOLHIMENTO_UPDATED_V1 = "cmd.acolhimento.updated.v1";
    public static final String ACOLHIMENTO_DELETED_V1 = "cmd.acolhimento.deleted.v1";

    // Outbound command para nucleo-relacionamento
    public static final String PROGRAMA_CREATED_V1 = "cmd.programa.created.v1";
    public static final String PROGRAMA_UPDATED_V1 = "cmd.programa.updated.v1";
    public static final String PROGRAMA_DELETED_V1 = "cmd.programa.deleted.v1";

    // Outbound callback para acolhimento
    public static final String ACOLHIMENTO_PROGRAMA_PROCESSED_V1 = "ev.acolhimento.programa.processed.v1";
    public static final String ACOLHIMENTO_PROGRAMA_REJECTED_V1 = "ev.acolhimento.programa.rejected.v1";

    // Inbound callback para nucleo-relacionamento
    public static final String PROGRAMA_NUCLEO_RELACIONAMENTO_PROCESSED_V1 = "ev.programa.nucleo-relacionamento.processed.v1";
    public static final String PROGRAMA_NUCLEO_RELACIONAMENTO_REJECTED_V1 = "ev.programa.nucleo-relacionamento.rejected.v1";

    private RoutingKeyCatalog() {
    }

    public static boolean isAcolhimentoInbound(String routingKey) {
        return ACOLHIMENTO_CREATED_V1.equals(routingKey)
                || ACOLHIMENTO_UPDATED_V1.equals(routingKey)
                || ACOLHIMENTO_DELETED_V1.equals(routingKey);
    }

    public static boolean isNucleoRelacionamentoCallback(String routingKey) {
        return PROGRAMA_NUCLEO_RELACIONAMENTO_PROCESSED_V1.equals(routingKey)
                || PROGRAMA_NUCLEO_RELACIONAMENTO_REJECTED_V1.equals(routingKey);
    }
}
