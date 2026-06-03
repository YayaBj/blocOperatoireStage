package com.example.repository;

import com.example.entity.DemandeSterilisation;
import com.example.entity.enums.StatutDemandeSterilisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemandeSterilisationRepository extends JpaRepository<DemandeSterilisation, Long> {

    boolean existsByCodeDemandeIgnoreCase(String codeDemande);

    List<DemandeSterilisation> findByStatut(StatutDemandeSterilisation statut);
}