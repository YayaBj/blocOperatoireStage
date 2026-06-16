package com.example.service;

import com.example.entity.IncidentSterilisation;
import com.example.entity.Machine;
import com.example.entity.ProcessusSterilisation;
import com.example.entity.enums.GraviteIncident;
import com.example.entity.enums.TypeIncidentSterilisation;
import com.example.repository.IncidentSterilisationRepository;
import com.example.repository.MachineRepository;
import com.example.repository.ProcessusSterilisationRepository;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IncidentSterilisationService {

    private final IncidentSterilisationRepository incidentRepository;
    private final ProcessusSterilisationRepository processusRepository;
    private final MachineRepository machineRepository;

    public IncidentSterilisationService(IncidentSterilisationRepository incidentRepository,
                                        ProcessusSterilisationRepository processusRepository,
                                        MachineRepository machineRepository) {
        this.incidentRepository = incidentRepository;
        this.processusRepository = processusRepository;
        this.machineRepository = machineRepository;
    }

    @Transactional
    public void createIncident(Long processusId,
                               Long machineId,
                               TypeIncidentSterilisation typeIncident,
                               GraviteIncident gravite,
                               String description) {

        verifierDonneesIncident(processusId, typeIncident, gravite, description);

        ProcessusSterilisation processus = findProcessus(processusId);

        Machine machine = findMachineIfPresent(machineId);

        IncidentSterilisation incident = new IncidentSterilisation(
                LocalDateTime.now(),
                typeIncident,
                gravite,
                description.trim(),
                processus,
                machine
        );

        incidentRepository.saveAndFlush(incident);
    }

    @Nullable
    private Machine findMachineIfPresent(Long machineId) {
        if (machineId == null) {
            return null;
        }

        return machineRepository.findById(machineId)
                .orElseThrow(() -> new IllegalArgumentException("Machine introuvable"));
    }

    private ProcessusSterilisation findProcessus(Long processusId) {
        return processusRepository.findById(processusId)
                .orElseThrow(() -> new IllegalArgumentException("Processus introuvable"));
    }

    private static void verifierDonneesIncident(Long processusId, TypeIncidentSterilisation typeIncident, GraviteIncident gravite, String description) {
        if (processusId == null) {
            throw new IllegalArgumentException("Le processus est obligatoire");
        }

        if (typeIncident == null) {
            throw new IllegalArgumentException("Le type d’incident est obligatoire");
        }

        if (gravite == null) {
            throw new IllegalArgumentException("La gravité est obligatoire");
        }

        if (description == null || description.trim().isBlank()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
    }

    @Transactional(readOnly = true)
    public List<IncidentSterilisation> findAll() {
        return incidentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<IncidentSterilisation> findByProcessus(Long processusId) {
        return incidentRepository.findByProcessusSterilisationId(processusId);
    }
}