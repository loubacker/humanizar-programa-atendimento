package com.humanizar.programaatendimento.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.humanizar.programaatendimento.application.inbound.dto.messaging.AcolhimentoNucleoPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoResponsavelDTO;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.enums.ResponsavelRole;
import com.humanizar.programaatendimento.domain.model.nucleo.NucleoPatient;
import com.humanizar.programaatendimento.domain.model.nucleo.NucleoPatientResponsavel;
import com.humanizar.programaatendimento.domain.port.nucleo.AbordagemPatientPort;
import com.humanizar.programaatendimento.domain.port.nucleo.NucleoPatientPort;
import com.humanizar.programaatendimento.domain.port.nucleo.NucleoPatientResponsavelPort;

@Service
public class AcolhimentoInboundService {

    private static final Logger log = LoggerFactory.getLogger(AcolhimentoInboundService.class);

    private final NucleoPatientPort nucleoPatientPort;
    private final NucleoPatientResponsavelPort responsavelPort;
    private final AbordagemPatientPort abordagemPatientPort;

    public AcolhimentoInboundService(NucleoPatientPort nucleoPatientPort,
            NucleoPatientResponsavelPort responsavelPort,
            AbordagemPatientPort abordagemPatientPort) {
        this.nucleoPatientPort = nucleoPatientPort;
        this.responsavelPort = responsavelPort;
        this.abordagemPatientPort = abordagemPatientPort;
    }

    @Transactional
    public void createNucleoPatient(UUID nucleoPatientId, UUID patientId, UUID nucleoId,
            List<NucleoResponsavelDTO> responsaveis,
            UUID correlationId) {
        String corrId = correlationId != null ? correlationId.toString() : null;

        if (nucleoPatientId == null) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.VALIDATION_ERROR, corrId, "nucleoPatientId e obrigatorio");
        }

        if (responsaveis == null || responsaveis.isEmpty()) {
            throw new ProgramaAtendimentoException(ReasonCode.RESPONSAVEL_REQUIRED, corrId);
        }

        if (nucleoPatientPort.existsById(nucleoPatientId)) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.VALIDATION_ERROR, corrId,
                    "nucleoPatientId ja existe: " + nucleoPatientId);
        }

        Optional<NucleoPatient> existingByPatientAndNucleo = nucleoPatientPort
                .findByPatientIdAndNucleoId(patientId, nucleoId);
        if (existingByPatientAndNucleo.isPresent()
                && !nucleoPatientId.equals(existingByPatientAndNucleo.get().getId())) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.VALIDATION_ERROR, corrId,
                    "Conflito de identidade para patientId/nucleoId. expectedNucleoPatientId="
                            + existingByPatientAndNucleo.get().getId()
                            + ", received=" + nucleoPatientId);
        }

        NucleoPatient nucleo = NucleoPatient.builder()
                .id(nucleoPatientId)
                .patientId(patientId)
                .nucleoId(nucleoId)
                .build();
        NucleoPatient saved = nucleoPatientPort.save(nucleo);

        List<NucleoPatientResponsavel> responsaveisDomain = toResponsavelDomain(
                saved.getId(),
                responsaveis,
                corrId);
        responsavelPort.saveAll(responsaveisDomain);

        log.info("NucleoPatient criado. id={}, patientId={}, nucleoId={}, responsaveis={}",
                saved.getId(), patientId, nucleoId, responsaveisDomain.size());
    }

    @Transactional
    public void applyNucleoPatientSnapshot(UUID patientId,
            List<AcolhimentoNucleoPatientDTO> incomingNucleoCommands,
            UUID correlationId) {
        String corrId = correlationId != null ? correlationId.toString() : null;
        if (incomingNucleoCommands == null || incomingNucleoCommands.isEmpty()) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.VALIDATION_ERROR, corrId, "nucleoPatient e obrigatorio");
        }

        List<NucleoPatient> currentNucleos = nucleoPatientPort.findAllByPatientId(patientId);
        Set<UUID> incomingNucleoPatientIds = incomingNucleoCommands.stream()
                .map(AcolhimentoNucleoPatientDTO::nucleoPatientId)
                .collect(Collectors.toSet());
        Map<UUID, NucleoPatient> currentByNucleoPatientId = currentNucleos.stream()
                .collect(Collectors.toMap(NucleoPatient::getId, n -> n));

        for (NucleoPatient current : currentNucleos) {
            if (!incomingNucleoPatientIds.contains(current.getId())) {
                deleteNucleo(current, patientId, corrId);
            }
        }

        for (AcolhimentoNucleoPatientDTO incomingNucleoCommand : incomingNucleoCommands) {
            NucleoPatient existing = currentByNucleoPatientId.get(incomingNucleoCommand.nucleoPatientId());
            if (existing == null) {
                if (nucleoPatientPort.existsById(incomingNucleoCommand.nucleoPatientId())) {
                    throw new ProgramaAtendimentoException(
                            ReasonCode.VALIDATION_ERROR, corrId,
                            "nucleoPatientId pertence a outro paciente: "
                                    + incomingNucleoCommand.nucleoPatientId());
                }

                createNucleoPatient(incomingNucleoCommand.nucleoPatientId(), patientId,
                        incomingNucleoCommand.nucleoId(),
                        incomingNucleoCommand.nucleoPatientResponsavel(), correlationId);
            } else {
                if (!existing.getNucleoId().equals(incomingNucleoCommand.nucleoId())) {
                    throw new ProgramaAtendimentoException(
                            ReasonCode.VALIDATION_ERROR, corrId,
                            "nucleoId divergente para nucleoPatientId="
                                    + incomingNucleoCommand.nucleoPatientId()
                                    + ". expected=" + existing.getNucleoId()
                                    + ", received="
                                    + incomingNucleoCommand.nucleoId());
                }
                reconcileResponsaveis(existing, incomingNucleoCommand.nucleoPatientResponsavel(),
                        correlationId);
            }
        }
    }

    @Transactional
    public void deleteAllNucleosByPatientId(UUID patientId, UUID correlationId) {
        String corrId = correlationId != null ? correlationId.toString() : null;
        List<NucleoPatient> nucleos = nucleoPatientPort.findAllByPatientId(patientId);

        for (NucleoPatient nucleo : nucleos) {
            deleteNucleo(nucleo, patientId, corrId);
        }

        log.info("Todos nucleos removidos para patientId={}. total={}", patientId, nucleos.size());
    }

    private void deleteNucleo(NucleoPatient nucleo, UUID patientId, String corrId) {
        if (!abordagemPatientPort.findByNucleoPatientId(nucleo.getId()).isEmpty()) {
            throw new ProgramaAtendimentoException(ReasonCode.HAS_ABORDAGEM, corrId);
        }

        responsavelPort.deleteByNucleoPatientId(nucleo.getId());
        nucleoPatientPort.deleteByPatientIdAndNucleoId(patientId, nucleo.getNucleoId());
    }

    private void reconcileResponsaveis(NucleoPatient existing,
            List<NucleoResponsavelDTO> incomingResponsaveis,
            UUID correlationId) {
        List<NucleoPatientResponsavel> currentResponsaveis = responsavelPort
                .findByNucleoPatientId(existing.getId());

        Set<UUID> currentIds = currentResponsaveis.stream()
                .map(NucleoPatientResponsavel::getResponsavelId)
                .collect(Collectors.toSet());
        Set<UUID> incomingIds = incomingResponsaveis.stream()
                .map(NucleoResponsavelDTO::responsavelId)
                .collect(Collectors.toSet());

        String corrId = correlationId != null ? correlationId.toString() : null;

        List<NucleoPatientResponsavel> added = incomingResponsaveis.stream()
                .filter(responsavelCommand -> !currentIds.contains(responsavelCommand.responsavelId()))
                .map(responsavelCommand -> new NucleoPatientResponsavel(
                        null, existing.getId(), responsavelCommand.responsavelId(),
                        parseRole(responsavelCommand.role(), corrId)))
                .toList();

        List<NucleoPatientResponsavel> removed = currentResponsaveis.stream()
                .filter(r -> !incomingIds.contains(r.getResponsavelId()))
                .toList();

        if (!added.isEmpty() || !removed.isEmpty()) {
            // Regrava o estado final para garantir consistencia quando houver add/remove no
            // mesmo update.
            responsavelPort.deleteByNucleoPatientId(existing.getId());
            List<NucleoPatientResponsavel> finalState = toResponsavelDomain(
                    existing.getId(),
                    incomingResponsaveis,
                    corrId);
            responsavelPort.saveAll(finalState);
        }
    }

    private List<NucleoPatientResponsavel> toResponsavelDomain(
            UUID nucleoPatientId,
            List<NucleoResponsavelDTO> responsaveis,
            String correlationId) {
        return responsaveis.stream()
                .map(responsavelCommand -> new NucleoPatientResponsavel(
                        null,
                        nucleoPatientId,
                        responsavelCommand.responsavelId(),
                        parseRole(responsavelCommand.role(), correlationId)))
                .toList();
    }

    private ResponsavelRole parseRole(String role, String correlationId) {
        try {
            return ResponsavelRole.valueOf(role);
        } catch (IllegalArgumentException ex) {
            throw new ProgramaAtendimentoException(
                    ReasonCode.VALIDATION_ERROR, correlationId,
                    "Role invalida: " + role);
        }
    }
}
