package com.humanizar.programaatendimento.domain.model.enums;

public enum ReasonCode {

    HAS_ABORDAGEM(409, "Não é permitido remover núcleo com abordagem vinculada.", false),
    RESPONSAVEL_REQUIRED(422, "NucleoPatient exige ao menos um responsável.", false),
    PATIENT_NOT_FOUND(404, "Paciente não encontrado.", false),
    NUCLEO_PATIENT_NOT_FOUND(404, "NucleoPatient não encontrado.", false),
    ABORDAGEM_DUPLICATED(409, "Abordagem já vinculada a este NucleoPatient.", false),
    VALIDATION_ERROR(400, "Falha de validação do payload/evento.", false),
    INBOUND_REQUIRED_FIELD(400, "Campo obrigatório ausente no inbound.", false),
    INBOUND_INVALID_ENUM(400, "Valor de enum inválido no inbound.", false),
    INBOUND_INVALID_DATETIME(400, "Formato de data/hora inválido no inbound.", false),
    INBOUND_EMPTY_COLLECTION(400, "Coleção obrigatória vazia no inbound.", false),
    INBOUND_DUPLICATE_ITEM(400, "Item duplicado no inbound.", false),
    INBOUND_PARSE_ERROR(400, "Falha de parsing no inbound.", false),
    INBOUND_CONTEXT_INCONSISTENT(400, "Inconsistência entre envelop.payload e payload.", false),
    INBOUND_PATIENT_MISMATCH(400, "patientId do núcleo diverge do patientId do programa.", false),
    DUPLICATE_PATIENT(409, "Programa já existe para o patientId informado.", false),
    DELETE_IN_PROGRESS(409, "Ja existe operacao DELETE pendente para o patientId informado.", false),
    DUPLICATE_PROGRAMA_SEMANA_DIA(409, "Dia da semana duplicado em programasSemana.", false),
    DUPLICATE_AT_ESCOLA_SEMANA_DIA(409, "Dia da semana duplicado em atEscolaSemana.", false),
    DUPLICATE_NUCLEO_RESPONSAVEL(409, "Responsável duplicado para o NucleoPatient.", false),
    DUPLICATE_CONSTRAINT(409, "Violação de restrição de unicidade.", false),
    UNSUPPORTED_EVENT_VERSION(422, "Versão de evento não suportada.", false),
    UNSUPPORTED_ROUTING_KEY(400, "Routing key não suportada.", false),
    DUPLICATE_EVENT(409, "Evento duplicado já processado.", false),
    INTEGRATION_FAILURE(502, "Falha de integração com dependência externa.", true),
    PERSISTENCE_FAILURE(503, "Falha de persistência no banco de dados.", true);

    private final int statusCode;
    private final String message;
    private final boolean retryable;

    ReasonCode(int statusCode, String message, boolean retryable) {
        this.statusCode = statusCode;
        this.message = message;
        this.retryable = retryable;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRetryable() {
        return retryable;
    }
}

