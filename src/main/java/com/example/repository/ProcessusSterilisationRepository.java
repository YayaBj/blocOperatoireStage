package com.example.repository;

import com.example.entity.ProcessusSterilisation;
import com.example.entity.enums.StatutProcessusSterilisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessusSterilisationRepository extends JpaRepository<ProcessusSterilisation, Long> {

    List<ProcessusSterilisation> findByStatut(StatutProcessusSterilisation statut);

    boolean existsByDemandeSterilisationId(Long demandeId);
}