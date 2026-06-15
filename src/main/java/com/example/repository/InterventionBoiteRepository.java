package com.example.repository;

import com.example.entity.InterventionBoite;
import com.example.entity.enums.StatutIntervention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterventionBoiteRepository extends JpaRepository<InterventionBoite, Long> {

    List<InterventionBoite> findByInterventionId(Long interventionId);

    List<InterventionBoite> findByBoiteChirurgicaleIdInAndInterventionStatutInterventionIn(
            List<Long> boiteIds,
            List<StatutIntervention> statuts
    );
}