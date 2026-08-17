package com.example.repository;

import com.example.entity.Intervention;
import com.example.entity.enums.StatutIntervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface InterventionRepository extends JpaRepository<Intervention, Long>, JpaSpecificationExecutor<Intervention> {

    List<Intervention> findByPatientIdAndStatutInterventionIn(Long patientId, List<StatutIntervention> statuts);

    List<Intervention> findBySalleIdAndStatutInterventionIn(Long salleId, List<StatutIntervention> statuts);

    boolean existsBySalleId(Long salleId);

    boolean existsByInterventionBoitesBoiteChirurgicaleId(Long boiteId);

    boolean existsByPatientId(Long patientId);
}