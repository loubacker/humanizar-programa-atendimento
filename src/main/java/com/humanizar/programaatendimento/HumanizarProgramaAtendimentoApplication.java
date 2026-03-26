package com.humanizar.programaatendimento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableResilientMethods
public class HumanizarProgramaAtendimentoApplication {

	static void main(String[] args) {
		SpringApplication.run(HumanizarProgramaAtendimentoApplication.class, args);
	}

}
