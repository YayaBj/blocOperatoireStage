package com.example.repository;

import com.example.entity.AffectationPersonnel;
import com.example.entity.enums.StatutIntervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AffectationPersonnelRepository extends JpaRepository<AffectationPersonnel, Long>, JpaSpecificationExecutor<AffectationPersonnel> {

    List<AffectationPersonnel> findByPersonnelIdInAndInterventionStatutInterventionIn(
            List<Long> personnelIds,
            List<StatutIntervention> statuts
    );

    List<AffectationPersonnel> findByInterventionId(Long interventionId);
}