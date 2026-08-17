package com.example.repository;

import com.example.entity.IncidentSterilisation;
import com.example.entity.enums.GraviteIncident;
import com.example.entity.enums.TypeIncidentSterilisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentSterilisationRepository extends JpaRepository<IncidentSterilisation, Long> {

    List<IncidentSterilisation> findByProcessusSterilisationId(Long processusId);

    List<IncidentSterilisation> findByTypeIncident(TypeIncidentSterilisation typeIncident);

    List<IncidentSterilisation> findByGravite(GraviteIncident gravite);

    List<IncidentSterilisation> findByProcessusSterilisationDemandeSterilisationBoiteChirurgicaleId(Long boiteId);
}