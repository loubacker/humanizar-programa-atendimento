package com.humanizar.programaatendimento.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.humanizar.programaatendimento.application.inbound.dto.InboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.AbordagemPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoResponsavelDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaSemanaDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaSemanaScheduleDTO;
import com.humanizar.programaatendimento.domain.model.enums.ResponsavelRole;
import com.humanizar.programaatendimento.domain.model.enums.Semana;
import com.humanizar.programaatendimento.domain.model.enums.SimNao;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.nucleo.AbordagemPatientEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.nucleo.NucleoPatientEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.nucleo.NucleoPatientResponsavelEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaAtendimentoEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaSemanaEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.ProgramaSemanaScheduleEntity;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.nucleo.AbordagemPatientRepository;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.nucleo.NucleoPatientRepository;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.nucleo.NucleoPatientResponsavelRepository;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.ProgramaAtendimentoRepository;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.ProgramaSemanaRepository;
import com.humanizar.programaatendimento.infrastructure.persistence.repository.programa.ProgramaSemanaScheduleRepository;

@SpringBootTest
@Transactional
class ProgramaUpdateServiceIntegrationTest {

    @Autowired
    private ProgramaUpdateService service;
    @Autowired
    private ProgramaAtendimentoRepository programaAtendimentoRepository;
    @Autowired
    private NucleoPatientRepository nucleoPatientRepository;
    @Autowired
    private NucleoPatientResponsavelRepository nucleoPatientResponsavelRepository;
    @Autowired
    private AbordagemPatientRepository abordagemPatientRepository;
    @Autowired
    private ProgramaSemanaRepository programaSemanaRepository;
    @Autowired
    private ProgramaSemanaScheduleRepository programaSemanaScheduleRepository;

    @Test
    void shouldUpdateWithoutDuplicatingExistingAbordagemPair() {
        UUID patientId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();
        UUID abordagemId = UUID.randomUUID();

        seedPrograma(patientId);
        seedNucleo(patientId, nucleoPatientId, nucleoId);
        seedResponsavel(nucleoPatientId, responsavelId, ResponsavelRole.COORDENADOR);
        seedAbordagem(nucleoPatientId, abordagemId);

        ProgramaAtendimentoDTO payload = createPayload(
                patientId,
                "2026-04-02T23:24:43",
                List.of(),
                List.of(new NucleoPatientDTO(
                        nucleoPatientId,
                        patientId,
                        nucleoId,
                        List.of(new NucleoResponsavelDTO(responsavelId, "COORDENADOR")),
                        List.of(new AbordagemPatientDTO(nucleoPatientId, abordagemId)))));

        assertDoesNotThrow(() -> service.updateByPatientId(patientId, createEnvelope(correlationId, payload)));
        assertEquals(1, abordagemPatientRepository.findByNucleoPatientId(nucleoPatientId).size());
        assertEquals(abordagemId, abordagemPatientRepository.findByNucleoPatientId(nucleoPatientId).getFirst().getAbordagemId());
    }

    @Test
    void shouldRemoveAbordagensAndResponsaveisWhenNucleoLeavesSnapshot() {
        UUID patientId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        UUID removedNucleoPatientId = UUID.randomUUID();
        UUID keptNucleoPatientId = UUID.randomUUID();
        UUID removedNucleoId = UUID.randomUUID();
        UUID keptNucleoId = UUID.randomUUID();
        UUID keptResponsavelId = UUID.randomUUID();

        seedPrograma(patientId);
        seedNucleo(patientId, removedNucleoPatientId, removedNucleoId);
        seedResponsavel(removedNucleoPatientId, UUID.randomUUID(), ResponsavelRole.COORDENADOR);
        seedAbordagem(removedNucleoPatientId, UUID.randomUUID());
        seedNucleo(patientId, keptNucleoPatientId, keptNucleoId);
        seedResponsavel(keptNucleoPatientId, keptResponsavelId, ResponsavelRole.COORDENADOR);

        ProgramaAtendimentoDTO payload = createPayload(
                patientId,
                "2026-04-02T23:24:43",
                List.of(),
                List.of(new NucleoPatientDTO(
                        keptNucleoPatientId,
                        patientId,
                        keptNucleoId,
                        List.of(new NucleoResponsavelDTO(keptResponsavelId, "COORDENADOR")),
                        List.of())));

        service.updateByPatientId(patientId, createEnvelope(correlationId, payload));

        assertEquals(1, nucleoPatientRepository.findAllByPatientId(patientId).size());
        assertEquals(0, abordagemPatientRepository.findByNucleoPatientId(removedNucleoPatientId).size());
        assertEquals(0, nucleoPatientResponsavelRepository.findByNucleoPatientId(removedNucleoPatientId).size());
    }

    @Test
    void shouldPreserveOtherProgramaSemanaRowsWhenUpdatingOneDay() {
        UUID patientId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();
        UUID segundaScheduleNucleo = UUID.randomUUID();
        UUID tercaScheduleNucleo = UUID.randomUUID();

        ProgramaAtendimentoEntity programa = seedPrograma(patientId);
        seedNucleo(patientId, nucleoPatientId, nucleoId);
        seedResponsavel(nucleoPatientId, responsavelId, ResponsavelRole.COORDENADOR);

        ProgramaSemanaEntity segunda = programaSemanaRepository.save(new ProgramaSemanaEntity(
                null,
                programa.getId(),
                Semana.SEGUNDA));
        ProgramaSemanaEntity terca = programaSemanaRepository.save(new ProgramaSemanaEntity(
                null,
                programa.getId(),
                Semana.TERCA));
        programaSemanaScheduleRepository.save(new ProgramaSemanaScheduleEntity(
                null,
                segunda.getId(),
                segundaScheduleNucleo,
                "08:00",
                "09:00",
                "MANHA"));
        programaSemanaScheduleRepository.save(new ProgramaSemanaScheduleEntity(
                null,
                terca.getId(),
                tercaScheduleNucleo,
                "10:00",
                "11:00",
                "MANHA"));

        ProgramaAtendimentoDTO payload = createPayload(
                patientId,
                "2026-04-02T23:24:43",
                List.of(
                        new ProgramaSemanaDTO(
                                "SEGUNDA",
                                List.of(new ProgramaSemanaScheduleDTO(
                                        segundaScheduleNucleo,
                                        "09:30",
                                        "10:30",
                                        "TARDE"))),
                        new ProgramaSemanaDTO(
                                "TERCA",
                                List.of(new ProgramaSemanaScheduleDTO(
                                        tercaScheduleNucleo,
                                        "10:00",
                                        "11:00",
                                        "MANHA")))),
                List.of(new NucleoPatientDTO(
                        nucleoPatientId,
                        patientId,
                        nucleoId,
                        List.of(new NucleoResponsavelDTO(responsavelId, "COORDENADOR")),
                        List.of())));

        service.updateByPatientId(patientId, createEnvelope(correlationId, payload));

        List<ProgramaSemanaEntity> semanas = programaSemanaRepository.findByProgramaAtendimentoId(programa.getId());
        assertEquals(2, semanas.size());
        assertEquals(segunda.getId(),
                semanas.stream().filter(s -> s.getDiaSemana() == Semana.SEGUNDA).findFirst().orElseThrow().getId());
        assertEquals(terca.getId(),
                semanas.stream().filter(s -> s.getDiaSemana() == Semana.TERCA).findFirst().orElseThrow().getId());
        assertEquals("09:30",
                programaSemanaScheduleRepository.findByProgramaSemanaId(segunda.getId()).getFirst().getHorarioInicio());
        assertEquals(1, programaSemanaScheduleRepository.findByProgramaSemanaId(terca.getId()).size());
    }

    private ProgramaAtendimentoEntity seedPrograma(UUID patientId) {
        ProgramaAtendimentoEntity entity = new ProgramaAtendimentoEntity();
        entity.setPatientId(patientId);
        entity.setDataInicio(LocalDateTime.parse("2026-04-01T08:00:00"));
        entity.setCadastroApp(SimNao.SIM);
        entity.setAtEscolar(SimNao.NAO);
        entity.setObservacao("obs");
        return programaAtendimentoRepository.save(entity);
    }

    private void seedNucleo(UUID patientId, UUID nucleoPatientId, UUID nucleoId) {
        nucleoPatientRepository.save(new NucleoPatientEntity(
                nucleoPatientId,
                patientId,
                nucleoId));
    }

    private void seedResponsavel(UUID nucleoPatientId, UUID responsavelId, ResponsavelRole role) {
        nucleoPatientResponsavelRepository.save(new NucleoPatientResponsavelEntity(
                null,
                nucleoPatientId,
                responsavelId,
                role));
    }

    private void seedAbordagem(UUID nucleoPatientId, UUID abordagemId) {
        abordagemPatientRepository.save(new AbordagemPatientEntity(
                null,
                nucleoPatientId,
                abordagemId));
    }

    private InboundEnvelopeDTO<ProgramaAtendimentoDTO> createEnvelope(
            UUID correlationId,
            ProgramaAtendimentoDTO payload) {
        return new InboundEnvelopeDTO<>(
                correlationId,
                "humanizar-acolhimento",
                LocalDateTime.now(),
                UUID.randomUUID(),
                "JUnit",
                "127.0.0.1",
                payload);
    }

    private ProgramaAtendimentoDTO createPayload(
            UUID patientId,
            String dataInicio,
            List<ProgramaSemanaDTO> programasSemana,
            List<NucleoPatientDTO> nucleos) {
        return new ProgramaAtendimentoDTO(
                patientId,
                dataInicio,
                SimNao.SIM,
                SimNao.NAO,
                "obs",
                programasSemana,
                List.of(),
                nucleos);
    }
}
