package com.humanizar.programaatendimento.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.http.ResponseEntity;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.outbound.dto.central.PendingCentralPageDTO;
import com.humanizar.programaatendimento.application.outbound.dto.central.PendingCentralSnapshotDTO;
import com.humanizar.programaatendimento.application.service.ProgramaRetrieveService;
import com.humanizar.programaatendimento.application.service.central.ProgramaCentralListService;
import com.humanizar.programaatendimento.application.service.central.ProgramaCentralSnapshotService;
import com.humanizar.programaatendimento.domain.exception.ProgramaAtendimentoException;
import com.humanizar.programaatendimento.domain.model.enums.ReasonCode;
import com.humanizar.programaatendimento.domain.model.enums.SimNao;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ProgramaRetrieveControllerRetryTest.TestConfig.class)
class ProgramaRetrieveControllerRetryTest {

    @Configuration(proxyBeanMethods = false)
    @EnableResilientMethods
    static class TestConfig {

        @Bean
        ProgramaRetrieveService programaRetrieveService() {
            return mock(ProgramaRetrieveService.class);
        }

        @Bean
        ProgramaCentralListService programaCentralListService() {
            return mock(ProgramaCentralListService.class);
        }

        @Bean
        ProgramaCentralSnapshotService programaCentralSnapshotService() {
            return mock(ProgramaCentralSnapshotService.class);
        }

        @Bean
        ProgramaRetrieveController programaRetrieveController(
                ProgramaRetrieveService retrieveService,
                ProgramaCentralListService centralListService,
                ProgramaCentralSnapshotService centralSnapshotService) {
            return new ProgramaRetrieveController(retrieveService, centralListService, centralSnapshotService);
        }
    }

    @Autowired
    private ProgramaRetrieveController controller;

    @Autowired
    private ProgramaRetrieveService retrieveService;

    @Autowired
    private ProgramaCentralListService centralListService;

    @Autowired
    private ProgramaCentralSnapshotService centralSnapshotService;

    @Test
    void shouldReturn200WithoutRetryWhenRetrieveSucceedsOnFirstAttempt() {
        UUID patientId = UUID.fromString("24ae0ca6-ad56-4daa-bd15-3188d766f13c");
        ProgramaAtendimentoDTO payload = new ProgramaAtendimentoDTO(
                patientId,
                "2026-04-02",
                SimNao.SIM,
                SimNao.NAO,
                "Payload de teste",
                List.of(),
                List.of(),
                List.of());

        when(retrieveService.findByPatientId(patientId)).thenReturn(payload);

        ResponseEntity<ProgramaAtendimentoDTO> response = controller.retrieve(patientId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(patientId, response.getBody().patientId());
        verify(retrieveService, times(1)).findByPatientId(patientId);
    }

    @Test
    void shouldRetryTransientFailureAndSucceedOnThirdAttemptForCentralList() {
        UUID patientId = UUID.fromString("3f110c55-bb15-4655-a38f-c474483a045d");
        PendingCentralPageDTO payload = new PendingCentralPageDTO(List.of(), 0, 10, 0, 0);

        when(centralListService.execute(patientId, 0, 10))
                .thenThrow(new TransientDataAccessResourceException("temporary failure 1"))
                .thenThrow(new TransientDataAccessResourceException("temporary failure 2"))
                .thenReturn(payload);

        ResponseEntity<PendingCentralPageDTO> response = controller.listCentral(patientId, 0, 10);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(0L, response.getBody().totalElements());
        verify(centralListService, times(3)).execute(patientId, 0, 10);
    }

    @Test
    void shouldRetryTransientFailureAndSucceedOnThirdAttemptForSnapshot() {
        UUID eventId = UUID.fromString("4dafabfd-4553-4f68-bc31-98a66891131a");
        PendingCentralSnapshotDTO payload = new PendingCentralSnapshotDTO(
                eventId,
                UUID.fromString("54952d81-bd7d-44ae-8a8c-bd77f4957bf3"),
                UUID.fromString("cd4ddd93-f9ff-43be-9fcc-5fb61353de3a"),
                "UPDATE",
                "SUCCESS",
                LocalDateTime.of(2026, 4, 2, 18, 0),
                "{\"snapshot\":true}",
                List.of());

        when(centralSnapshotService.execute(eventId))
                .thenThrow(new TransientDataAccessResourceException("temporary failure 1"))
                .thenThrow(new TransientDataAccessResourceException("temporary failure 2"))
                .thenReturn(Optional.of(payload));

        ResponseEntity<PendingCentralSnapshotDTO> response = controller.snapshot(eventId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(eventId, response.getBody().eventId());
        verify(centralSnapshotService, times(3)).execute(eventId);
    }

    @Test
    void shouldNotRetryForDomainException() {
        UUID patientId = UUID.fromString("166a7e34-9d6f-4d5c-88ff-6c2b58db7204");
        ProgramaAtendimentoException exception = new ProgramaAtendimentoException(
                ReasonCode.PATIENT_NOT_FOUND,
                "corr-retrieve-404",
                "Programa nao encontrado");

        when(retrieveService.findByPatientId(patientId)).thenThrow(exception);

        ProgramaAtendimentoException thrown = assertThrows(
                ProgramaAtendimentoException.class,
                () -> controller.retrieve(patientId));

        assertEquals(ReasonCode.PATIENT_NOT_FOUND, thrown.getReasonCode());
        verify(retrieveService, times(1)).findByPatientId(patientId);
    }
}
