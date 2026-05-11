package com.example.repository;

import com.example.entity.InterventionMateriel;
import com.example.entity.enums.StatutIntervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface InterventionMaterielRepository extends JpaRepository<InterventionMateriel, Long>, JpaSpecificationExecutor<InterventionMateriel> {

    List<InterventionMateriel> findByUniteMaterielIdInAndInterventionStatutInterventionIn(
            List<Long> uniteMaterielIds,
            List<StatutIntervention> statuts
    );

    List<InterventionMateriel> findByInterventionId(Long interventionId);
}