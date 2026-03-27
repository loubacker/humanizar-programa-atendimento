package com.humanizar.programaatendimento.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.humanizar.programaatendimento.application.inbound.dto.InboundDeleteContextDTO;
import com.humanizar.programaatendimento.application.inbound.dto.InboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaDeleteDTO;
import com.humanizar.programaatendimento.application.inbound.mapper.InboundDeleteContextMapper;
import com.humanizar.programaatendimento.application.usecase.outbox.DeleteOutboxCommandUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaSnapshotUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.DeleteAbordagensUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.DeleteProgramaTreeUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaTemplateUsecase;
import com.humanizar.programaatendimento.application.usecase.programa.SavePendingProgramaUseCase;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.pending.PendingProgramaAtendimento;
import com.humanizar.programaatendimento.domain.model.enums.OperationType;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaAtendimento;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtendimentoPort;
import com.humanizar.programaatendimento.infrastructure.controller.dto.ProgramaAtendimentoDeleteResponseDTO;

@Service
public class ProgramaDeleteService {

    private final InboundDeleteContextMapper inboundDeleteContextMapper;
    private final ProgramaAtendimentoPort programaAtendimentoPort;
    private final AcolhimentoInboundService acolhimentoInboundService;
    private final DeleteProgramaTreeUseCase deleteProgramaTreeUseCase;
    private final DeleteAbordagensUseCase deleteAbordagensUseCase;
    private final BuildProgramaSnapshotUseCase buildProgramaSnapshotUseCase;
    private final SavePendingProgramaUseCase savePendingProgramaUseCase;
    private final BuildProgramaTemplateUsecase buildProgramaTemplateUsecase;
    private final DeleteOutboxCommandUseCase deleteOutboxCommandUseCase;

    public ProgramaDeleteService(
            InboundDeleteContextMapper inboundDeleteContextMapper,
            ProgramaAtendimentoPort programaAtendimentoPort,
            AcolhimentoInboundService acolhimentoInboundService,
            DeleteProgramaTreeUseCase deleteProgramaTreeUseCase,
            DeleteAbordagensUseCase deleteAbordagensUseCase,
            BuildProgramaSnapshotUseCase buildProgramaSnapshotUseCase,
            SavePendingProgramaUseCase savePendingProgramaUseCase,
            BuildProgramaTemplateUsecase buildProgramaTemplateUsecase,
            DeleteOutboxCommandUseCase deleteOutboxCommandUseCase) {
        this.inboundDeleteContextMapper = inboundDeleteContextMapper;
        this.programaAtendimentoPort = programaAtendimentoPort;
        this.acolhimentoInboundService = acolhimentoInboundService;
        this.deleteProgramaTreeUseCase = deleteProgramaTreeUseCase;
        this.deleteAbordagensUseCase = deleteAbordagensUseCase;
        this.buildProgramaSnapshotUseCase = buildProgramaSnapshotUseCase;
        this.savePendingProgramaUseCase = savePendingProgramaUseCase;
        this.buildProgramaTemplateUsecase = buildProgramaTemplateUsecase;
        this.deleteOutboxCommandUseCase = deleteOutboxCommandUseCase;
    }

    @Transactional
    public ProgramaAtendimentoDeleteResponseDTO deleteByPatientId(
            UUID pathPatientId,
            InboundEnvelopeDTO<ProgramaDeleteDTO> envelop) {
        InboundDeleteContextDTO context = inboundDeleteContextMapper.fromDelete(pathPatientId, envelop);
        UUID patientId = context.payload().patientId();
        UUID correlationId = context.envelop().correlationId();
        String correlationIdText = correlationId != null ? correlationId.toString() : null;

        ProgramaAtendimento existing = programaAtendimentoPort.findByPatientId(patientId)
                .orElseThrow(() -> new ProgramaAtendimentoException(
                        ReasonCode.PATIENT_NOT_FOUND, correlationIdText,
                        "Programa nao encontrado para patientId=" + patientId));

        ProgramaAtendimentoDTO snapshot = buildProgramaSnapshotUseCase.buildSnapshot(existing, patientId);

        PendingProgramaAtendimento pending = savePendingProgramaUseCase.save(
                correlationId, patientId, existing.getId(), OperationType.DELETE,
                savePendingProgramaUseCase.serializePayload(snapshot, correlationIdText));

        return buildProgramaTemplateUsecase.executeWithPendingGuard(
                pending.getEventId(), correlationIdText, false,
                () -> {
                    deleteProgramaTreeUseCase.execute(existing.getId());
                    deleteAbordagensUseCase.execute(patientId);

                    acolhimentoInboundService.deleteAllNucleosByPatientId(
                            patientId, correlationId);

                    programaAtendimentoPort.deleteByPatientId(patientId);

                    deleteOutboxCommandUseCase.execute(
                            context.envelop(), pending.getEventId(), existing.getId());

                    return new ProgramaAtendimentoDeleteResponseDTO("SUCCESS", "DELETE", patientId);
                });
    }
}
