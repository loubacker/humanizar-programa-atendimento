package com.humanizar.programaatendimento.infrastructure.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.humanizar.programaatendimento.application.inbound.dto.InboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.service.ProgramaUpdateService;
import com.humanizar.programaatendimento.infrastructure.controller.dto.ProgramaAtendimentoUpdateResponseDTO;

@RestController
@RequestMapping("/api/v1/programa-atendimento")
public class ProgramaUpdateController {

    private static final Logger log = LoggerFactory.getLogger(ProgramaUpdateController.class);

    private final ProgramaUpdateService programaUpdateService;

    public ProgramaUpdateController(ProgramaUpdateService programaUpdateService) {
        this.programaUpdateService = programaUpdateService;
    }

    @PutMapping("/update/{patientId}")
    public ResponseEntity<ProgramaAtendimentoUpdateResponseDTO> update(
            @PathVariable UUID patientId,
            @RequestBody InboundEnvelopeDTO<ProgramaAtendimentoDTO> envelope) {
        String correlationId = envelope.correlationIdAsString();
        String payloadPatientId = envelope.payload() != null && envelope.payload().patientId() != null
                ? envelope.payload().patientId().toString()
                : null;

        log.info("Recebido PUT /api/v1/programa-atendimento/update/{}. correlationId={}, payloadPatientId={}, operacao=UPDATE",
                patientId, correlationId, payloadPatientId);
        ProgramaAtendimentoUpdateResponseDTO response = programaUpdateService.updateByPatientId(
                patientId, envelope);
        log.info("PUT /api/v1/programa-atendimento/update/{} concluido com sucesso. correlationId={}",
                patientId, correlationId);
        return ResponseEntity.ok(response);
    }
}
