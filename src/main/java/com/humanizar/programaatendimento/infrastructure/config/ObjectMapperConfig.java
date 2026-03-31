package com.humanizar.programaatendimento.infrastructure.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.humanizar.programaatendimento.application.inbound.dto.InboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.inbound.dto.messaging.AcolhimentoDeletedDTO;
import com.humanizar.programaatendimento.application.inbound.dto.messaging.AcolhimentoNucleoPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.messaging.AcolhimentoUpsertDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.AbordagemPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoPatientDTO;
import com.humanizar.programaatendimento.application.inbound.dto.nucleo.NucleoResponsavelDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.AtEscolaSemanaDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.AtEscolaSemanaScheduleDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaAtendimentoDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaDeleteDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaEscolaDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaSemanaDTO;
import com.humanizar.programaatendimento.application.inbound.dto.programa.ProgramaSemanaScheduleDTO;
import com.humanizar.programaatendimento.application.outbound.dto.CallbackDTO;
import com.humanizar.programaatendimento.application.outbound.dto.OutboundEnvelopeDTO;
import com.humanizar.programaatendimento.application.outbound.dto.ProgramaCommandDTO;
import com.humanizar.programaatendimento.application.outbound.dto.ProgramaDeletedCommandDTO;

@Configuration
@ImportRuntimeHints(ObjectMapperConfig.ObjectMapperRuntimeHints.class)
public class ObjectMapperConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    public static class ObjectMapperRuntimeHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            registerJsonBinding(hints, InboundEnvelopeDTO.class);
            registerJsonBinding(hints, AcolhimentoDeletedDTO.class);
            registerJsonBinding(hints, AcolhimentoNucleoPatientDTO.class);
            registerJsonBinding(hints, AcolhimentoUpsertDTO.class);
            registerJsonBinding(hints, AbordagemPatientDTO.class);
            registerJsonBinding(hints, NucleoPatientDTO.class);
            registerJsonBinding(hints, NucleoResponsavelDTO.class);
            registerJsonBinding(hints, ProgramaAtendimentoDTO.class);
            registerJsonBinding(hints, ProgramaDeleteDTO.class);
            registerJsonBinding(hints, ProgramaEscolaDTO.class);
            registerJsonBinding(hints, ProgramaSemanaDTO.class);
            registerJsonBinding(hints, ProgramaSemanaScheduleDTO.class);
            registerJsonBinding(hints, AtEscolaSemanaDTO.class);
            registerJsonBinding(hints, AtEscolaSemanaScheduleDTO.class);
            registerJsonBinding(hints, CallbackDTO.class);
            registerJsonBinding(hints, OutboundEnvelopeDTO.class);
            registerJsonBinding(hints, ProgramaCommandDTO.class);
            registerJsonBinding(hints, ProgramaDeletedCommandDTO.class);
        }

        private void registerJsonBinding(RuntimeHints hints, Class<?> type) {
            hints.reflection().registerType(type, MemberCategory.values());
        }
    }
}
