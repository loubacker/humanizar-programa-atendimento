package com.humanizar.programaatendimento.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.humanizar.programaatendimento.application.inbound.dto.InboundContextDTO;
import com.humanizar.programaatendimento.application.inbound.dto.InboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.inbound.mapper.InboundContextMapper;
import com.humanizar.programaatendimento.application.inbound.mapper.InboundProgramaAtendimentoMapper;
import com.humanizar.programaatendimento.application.outbound.dto.ProgramaCommandDTO;
import com.humanizar.programaatendimento.application.usecase.outbox.CreateOutboxCommandUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaAtendimentoUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaCommandsUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.BuildProgramaTemplateUsecase;
import com.humanizar.programaatendimento.application.usecase.programa.SaveAbordagensUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.SavePendingProgramaUseCase;
import com.humanizar.programaatendimento.application.usecase.programa.SaveProgramaTreeUseCase;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.pending.PendingProgramaAtendimento;
import com.humanizar.programaatendimento.domain.model.enums.OperationType;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.programa.ProgramaAtendimento;
import com.humanizar.programaatendimento.domain.port.nucleo.NucleoPatientPort;
import com.humanizar.programaatendimento.domain.port.programa.ProgramaAtendimentoPort;
import com.humanizar.programaatendimento.infrastructure.controller.dto.ProgramaAtendimentoCreateResponseDTO;

@Service
public class ProgramaCreateService {

    private final InboundContextMapper inboundContextMapper;
    private final InboundProgramaAtendimentoMapper inboundProgramaAtendimentoMapper;
    private final ProgramaAtendimentoPort programaAtendimentoPort;
    private final AcolhimentoInboundService acolhimentoInboundService;
    private final NucleoPatientPort nucleoPatientPort;
    private final SaveProgramaTreeUseCase saveProgramaTreeUseCase;
    private final SaveAbordagensUseCase saveAbordagensUseCase;
    private final BuildProgramaAtendimentoUseCase buildProgramaAtendimentoUseCase;
    private final BuildProgramaCommandsUseCase buildProgramaCommandsUseCase;
    private final SavePendingProgramaUseCase savePendingProgramaUseCase;
    private final BuildProgramaTemplateUsecase buildProgramaTemplateUsecase;
    private final CreateOutboxCommandUseCase createOutboxCommandUseCase;

    public ProgramaCreateService(
            InboundContextMapper inboundContextMapper,
            InboundProgramaAtendimentoMapper inboundProgramaAtendimentoMapper,
            ProgramaAtendimentoPort programaAtendimentoPort,
            AcolhimentoInboundService acolhimentoInboundService,
            NucleoPatientPort nucleoPatientPort,
            SaveProgramaTreeUseCase saveProgramaTreeUseCase,
            SaveAbordagensUseCase saveAbordagensUseCase,
            BuildProgramaAtendimentoUseCase buildProgramaAtendimentoUseCase,
            BuildProgramaCommandsUseCase buildProgramaCommandsUseCase,
            SavePendingProgramaUseCase savePendingProgramaUseCase,
            BuildProgramaTemplateUsecase buildProgramaTemplateUsecase,
            CreateOutboxCommandUseCase createOutboxCommandUseCase) {
        this.inboundContextMapper = inboundContextMapper;
        this.inboundProgramaAtendimentoMapper = inboundProgramaAtendimentoMapper;
        this.programaAtendimentoPort = programaAtendimentoPort;
        this.acolhimentoInboundService = acolhimentoInboundService;
        this.nucleoPatientPort = nucleoPatientPort;
        this.saveProgramaTreeUseCase = saveProgramaTreeUseCase;
        this.saveAbordagensUseCase = saveAbordagensUseCase;
        this.buildProgramaAtendimentoUseCase = buildProgramaAtendimentoUseCase;
        this.buildProgramaCommandsUseCase = buildProgramaCommandsUseCase;
        this.savePendingProgramaUseCase = savePendingProgramaUseCase;
        this.buildProgramaTemplateUsecase = buildProgramaTemplateUsecase;
        this.createOutboxCommandUseCase = createOutboxCommandUseCase;
    }

    @Transactional
    public ProgramaAtendimentoCreateResponseDTO register(
            InboundEnvelopeDTO<ProgramaAtendimentoDTO> envelope) {
        InboundContextDTO<ProgramaAtendimentoDTO> context = inboundContextMapper.fromEnvelop(envelope);
        UUID correlationId = context.envelop().correlationId();
        String correlationIdText = correlationId != null ? correlationId.toString() : null;

        ProgramaAtendimentoDTO payload = inboundProgramaAtendimentoMapper.toCreatePayload(
                context.payload(), correlationIdText);
        UUID patientId = payload.patientId();

        PendingProgramaAtendimento pending = savePendingProgramaUseCase.save(
                correlationId, patientId, null, OperationType.CREATE,
                savePendingProgramaUseCase.serializePayload(payload, correlationIdText));

        if (programaAtendimentoPort.findByPatientId(patientId).isPresent()) {
            savePendingProgramaUseCase.markAsError(pending.getEventId());
            throw new ProgramaAtendimentoException(
                    ReasonCode.DUPLICATE_PATIENT, correlationIdText,
                    "Programa ja existe para patientId=" + patientId);
        }

        return buildProgramaTemplateUsecase.executeWithPendingGuard(
                pending.getEventId(), correlationIdText, true,
                () -> {
                    UUID programaId = UUID.randomUUID();
                    ProgramaAtendimento programa = buildProgramaAtendimentoUseCase.execute(
                            programaId, patientId, payload, correlationIdText);
                    programaAtendimentoPort.save(programa);

                    saveProgramaTreeUseCase.saveProgramasSemana(programaId, payload.programasSemana(), correlationIdText);
                    saveProgramaTreeUseCase.saveProgramasEscola(programaId, payload.programasEscola(), correlationIdText);
                    saveNucleoPatients(payload.nucleoPatient(), patientId, correlationId);
                    saveAbordagensUseCase.execute(payload.nucleoPatient());

                    List<ProgramaCommandDTO> commandPayload = buildProgramaCommandsUseCase.execute(
                            payload.nucleoPatient());
                    createOutboxCommandUseCase.execute(
                            context.envelop(), pending.getEventId(), programaId, commandPayload);

                    return new ProgramaAtendimentoCreateResponseDTO(
                            "Programa de Atendimento Criado com Sucesso para Paciente " + patientId,
                            patientId, correlationId);
                });
    }

    private void saveNucleoPatients(
            List<NucleoPatientDTO> nucleoPatients, UUID patientId, UUID correlationId) {
        for (NucleoPatientDTO np : nucleoPatients) {
            if (!nucleoPatientPort.existsById(np.nucleoPatientId())) {
                acolhimentoInboundService.createNucleoPatient(
                        np.nucleoPatientId(), patientId, np.nucleoId(),
                        np.nucleoPatientResponsavel(), correlationId);
            }
        }
    }
}
