package com.humanizar.programaatendimento.infrastructure.persistence.repository.programa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.humanizar.programaatendimento.infrastructure.persistence.entity.programa.AtEscolaSemanaScheduleEntity;

@Repository
public interface AtEscolaSemanaScheduleRepository extends JpaRepository<AtEscolaSemanaScheduleEntity, UUID> {

    List<AtEscolaSemanaScheduleEntity> findByAtEscolaSemanaId(UUID atEscolaSemanaId);

    void deleteByAtEscolaSemanaId(UUID atEscolaSemanaId);
}
