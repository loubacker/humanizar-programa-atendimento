package com.humanizar.programaatendimento.application.usecase.acolhimento;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.messaging.AcolhimentoUpdatedDTO;
import com.humanizar.programaatendimento.application.inbound.messaging.handler.EventOutcome;
import com.humanizar.programaatendimento.application.service.AcolhimentoInboundService;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.ProcessedEvent;
import com.humanizar.programaatendimento.domain.model.enums.ProcessedResult;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.port.ProcessedEventPort;

@Component
public class AcolhimentoUpdatedUseCase {

    private static final Logger log = LoggerFactory.getLogger(AcolhimentoUpdatedUseCase.class);

    private final AcolhimentoInboundService nucleoPatientService;
    private final ProcessedEventPort processedEventPort;

    public AcolhimentoUpdatedUseCase(
            AcolhimentoInboundService nucleoPatientService,
            ProcessedEventPort processedEventPort) {
        this.nucleoPatientService = nucleoPatientService;
        this.processedEventPort = processedEventPort;
    }

    public EventOutcome execute(
            String consumerName,
            String routingKey,
            OutboundEnvelopeDTO<?> envelope,
            AcolhimentoUpdatedDTO command,
            String sourceExchange) {

        try {
            nucleoPatientService.applyNucleoPatientSnapshot(
                    command.patientId(),
                    command.nucleoPatient(),
                    envelope.correlationId());

            saveProcessedEvent(buildProcessedEvent(
                    consumerName,
                    envelope,
                    sourceExchange,
                    routingKey,
                    ProcessedResult.SUCCESS,
                    null,
                    null));
            return EventOutcome.success();

        } catch (DataIntegrityViolationException ex) {
            log.info(
                    "DataIntegrityViolation no processamento de atualização -- possível duplicata. consumer={}, eventId={}",
                    consumerName, envelope.eventId());
            return EventOutcome.ignored(ReasonCode.DUPLICATE_EVENT);

        } catch (ProgramaAtendimentoException ex) {
            return handleDomainException(ex, consumerName, routingKey, envelope, sourceExchange);

        } catch (Exception ex) {
            return handleUnexpectedException(ex, consumerName, routingKey, envelope, sourceExchange);
        }
    }

    private EventOutcome handleDomainException(
            ProgramaAtendimentoException ex,
            String consumerName,
            String routingKey,
            OutboundEnvelopeDTO<?> envelope,
            String sourceExchange) {

        ReasonCode reason = ex.getReasonCode() != null
                ? ex.getReasonCode()
                : ReasonCode.VALIDATION_ERROR;

        if (reason == ReasonCode.DUPLICATE_EVENT) {
            log.info("Evento duplicado ignorado. eventId={}, correlationId={}, consumer={}",
                    envelope.eventId(), envelope.correlationId(), consumerName);
            saveProcessedEvent(buildProcessedEvent(
                    consumerName,
                    envelope,
                    sourceExchange,
                    routingKey,
                    ProcessedResult.IGNORED,
                    reason,
                    reason.name().toLowerCase()));
            return EventOutcome.ignored(reason);
        }

        if (reason.isRetryable()) {
            log.error("Falha retentavel. reasonCode={}, eventId={}, correlationId={}, consumer={}",
                    reason, envelope.eventId(), envelope.correlationId(), consumerName, ex);
            return EventOutcome.failed(reason);
        }

        log.warn("Falha funcional nao retentavel. reasonCode={}, eventId={}, correlationId={}, consumer={}",
                reason, envelope.eventId(), envelope.correlationId(), consumerName);
        saveProcessedEvent(buildProcessedEvent(
                consumerName,
                envelope,
                sourceExchange,
                routingKey,
                ProcessedResult.FAILED,
                reason,
                reason.name().toLowerCase()));
        return EventOutcome.failed(reason);
    }

    private EventOutcome handleUnexpectedException(
            Exception ex,
            String consumerName,
            String routingKey,
            OutboundEnvelopeDTO<?> envelope,
            String sourceExchange) {

        if (ex instanceof IllegalArgumentException) {
            String detail = "inbound_invalid_enum: " + ex.getMessage();
            log.warn(
                    "IllegalArgumentException tratada como INBOUND_INVALID_ENUM. eventId={}, correlationId={}, consumer={}",
                    envelope.eventId(), envelope.correlationId(), consumerName, ex);
            saveProcessedEvent(buildProcessedEvent(
                    consumerName,
                    envelope,
                    sourceExchange,
                    routingKey,
                    ProcessedResult.FAILED,
                    ReasonCode.INBOUND_INVALID_ENUM,
                    detail));
            return EventOutcome.failed(ReasonCode.INBOUND_INVALID_ENUM, detail);
        }

        log.error(
                "Excecao inesperada. eventId={}, correlationId={}, consumer={}, routingKey={}, aggregateType={}, aggregateId={}",
                envelope.eventId(), envelope.correlationId(), consumerName,
                routingKey, envelope.aggregateType(), envelope.aggregateId(), ex);

        String exceptionType = ex != null ? ex.getClass().getSimpleName() : "UnknownException";
        return EventOutcome.failed(ReasonCode.PERSISTENCE_FAILURE,
                "unexpected_error: " + exceptionType);
    }

    private ProcessedEvent buildProcessedEvent(
            String consumerName,
            OutboundEnvelopeDTO<?> envelope,
            String sourceExchange,
            String sourceRoutingKey,
            ProcessedResult result,
            ReasonCode reasonCode,
            String errorMessage) {
        return ProcessedEvent.builder()
                .consumerName(consumerName)
                .eventId(envelope.eventId())
                .correlationId(envelope.correlationId())
                .sourceExchange(sourceExchange)
                .sourceRoutingKey(sourceRoutingKey)
                .aggregateType(envelope.aggregateType())
                .aggregateId(envelope.aggregateId())
                .actorId(envelope.actorId())
                .userAgent(envelope.userAgent())
                .originIp(envelope.originIp())
                .processedAt(LocalDateTime.now())
                .result(result)
                .reasonCode(reasonCode)
                .errorMessage(errorMessage)
                .build();
    }

    private void saveProcessedEvent(ProcessedEvent event) {
        try {
            processedEventPort.save(event);
        } catch (DataIntegrityViolationException ex) {
            log.info(
                    "Evento duplicado detectado via constraint ao gravar processed_event. consumer={}, eventId={}. Ignorando.",
                    event.getConsumerName(), event.getEventId());
        }
    }
}
