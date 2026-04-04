package com.humanizar.programaatendimento.infrastructure.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.outbound.dto.central.PendingCentralPageDTO;
import com.humanizar.programaatendimento.application.outbound.dto.central.PendingCentralSnapshotDTO;
import com.humanizar.programaatendimento.application.service.ProgramaRetrieveService;
import com.humanizar.programaatendimento.application.service.central.ProgramaCentralListService;
import com.humanizar.programaatendimento.application.service.central.ProgramaCentralSnapshotService;
import com.humanizar.programaatendimento.infrastructure.config.ResilientMethodsConfig.Retry;

@RestController
@RequestMapping("/api/v1/programa-atendimento")
public class ProgramaRetrieveController {

    private static final Logger log = LoggerFactory.getLogger(ProgramaRetrieveController.class);

    private final ProgramaRetrieveService programaRetrieveService;
    private final ProgramaCentralListService programaCentralListService;
    private final ProgramaCentralSnapshotService programaCentralSnapshotService;

    public ProgramaRetrieveController(
            ProgramaRetrieveService programaRetrieveService,
            ProgramaCentralListService programaCentralListService,
            ProgramaCentralSnapshotService programaCentralSnapshotService) {
        this.programaRetrieveService = programaRetrieveService;
        this.programaCentralListService = programaCentralListService;
        this.programaCentralSnapshotService = programaCentralSnapshotService;
    }

    @Retry
    @GetMapping("/{patientId}")
    public ResponseEntity<ProgramaAtendimentoDTO> retrieve(@PathVariable UUID patientId) {
        log.info("Recebido GET /api/v1/programa-atendimento/{}. operacao=RETRIEVE", patientId);
        ProgramaAtendimentoDTO payload = programaRetrieveService.findByPatientId(patientId);
        log.info("GET /api/v1/programa-atendimento/{} concluido com sucesso", patientId);
        return ResponseEntity.ok(payload);
    }

    @Retry
    @GetMapping("/central/{patientId}")
    public ResponseEntity<PendingCentralPageDTO> listCentral(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Recebido GET /api/v1/programa-atendimento/central/{}. page={}, size={}", patientId, page, size);
        PendingCentralPageDTO response = programaCentralListService.execute(patientId, page, size);
        log.info("GET /api/v1/programa-atendimento/central/{} Concluido. totalElements={}", patientId,
                response.totalElements());
        return ResponseEntity.ok(response);
    }

    @Retry
    @GetMapping("/central/snapshot/{eventId}")
    public ResponseEntity<PendingCentralSnapshotDTO> snapshot(@PathVariable UUID eventId) {
        log.info("Recebido GET /api/v1/programa-atendimento/central/snapshot/{}. operacao=SNAPSHOT", eventId);
        return programaCentralSnapshotService.execute(eventId)
                .map(dto -> {
                    log.info("GET /api/v1/programa-atendimento/central/snapshot/{} Concluido com Sucesso", eventId);
                    return ResponseEntity.ok(dto);
                })
                .orElseGet(() -> {
                    log.warn("GET /api/v1/programa-atendimento/central/snapshot/{} Nao encontrado", eventId);
                    return ResponseEntity.notFound().build();
                });
    }
}
