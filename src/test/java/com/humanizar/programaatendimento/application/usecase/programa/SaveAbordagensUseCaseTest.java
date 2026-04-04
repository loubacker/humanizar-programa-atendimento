package com.humanizar.programaatendimento.application.usecase.programa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.humanizar.programaatendimento.application.inbound.dto.nucleo.AbordagemPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoPatientDTO;
import com.humanizar.programaatendimento.domain.model.nucleo.AbordagemPatient;
import com.humanizar.programaatendimento.domain.port.nucleo.AbordagemPatientPort;

@ExtendWith(MockitoExtension.class)
class SaveAbordagensUseCaseTest {

    @Mock
    private AbordagemPatientPort abordagemPatientPort;

    @Captor
    private ArgumentCaptor<List<AbordagemPatient>> abordagensCaptor;

    private SaveAbordagensUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SaveAbordagensUseCase(abordagemPatientPort);
    }

    @Test
    void shouldSaveOnlyNewAbordagens() {
        UUID nucleoPatientId = UUID.randomUUID();
        UUID abordagemExistenteId = UUID.randomUUID();
        UUID abordagemNovaId = UUID.randomUUID();

        when(abordagemPatientPort.findByNucleoPatientId(nucleoPatientId)).thenReturn(List.of(
                new AbordagemPatient(UUID.randomUUID(), nucleoPatientId, abordagemExistenteId)));

        useCase.execute(List.of(new NucleoPatientDTO(
                nucleoPatientId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(),
                List.of(
                        new AbordagemPatientDTO(nucleoPatientId, abordagemExistenteId),
                        new AbordagemPatientDTO(nucleoPatientId, abordagemNovaId)))));

        verify(abordagemPatientPort).saveAll(abordagensCaptor.capture());
        AbordagemPatient abordagem = abordagensCaptor.getValue().getFirst();
        assertNull(abordagem.getId());
        assertEquals(nucleoPatientId, abordagem.getNucleoPatientId());
        assertEquals(abordagemNovaId, abordagem.getAbordagemId());
        verify(abordagemPatientPort, never()).deleteById(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldDeleteOnlyRemovedAbordagens() {
        UUID nucleoPatientId = UUID.randomUUID();
        UUID abordagemEntityId = UUID.randomUUID();
        UUID abordagemId = UUID.randomUUID();

        when(abordagemPatientPort.findByNucleoPatientId(nucleoPatientId)).thenReturn(List.of(
                new AbordagemPatient(abordagemEntityId, nucleoPatientId, abordagemId)));

        useCase.execute(List.of(new NucleoPatientDTO(
                nucleoPatientId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(),
                List.of())));

        verify(abordagemPatientPort).deleteById(abordagemEntityId);
        verify(abordagemPatientPort, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void shouldDoNothingWhenPayloadMatchesDatabase() {
        UUID nucleoPatientId = UUID.randomUUID();
        UUID abordagemEntityId = UUID.randomUUID();
        UUID abordagemId = UUID.randomUUID();

        when(abordagemPatientPort.findByNucleoPatientId(nucleoPatientId)).thenReturn(List.of(
                new AbordagemPatient(abordagemEntityId, nucleoPatientId, abordagemId)));

        useCase.execute(List.of(new NucleoPatientDTO(
                nucleoPatientId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(),
                List.of(new AbordagemPatientDTO(nucleoPatientId, abordagemId)))));

        verify(abordagemPatientPort).findByNucleoPatientId(nucleoPatientId);
        verifyNoMoreInteractions(abordagemPatientPort);
    }
}
