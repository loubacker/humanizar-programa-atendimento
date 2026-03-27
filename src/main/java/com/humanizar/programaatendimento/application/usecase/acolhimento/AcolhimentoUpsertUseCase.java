package com.humanizar.programaatendimento.application.usecase.acolhimento;

import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.application.inbound.dto.messaging.AcolhimentoUpsertDTO;
import com.humanizar.programaatendimento.application.inbound.messaging.handler.EventOutcome;
import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.service.AcolhimentoInboundService;

@Component
public class AcolhimentoUpsertUseCase {

    private final AcolhimentoInboundService nucleoPatientService;
    private final AcolhimentoEventResultHandler resultHandler;

    public AcolhimentoUpsertUseCase(
            AcolhimentoInboundService nucleoPatientService,
            AcolhimentoEventResultHandler resultHandler) {
        this.nucleoPatientService = nucleoPatientService;
        this.resultHandler = resultHandler;
    }

    public EventOutcome execute(
            String consumerName,
            String routingKey,
            OutboundEnvelopeDTO<?> envelope,
            AcolhimentoUpsertDTO command,
            String sourceExchange) {

        return resultHandler.executeWithErrorHandling(
                consumerName, routingKey, envelope, sourceExchange,
                () -> nucleoPatientService.applyNucleoPatientSnapshot(
                        command.patientId(),
                        command.nucleoPatient(),
                        envelope.correlationId()));
    }
}
