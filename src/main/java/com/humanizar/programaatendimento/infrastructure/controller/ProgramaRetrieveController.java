package com.humanizar.programaatendimento.infrastructure.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.service.ProgramaRetrieveService;
import com.humanizar.programaatendimento.infrastructure.config.ResilientMethodsConfig.Retry;

@RestController
@RequestMapping("/api/v1/programa-atendimento")
public class ProgramaRetrieveController {

    private static final Logger log = LoggerFactory.getLogger(ProgramaRetrieveController.class);

    private final ProgramaRetrieveService programaRetrieveService;

    public ProgramaRetrieveController(ProgramaRetrieveService programaRetrieveService) {
        this.programaRetrieveService = programaRetrieveService;
    }

    @Retry
    @GetMapping("/{patientId}")
    public ResponseEntity<ProgramaAtendimentoDTO> retrieve(@PathVariable UUID patientId) {
        log.info("Recebido GET /api/v1/programa-atendimento/{}. operacao=RETRIEVE", patientId);
        ProgramaAtendimentoDTO payload = programaRetrieveService.findByPatientId(patientId);
        log.info("GET /api/v1/programa-atendimento/{} concluido com sucesso", patientId);
        return ResponseEntity.ok(payload);
    }
}
