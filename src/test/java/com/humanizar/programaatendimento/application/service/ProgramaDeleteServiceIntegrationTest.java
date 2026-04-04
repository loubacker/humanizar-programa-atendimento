package com.humanizar.programaatendimento.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.humanizar.programaatendimento.application.catalog.TargetCatalog;
import com.humanizar.programaatendimento.domain.model.enums.OperationType;
import com.humanizar.programaatendimento.domain.model.enums.ResponsavelRole;
import com.humanizar.programaatendimento.domain.model.enums.Semana;
import com.humanizar.programaatendimento.domain.model.enums.SimNao;
import com.humanizar.programaatendimento.domain.model.enums.Status;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.nucleo.AbordagemPatientEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.nucleo.NucleoPatientEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.nucleo.NucleoPatientResponsavelEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.pending.PendingProgramaEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.AtEscolaSemanaEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.AtEscolaSemanaScheduleEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaAtendimentoEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaEscolaEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaSemanaEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaSemanaScheduleEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.nucleo.AbordagemPatientRepository;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.nucleo.NucleoPatientRepository;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.nucleo.NucleoPatientResponsavelRepository;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.pending.PendingProgramaRepository;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.pending.PendingTargetStatusRepository;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.AtEscolaSemanaRepository;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.AtEscolaSemanaScheduleRepository;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.ProgramaAtendimentoRepository;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.ProgramaEscolaRepository;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.ProgramaSemanaRepository;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.ProgramaSemanaScheduleRepository;

@SpringBootTest
class ProgramaDeleteServiceIntegrationTest {

    @Autowired
    private ProgramaDeleteService service;
    @Autowired
    private ProgramaAtendimentoRepository programaAtendimentoRepository;
    @Autowired
    private ProgramaSemanaRepository programaSemanaRepository;
    @Autowired
    private ProgramaSemanaScheduleRepository programaSemanaScheduleRepository;
    @Autowired
    private ProgramaEscolaRepository programaEscolaRepository;
    @Autowired
    private AtEscolaSemanaRepository atEscolaSemanaRepository;
    @Autowired
    private AtEscolaSemanaScheduleRepository atEscolaSemanaScheduleRepository;
    @Autowired
    private NucleoPatientRepository nucleoPatientRepository;
    @Autowired
    private NucleoPatientResponsavelRepository nucleoPatientResponsavelRepository;
    @Autowired
    private AbordagemPatientRepository abordagemPatientRepository;
    @Autowired
    private PendingProgramaRepository pendingProgramaRepository;
    @Autowired
    private PendingTargetStatusRepository pendingTargetStatusRepository;

    private UUID eventId;
    private UUID patientId;
    private UUID programaId;

    @AfterEach
    void tearDown() {
        if (eventId != null) {
            pendingTargetStatusRepository.deleteByEventId(eventId);
            pendingProgramaRepository.deleteById(eventId);
        }

        if (programaId != null) {
            programaSemanaScheduleRepository.findAll().stream()
                    .filter(item -> programaSemanaRepository.findByProgramaAtendimentoId(programaId).stream()
                            .anyMatch(semana -> semana.getId().equals(item.getProgramaSemanaId())))
                    .map(ProgramaSemanaScheduleEntity::getId)
                    .toList()
                    .forEach(programaSemanaScheduleRepository::deleteById);

            programaSemanaRepository.findByProgramaAtendimentoId(programaId).stream()
                    .map(ProgramaSemanaEntity::getId)
                    .toList()
                    .forEach(programaSemanaRepository::deleteById);

            programaEscolaRepository.findByProgramaAtendimentoId(programaId).forEach(escola -> {
                atEscolaSemanaRepository.findByProgramaEscolaId(escola.getId()).forEach(atSemana -> {
                    atEscolaSemanaScheduleRepository.findByAtEscolaSemanaId(atSemana.getId()).stream()
                            .map(AtEscolaSemanaScheduleEntity::getId)
                            .toList()
                            .forEach(atEscolaSemanaScheduleRepository::deleteById);
                    atEscolaSemanaRepository.deleteById(atSemana.getId());
                });
                programaEscolaRepository.deleteById(escola.getId());
            });

            programaAtendimentoRepository.deleteById(programaId);
        }

        if (patientId != null) {
            nucleoPatientRepository.findAllByPatientId(patientId).forEach(nucleo -> {
                abordagemPatientRepository.findByNucleoPatientId(nucleo.getId()).stream()
                        .map(AbordagemPatientEntity::getId)
                        .toList()
                        .forEach(abordagemPatientRepository::deleteById);
                nucleoPatientResponsavelRepository.findByNucleoPatientId(nucleo.getId()).stream()
                        .map(NucleoPatientResponsavelEntity::getId)
                        .toList()
                        .forEach(nucleoPatientResponsavelRepository::deleteById);
                nucleoPatientRepository.deleteById(nucleo.getId());
            });
        }
    }

    @Test
    void shouldDeleteLocalAggregateAfterProcessedCallbackWhenPendingIsSuccess() {
        UUID correlationId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();

        patientId = UUID.randomUUID();
        eventId = UUID.randomUUID();

        ProgramaAtendimentoEntity programa = seedPrograma(patientId);
        programaId = programa.getId();
        seedProgramaSemana(programaId);
        seedProgramaEscola(programaId);
        seedNucleo(patientId, nucleoPatientId, nucleoId);
        seedResponsavel(nucleoPatientId);
        seedAbordagem(nucleoPatientId);
        seedPendingDelete(eventId, correlationId, patientId, programaId);

        service.processDeletePosCallback(
                eventId,
                TargetCatalog.TARGET_NUCLEO_RELACIONAMENTO,
                "PROCESSED");

        assertEquals(0, programaAtendimentoRepository.findByPatientId(patientId).stream().count());
        assertEquals(0, programaSemanaRepository.findByProgramaAtendimentoId(programaId).size());
        assertEquals(0, programaEscolaRepository.findByProgramaAtendimentoId(programaId).size());
        assertEquals(0, nucleoPatientRepository.findAllByPatientId(patientId).size());

        PendingProgramaEntity pending = pendingProgramaRepository.findByEventId(eventId).orElseThrow();
        assertEquals(Status.SUCCESS, pending.getStatus());
        assertEquals(null, pending.getErrorMessage());
    }

    private ProgramaAtendimentoEntity seedPrograma(UUID patientId) {
        ProgramaAtendimentoEntity entity = new ProgramaAtendimentoEntity();
        entity.setPatientId(patientId);
        entity.setDataInicio(LocalDateTime.parse("2026-04-04T09:00:00"));
        entity.setCadastroApp(SimNao.SIM);
        entity.setAtEscolar(SimNao.NAO);
        entity.setObservacao("delete-test");
        return programaAtendimentoRepository.save(entity);
    }

    private void seedProgramaSemana(UUID programaAtendimentoId) {
        ProgramaSemanaEntity semana = programaSemanaRepository.save(
                new ProgramaSemanaEntity(null, programaAtendimentoId, Semana.SEGUNDA));
        programaSemanaScheduleRepository.save(new ProgramaSemanaScheduleEntity(
                null,
                semana.getId(),
                UUID.randomUUID(),
                "08:00",
                "09:00",
                "MANHA"));
    }

    private void seedProgramaEscola(UUID programaAtendimentoId) {
        ProgramaEscolaEntity escola = programaEscolaRepository.save(new ProgramaEscolaEntity(
                null,
                programaAtendimentoId,
                "Profissional",
                "Escola"));
        AtEscolaSemanaEntity atSemana = atEscolaSemanaRepository.save(new AtEscolaSemanaEntity(
                null,
                escola.getId(),
                Semana.TERCA));
        atEscolaSemanaScheduleRepository.save(new AtEscolaSemanaScheduleEntity(
                null,
                atSemana.getId(),
                UUID.randomUUID(),
                "10:00",
                "11:00",
                "MANHA"));
    }

    private void seedNucleo(UUID patientId, UUID nucleoPatientId, UUID nucleoId) {
        nucleoPatientRepository.save(new NucleoPatientEntity(
                nucleoPatientId,
                patientId,
                nucleoId));
    }

    private void seedResponsavel(UUID nucleoPatientId) {
        nucleoPatientResponsavelRepository.save(new NucleoPatientResponsavelEntity(
                null,
                nucleoPatientId,
                UUID.randomUUID(),
                ResponsavelRole.COORDENADOR));
    }

    private void seedAbordagem(UUID nucleoPatientId) {
        abordagemPatientRepository.save(new AbordagemPatientEntity(
                null,
                nucleoPatientId,
                UUID.randomUUID()));
    }

    private void seedPendingDelete(UUID eventId, UUID correlationId, UUID patientId, UUID programaId) {
        pendingProgramaRepository.save(new PendingProgramaEntity(
                eventId,
                correlationId,
                patientId,
                programaId,
                OperationType.DELETE,
                "{\"snapshot\":true}",
                LocalDateTime.now(),
                Status.SUCCESS,
                null));
    }
}
