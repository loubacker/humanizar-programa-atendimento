package com.humanizar.programaatendimento.infrastructure.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.humanizar.programaatendimento.application.inbound.dto.InboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaDeleteDTO;
import com.humanizar.programaatendimento.application.service.ProgramaDeleteService;
import com.humanizar.programaatendimento.infrastructure.controller.dto.ProgramaAtendimentoDeleteResponseDTO;

@RestController
@RequestMapping("/api/v1/programa-atendimento")
public class ProgramaDeleteController {

    private static final Logger log = LoggerFactory.getLogger(ProgramaDeleteController.class);

    private final ProgramaDeleteService programaDeleteService;

    public ProgramaDeleteController(ProgramaDeleteService programaDeleteService) {
        this.programaDeleteService = programaDeleteService;
    }

    @DeleteMapping("/delete/{patientId}")
    public ResponseEntity<ProgramaAtendimentoDeleteResponseDTO> delete(
            @PathVariable UUID patientId,
            @RequestBody InboundEnvelopeDTO<ProgramaDeleteDTO> envelop) {
        String correlationId = envelop.correlationIdAsString();
        String payloadPatientId = envelop.payload() != null && envelop.payload().patientId() != null
                ? envelop.payload().patientId().toString()
                : null;

        log.info("Recebido DELETE /api/v1/programa-atendimento/delete/{}. correlationId={}, payloadPatientId={}, operacao=DELETE",
                patientId, correlationId, payloadPatientId);
        ProgramaAtendimentoDeleteResponseDTO response = programaDeleteService.deleteByPatientId(
                patientId, envelop);
        log.info("DELETE /api/v1/programa-atendimento/delete/{} concluido com sucesso. correlationId={}",
                patientId, correlationId);
        return ResponseEntity.ok(response);
    }
}
