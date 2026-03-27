package com.humanizar.programaatendimento.infrastructure.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.humanizar.programaatendimento.application.inbound.dto.InboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.service.ProgramaCreateService;
import com.humanizar.programaatendimento.infrastructure.controller.dto.ProgramaAtendimentoCreateResponseDTO;

@RestController
@RequestMapping("/api/v1/programa-atendimento")
public class ProgramaCreateController {

    private static final Logger log = LoggerFactory.getLogger(ProgramaCreateController.class);

    private final ProgramaCreateService programaCreateService;

    public ProgramaCreateController(ProgramaCreateService programaCreateService) {
        this.programaCreateService = programaCreateService;
    }

    @PostMapping("/register")
    public ResponseEntity<ProgramaAtendimentoCreateResponseDTO> register(
            @RequestBody InboundEnvelopeDTO<ProgramaAtendimentoDTO> envelope) {
        String correlationId = envelope.correlationIdAsString();
        String patientId = envelope.payload() != null && envelope.payload().patientId() != null
                ? envelope.payload().patientId().toString()
                : null;

        log.info("Recebido POST register. correlationId={}, patientId={}, operacao=CREATE",
                correlationId, patientId);
        ProgramaAtendimentoCreateResponseDTO response = programaCreateService.register(envelope);
        log.info("POST register concluido com sucesso. correlationId={}, patientId={}",
                correlationId, patientId);
        return ResponseEntity.ok(response);
    }
}
