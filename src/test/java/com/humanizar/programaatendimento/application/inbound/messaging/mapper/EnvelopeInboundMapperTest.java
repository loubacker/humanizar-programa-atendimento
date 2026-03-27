package com.humanizar.programaatendimento.application.inbound.messaging.mapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;

class EnvelopeInboundMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final EnvelopeInboundMapper mapper = new EnvelopeInboundMapper(objectMapper);

    @Test
    void shouldParseAndValidateEnvelopeSuccessfully() throws Exception {
        byte[] body = objectMapper.writeValueAsBytes(validEnvelope());
        OutboundEnvelopeDTO<Object> parsed = mapper.parseEnvelope(body);

        assertDoesNotThrow(() -> mapper.validate(parsed));
        assertEquals(parsed.correlationId().toString(), InboundAcolhimentoValidation.correlationIdAsString(parsed));
    }

    @Test
    void shouldFailWhenBodyCannotBeParsed() {
        ProgramaAtendimentoException ex = assertThrows(
                ProgramaAtendimentoException.class,
                () -> mapper.parseEnvelope("{not-json".getBytes(StandardCharsets.UTF_8)));

        assertEquals(ReasonCode.INBOUND_PARSE_ERROR, ex.getReasonCode());
    }

    @Test
    void shouldFailWhenMandatoryFieldIsMissing() {
        OutboundEnvelopeDTO<Object> envelope = new OutboundEnvelopeDTO<>(
                null,
                UUID.randomUUID(),
                "humanizar-acolhimento",
                "humanizar.acolhimento.command",
                "cmd.acolhimento.created.v1",
                "acolhimento",
                UUID.randomUUID(),
                1,
                LocalDateTime.now(),
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1",
                java.util.Map.of("patientId", UUID.randomUUID().toString()));

        ProgramaAtendimentoException ex = assertThrows(
                ProgramaAtendimentoException.class,
                () -> mapper.validate(envelope));

        assertEquals(ReasonCode.INBOUND_REQUIRED_FIELD, ex.getReasonCode());
    }

    private OutboundEnvelopeDTO<Object> validEnvelope() {
        return new OutboundEnvelopeDTO<>(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "humanizar-acolhimento",
                "humanizar.acolhimento.command",
                "cmd.acolhimento.created.v1",
                "acolhimento",
                UUID.randomUUID(),
                1,
                LocalDateTime.now(),
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1",
                java.util.Map.of("patientId", UUID.randomUUID().toString()));
    }
}
