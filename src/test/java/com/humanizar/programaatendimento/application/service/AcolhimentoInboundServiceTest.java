package com.humanizar.programaatendimento.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.humanizar.programaatendimento.domain.model.nucleo.AbordagemPatient;
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
                UUID.randomUUID()
        );

        verify(responsavelPort).saveAll(responsaveisCaptor.capture());
        List<NucleoPatientResponsavel> saved = responsaveisCaptor.getValue();
        assertEquals(1, saved.size());
        assertEquals(responsavelId, saved.getFirst().getResponsavelId());
        assertEquals(ResponsavelRole.COORDENADOR, saved.getFirst().getRole());
    }

    @Test
    void shouldReconcileSnapshotDeletingAndCreatingNucleos() {
        UUID patientId = UUID.randomUUID();
        UUID currentNucleoPatientId = UUID.randomUUID();
        UUID currentNucleoId = UUID.randomUUID();
        UUID incomingNucleoPatientId = UUID.randomUUID();
        UUID incomingNucleoId = UUID.randomUUID();

        when(nucleoPatientPort.findAllByPatientId(patientId)).thenReturn(List.of(
                NucleoPatient.builder()
                        .id(currentNucleoPatientId)
                        .patientId(patientId)
                        .nucleoId(currentNucleoId)
                        .build()));
        when(abordagemPatientPort.findByNucleoPatientId(currentNucleoPatientId)).thenReturn(List.of());
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
                UUID.randomUUID()
        );

        verify(nucleoPatientPort).deleteByPatientIdAndNucleoId(patientId, currentNucleoId);
        verify(nucleoPatientPort).save(any());
        verify(responsavelPort).saveAll(any());
    }

    @Test
    void shouldFailDeleteWhenNucleoHasAbordagem() {
        UUID patientId = UUID.randomUUID();
        UUID nucleoPatientId = UUID.randomUUID();
        UUID nucleoId = UUID.randomUUID();

        when(nucleoPatientPort.findAllByPatientId(patientId)).thenReturn(List.of(
                NucleoPatient.builder()
                        .id(nucleoPatientId)
                        .patientId(patientId)
                        .nucleoId(nucleoId)
                        .build()));
        when(abordagemPatientPort.findByNucleoPatientId(nucleoPatientId)).thenReturn(List.of(
                new AbordagemPatient(UUID.randomUUID(), nucleoPatientId, UUID.randomUUID())));

        ProgramaAtendimentoException ex = assertThrows(
                ProgramaAtendimentoException.class,
                () -> service.deleteAllNucleosByPatientId(
                        patientId,
                        UUID.randomUUID()
                ));

        assertEquals(ReasonCode.HAS_ABORDAGEM, ex.getReasonCode());
        verify(nucleoPatientPort).findAllByPatientId(patientId);
        verify(abordagemPatientPort).findByNucleoPatientId(eq(nucleoPatientId));
    }
}
