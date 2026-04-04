package com.humanizar.programaatendimento.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.humanizar.programaatendimento.application.inbound.dto.InboundContextDTO;
import com.humanizar.programaatendimento.application.inbound.dto.InboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.messaging.AcolhimentoNucleoPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.inbound.mapper.InboundContextMapper;
import com.humanizar.programaatendimento.application.inbound.mapper.InboundProgramaAtendimentoMapper;
import com.humanizar.programaatendimento.application.outbound.dto.ProgramaCommandDTO;
import com.humanizar.programaatendimento.application.usecase.outbox.UpdateOutboxCommandUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaAtendimentoUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaCommandsUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaTemplateUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.SaveAbordagensUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.SavePendingProgramaUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.SaveProgramaTreeUseCase;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.pending.PendingProgramaAtendimento;
import com.humanizar.programaatendimento.domain.model.enums.OperationType;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaAtendimento;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtendimentoPort;
import com.humanizar.programaatendimento.infrastructure.controller.dto.ProgramaAtendimentoUpdateResponseDTO;

@Service
public class ProgramaUpdateService {

    private final InboundContextMapper inboundContextMapper;
    private final InboundProgramaAtendimentoMapper inboundProgramaAtendimentoMapper;
    private final ProgramaAtendimentoPort programaAtendimentoPort;
    private final AcolhimentoInboundService acolhimentoInboundService;
    private final SaveProgramaTreeUseCase saveProgramaTreeUseCase;
    private final SaveAbordagensUseCase saveAbordagensUseCase;
    private final BuildProgramaAtendimentoUseCase buildProgramaAtendimentoUseCase;
    private final BuildProgramaCommandsUseCase buildProgramaCommandsUseCase;
    private final SavePendingProgramaUseCase savePendingProgramaUseCase;
    private final BuildProgramaTemplateUseCase buildProgramaTemplateUsecase;
    private final UpdateOutboxCommandUseCase updateOutboxCommandUseCase;

    public ProgramaUpdateService(
            InboundContextMapper inboundContextMapper,
            InboundProgramaAtendimentoMapper inboundProgramaAtendimentoMapper,
            ProgramaAtendimentoPort programaAtendimentoPort,
            AcolhimentoInboundService acolhimentoInboundService,
            SaveProgramaTreeUseCase saveProgramaTreeUseCase,
            SaveAbordagensUseCase saveAbordagensUseCase,
            BuildProgramaAtendimentoUseCase buildProgramaAtendimentoUseCase,
            BuildProgramaCommandsUseCase buildProgramaCommandsUseCase,
            SavePendingProgramaUseCase savePendingProgramaUseCase,
            BuildProgramaTemplateUseCase buildProgramaTemplateUsecase,
            UpdateOutboxCommandUseCase updateOutboxCommandUseCase) {
        this.inboundContextMapper = inboundContextMapper;
        this.inboundProgramaAtendimentoMapper = inboundProgramaAtendimentoMapper;
        this.programaAtendimentoPort = programaAtendimentoPort;
        this.acolhimentoInboundService = acolhimentoInboundService;
        this.saveProgramaTreeUseCase = saveProgramaTreeUseCase;
        this.saveAbordagensUseCase = saveAbordagensUseCase;
        this.buildProgramaAtendimentoUseCase = buildProgramaAtendimentoUseCase;
        this.buildProgramaCommandsUseCase = buildProgramaCommandsUseCase;
        this.savePendingProgramaUseCase = savePendingProgramaUseCase;
        this.buildProgramaTemplateUsecase = buildProgramaTemplateUsecase;
        this.updateOutboxCommandUseCase = updateOutboxCommandUseCase;
    }

    @Transactional
    public ProgramaAtendimentoUpdateResponseDTO updateByPatientId(
            UUID pathPatientId,
            InboundEnvelopeDTO<ProgramaAtendimentoDTO> envelope) {
        InboundContextDTO<ProgramaAtendimentoDTO> context = inboundContextMapper.fromUpdate(
                pathPatientId, envelope);
        UUID correlationId = context.envelop().correlationId();
        String correlationIdText = correlationId != null ? correlationId.toString() : null;

        ProgramaAtendimentoDTO payload = inboundProgramaAtendimentoMapper.toUpdatePayload(
                context.payload(), correlationIdText);
        UUID patientId = payload.patientId();

        ProgramaAtendimento existing = programaAtendimentoPort.findByPatientId(patientId)
                .orElseThrow(() -> new ProgramaAtendimentoException(
                        ReasonCode.PATIENT_NOT_FOUND, correlationIdText,
                        "Programa nao encontrado para patientId=" + patientId));
        UUID programaId = existing.getId();

        PendingProgramaAtendimento pending = savePendingProgramaUseCase.save(
                correlationId, patientId, programaId, OperationType.UPDATE,
                savePendingProgramaUseCase.serializePayload(payload, correlationIdText));

        return buildProgramaTemplateUsecase.executeWithPendingGuard(
                pending.getEventId(), correlationIdText, false,
                () -> {
                    ProgramaAtendimento updated = buildProgramaAtendimentoUseCase.buildForUpdate(
                            programaId, patientId, payload, correlationIdText);
                    updated.setCreatedAt(existing.getCreatedAt());
                    programaAtendimentoPort.save(updated);

                    saveProgramaTreeUseCase.saveProgramasSemana(programaId, payload.programasSemana(),
                            correlationIdText);
                    saveProgramaTreeUseCase.saveProgramasEscola(programaId, payload.programasEscola(),
                            correlationIdText);

                    List<AcolhimentoNucleoPatientDTO> nucleoCommands = toNucleoCommands(payload.nucleoPatient());
                    acolhimentoInboundService.applyNucleoPatientSnapshot(
                            patientId, nucleoCommands, correlationId);

                    saveAbordagensUseCase.execute(payload.nucleoPatient());

                    List<ProgramaCommandDTO> commandPayload = buildProgramaCommandsUseCase.execute(
                            payload.nucleoPatient());
                    updateOutboxCommandUseCase.execute(
                            context.envelop(), pending.getEventId(), programaId, commandPayload);

                    return new ProgramaAtendimentoUpdateResponseDTO(
                            "Programa de Atendimento Atualizado com Sucesso para Paciente " + patientId,
                            patientId, correlationId);
                });
    }

    private List<AcolhimentoNucleoPatientDTO> toNucleoCommands(List<NucleoPatientDTO> nucleoPatients) {
        return nucleoPatients.stream()
                .map(np -> new AcolhimentoNucleoPatientDTO(
                        np.nucleoPatientId(), np.nucleoId(), np.nucleoPatientResponsavel()))
                .toList();
    }
}
