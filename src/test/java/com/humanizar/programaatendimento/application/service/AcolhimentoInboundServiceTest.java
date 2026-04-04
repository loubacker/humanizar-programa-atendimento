package com.humanizar.programaatendimento.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class AcolhimentoInboundServiceTest {

    @Mock
    private NucleoPatientPort nucleoPatientPort;

    @Mock
    private NucleoPatientResponsavelPort responsavelPort;

    @Mock
    private AbordagemPatientPort abordagemPatientPort;

    @Captor
    private ArgumentCaptor<List<NucleoPatientResponsavel>> responsaveisCaptor;

    private AcolhimentoInboundService service;

    @BeforeEach
    void setUp() {
        service = new AcolhimentoInboundService(nucleoPatientPort, responsavelPort, abordagemPatientPort);
    }

    @Test
    void shouldCreateNucleoPatientWithResponsaveis() {
        UUID patientId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();

        when(nucleoPatientPort.existsById(nucleoPatientId)).thenReturn(false);
        when(nucleoPatientPort.findByPatientIdAndNucleoId(patientId, nucleoId)).thenReturn(Optional.empty());
        when(nucleoPatientPort.save(any())).thenReturn(NucleoPatient.builder()
                .id(nucleoPatientId)
                .patientId(patientId)
                .nucleoId(nucleoId)
                .build());

        service.createNucleoPatient(
                nucleoPatientId,
                patientId,
                nucleoId,
                List.of(new NucleoResponsavelDTO(responsavelId, "COORDENADOR")),
                UUID.randomUUID());

        verify(responsavelPort).saveAll(responsaveisCaptor.capture());
        List<NucleoPatientResponsavel> saved = responsaveisCaptor.getValue();
        assertEquals(1, saved.size());
        assertEquals(responsavelId, saved.getFirst().getResponsavelId());
        assertEquals(ResponsavelRole.COORDENADOR, saved.getFirst().getRole());
    }

    @Test
    void shouldUpdateExistingResponsavelRoleWithoutDeletingEverything() {
        UUID patientId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();
        UUID responsavelEntityId = UUID.randomUUID();

        when(nucleoPatientPort.findAllByPatientId(patientId)).thenReturn(List.of(
                NucleoPatient.builder()
                        .id(nucleoPatientId)
                        .patientId(patientId)
                        .nucleoId(nucleoId)
                        .build()));
        when(responsavelPort.findByNucleoPatientId(nucleoPatientId)).thenReturn(List.of(
                new NucleoPatientResponsavel(
                        responsavelEntityId,
                        nucleoPatientId,
                        responsavelId,
                        ResponsavelRole.COORDENADOR)));

        service.applyNucleoPatientSnapshot(
                patientId,
                List.of(new AcolhimentoNucleoPatientDTO(
                        nucleoPatientId,
                        nucleoId,
                        List.of(new NucleoResponsavelDTO(responsavelId, "ADMINISTRADOR")))),
                UUID.randomUUID());

        verify(responsavelPort).save(new NucleoPatientResponsavel(
                responsavelEntityId,
                nucleoPatientId,
                responsavelId,
                ResponsavelRole.ADMINISTRADOR));
        verify(responsavelPort, never()).deleteByNucleoPatientId(nucleoPatientId);
        verify(responsavelPort, never()).saveAll(any());
    }

    @Test
    void shouldDeleteRemovedResponsavelById() {
        UUID patientId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();
        UUID removedResponsavelEntityId = UUID.randomUUID();
        UUID removedResponsavelId = UUID.randomUUID();

        when(nucleoPatientPort.findAllByPatientId(patientId)).thenReturn(List.of(
                NucleoPatient.builder()
                        .id(nucleoPatientId)
                        .patientId(patientId)
                        .nucleoId(nucleoId)
                        .build()));
        when(responsavelPort.findByNucleoPatientId(nucleoPatientId)).thenReturn(List.of(
                new NucleoPatientResponsavel(
                        removedResponsavelEntityId,
                        nucleoPatientId,
                        removedResponsavelId,
                        ResponsavelRole.COORDENADOR)));

        service.applyNucleoPatientSnapshot(
                patientId,
                List.of(new AcolhimentoNucleoPatientDTO(
                        nucleoPatientId,
                        nucleoId,
                        List.of())),
                UUID.randomUUID());

        verify(responsavelPort).deleteById(removedResponsavelEntityId);
    }

    @Test
    void shouldDeleteRemovedNucleoCleaningAbordagensAndResponsaveis() {
        UUID patientId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();
        UUID incomingNucleoPatientId = UUID.randomUUID();
        UUID incomingNucleoId = UUID.randomUUID();

        when(nucleoPatientPort.findAllByPatientId(patientId)).thenReturn(List.of(
                NucleoPatient.builder()
                        .id(nucleoPatientId)
                        .patientId(patientId)
                        .nucleoId(nucleoId)
                        .build()));
        when(nucleoPatientPort.existsById(incomingNucleoPatientId)).thenReturn(false);
        when(nucleoPatientPort.findByPatientIdAndNucleoId(patientId, incomingNucleoId)).thenReturn(Optional.empty());
        when(nucleoPatientPort.save(any())).thenReturn(NucleoPatient.builder()
                .id(incomingNucleoPatientId)
                .patientId(patientId)
                .nucleoId(incomingNucleoId)
                .build());

        service.applyNucleoPatientSnapshot(
                patientId,
                List.of(new AcolhimentoNucleoPatientDTO(
                        incomingNucleoPatientId,
                        incomingNucleoId,
                        List.of(new NucleoResponsavelDTO(UUID.randomUUID(), "COORDENADOR")))),
                UUID.randomUUID());

        verify(abordagemPatientPort).deleteByNucleoPatientId(nucleoPatientId);
        verify(responsavelPort).deleteByNucleoPatientId(nucleoPatientId);
        verify(nucleoPatientPort).deleteByPatientIdAndNucleoId(patientId, nucleoId);
    }

    @Test
    void shouldFailWhenIncomingNucleoIdDivergesFromExistingRecord() {
        UUID patientId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID existingNucleoId = UUID.randomUUID();
        UUID incomingNucleoId = UUID.randomUUID();

        when(nucleoPatientPort.findAllByPatientId(patientId)).thenReturn(List.of(
                NucleoPatient.builder()
                        .id(nucleoPatientId)
                        .patientId(patientId)
                        .nucleoId(existingNucleoId)
                        .build()));

        ProgramaAtendimentoException ex = assertThrows(
                ProgramaAtendimentoException.class,
                () -> service.applyNucleoPatientSnapshot(
                        patientId,
                        List.of(new AcolhimentoNucleoPatientDTO(
                                nucleoPatientId,
                                incomingNucleoId,
                                List.of(new NucleoResponsavelDTO(UUID.randomUUID(), "COORDENADOR")))),
                        UUID.randomUUID()));

        assertEquals(ReasonCode.VALIDATION_ERROR, ex.getReasonCode());
        verify(responsavelPort, never()).save(any());
    }
}
