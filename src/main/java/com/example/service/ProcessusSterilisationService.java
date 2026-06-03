package com.example.service;

import com.example.entity.DemandeSterilisation;
import com.example.entity.Machine;
import com.example.entity.ProcessusSterilisation;
import com.example.entity.enums.StatutDemandeSterilisation;
import com.example.entity.enums.StatutMachine;
import com.example.entity.enums.StatutProcessusSterilisation;
import com.example.entity.enums.TypeMachine;
import com.example.repository.DemandeSterilisationRepository;
import com.example.repository.MachineRepository;
import com.example.repository.ProcessusSterilisationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProcessusSterilisationService {

    private final ProcessusSterilisationRepository processusRepository;
    private final DemandeSterilisationRepository demandeRepository;
    private final MachineRepository machineRepository;

    public ProcessusSterilisationService(ProcessusSterilisationRepository processusRepository,
                                         DemandeSterilisationRepository demandeRepository,
                                         MachineRepository machineRepository) {
        this.processusRepository = processusRepository;
        this.demandeRepository = demandeRepository;
        this.machineRepository = machineRepository;
    }

    @Transactional
    public void creerProcessus(Long demandeId,
                               Long machineLavageId,
                               Long machineAutoclaveId,
                               String commentaire) {

        DemandeSterilisation demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));

        if (demande.getStatut() != StatutDemandeSterilisation.ACCEPTEE) {
            throw new IllegalArgumentException("Seule une demande acceptée peut lancer un processus");
        }

        if (processusRepository.existsByDemandeSterilisationId(demandeId)) {
            throw new IllegalArgumentException("Un processus existe déjà pour cette demande");
        }

        Machine machineLavage = machineRepository.findById(machineLavageId)
                .orElseThrow(() -> new IllegalArgumentException("Machine de lavage introuvable"));

        Machine machineAutoclave = machineRepository.findById(machineAutoclaveId)
                .orElseThrow(() -> new IllegalArgumentException("Machine autoclave introuvable"));

        if (machineLavage.getTypeMachine() != TypeMachine.LAVAGE) {
            throw new IllegalArgumentException("La machine de lavage sélectionnée n’est pas de type LAVAGE");
        }

        if (machineAutoclave.getTypeMachine() != TypeMachine.STERILISATION) {
            throw new IllegalArgumentException("La machine autoclave sélectionnée n’est pas de type STÉRILISATION");
        }

        verifierMachineUtilisable(machineLavage);
        verifierMachineUtilisable(machineAutoclave);

        ProcessusSterilisation processus = new ProcessusSterilisation(
                LocalDateTime.now(),
                StatutProcessusSterilisation.EN_ATTENTE,
                demande,
                machineLavage,
                machineAutoclave,
                commentaire
        );

        demande.setStatut(StatutDemandeSterilisation.EN_COURS);

        machineLavage.setStatut(StatutMachine.ACTIVE);
        machineLavage.setDerniereUtilisation(LocalDateTime.now());
        machineLavage.setCycleEnCours("Processus demande " + demande.getCodeDemande());

        machineAutoclave.setStatut(StatutMachine.ACTIVE);
        machineAutoclave.setDerniereUtilisation(LocalDateTime.now());
        machineAutoclave.setCycleEnCours("Processus demande " + demande.getCodeDemande());

        processusRepository.save(processus);
        demandeRepository.save(demande);
        machineRepository.save(machineLavage);
        machineRepository.save(machineAutoclave);
    }

    @Transactional
    public void avancerProcessus(Long processusId) {
        ProcessusSterilisation processus = findById(processusId);

        if (processus.getStatut() == StatutProcessusSterilisation.TERMINE) {
            throw new IllegalArgumentException("Ce processus est déjà terminé");
        }

        if (processus.getStatut() == StatutProcessusSterilisation.ECHEC) {
            throw new IllegalArgumentException("Ce processus est en échec");
        }

        StatutProcessusSterilisation prochainStatut = getNextStatut(processus.getStatut());
        processus.setStatut(prochainStatut);

        if (prochainStatut == StatutProcessusSterilisation.TERMINE) {
            terminerProcessus(processus);
        }

        processusRepository.saveAndFlush(processus);
    }

    @Transactional
    public void mettreEnEchec(Long processusId, String commentaire) {
        ProcessusSterilisation processus = findById(processusId);

        if (processus.getStatut() == StatutProcessusSterilisation.TERMINE) {
            throw new IllegalArgumentException("Impossible de mettre en échec un processus terminé");
        }

        processus.setStatut(StatutProcessusSterilisation.ECHEC);
        processus.setCommentaire(commentaire);

        processus.getDemandeSterilisation().setStatut(StatutDemandeSterilisation.REFUSEE);

        libererMachines(processus);

        processusRepository.saveAndFlush(processus);
    }

    private void terminerProcessus(ProcessusSterilisation processus) {
        processus.setDateFin(LocalDateTime.now());
        processus.getDemandeSterilisation().setStatut(StatutDemandeSterilisation.TERMINEE);

        processus.getDemandeSterilisation()
                .getBoiteChirurgicale()
                .setStatut(com.example.entity.enums.StatutBoite.EN_STOCK_STERILE);

        processus.getDemandeSterilisation()
                .getBoiteChirurgicale()
                .getMateriels()
                .forEach(boiteMateriel ->
                        boiteMateriel.getUniteMateriel()
                                .setEtat(com.example.entity.enums.EtatMateriel.STERILE)
                );

        libererMachines(processus);
    }

    private void libererMachines(ProcessusSterilisation processus) {
        Machine lavage = processus.getMachineLavage();
        Machine autoclave = processus.getMachineAutoclave();

        if (lavage != null) {
            lavage.setStatut(StatutMachine.IDLE);
            lavage.setCycleEnCours(null);
        }

        if (autoclave != null) {
            autoclave.setStatut(StatutMachine.IDLE);
            autoclave.setCycleEnCours(null);
        }
    }

    private void verifierMachineUtilisable(Machine machine) {
        if (machine.getStatut() == StatutMachine.MAINTENANCE) {
            throw new IllegalArgumentException("La machine " + machine.getNom() + " est en maintenance");
        }

        if (machine.getStatut() == StatutMachine.ERROR) {
            throw new IllegalArgumentException("La machine " + machine.getNom() + " est en erreur");
        }
    }

    private StatutProcessusSterilisation getNextStatut(StatutProcessusSterilisation statut) {
        return switch (statut) {
            case EN_ATTENTE -> StatutProcessusSterilisation.LAVAGE;
            case LAVAGE -> StatutProcessusSterilisation.CONDITIONNEMENT;
            case CONDITIONNEMENT -> StatutProcessusSterilisation.AUTOCLAVE;
            case AUTOCLAVE -> StatutProcessusSterilisation.VALIDATION;
            case VALIDATION -> StatutProcessusSterilisation.TERMINE;
            default -> statut;
        };
    }

    @Transactional(readOnly = true)
    public List<ProcessusSterilisation> findAll() {
        return processusRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ProcessusSterilisation findById(Long id) {
        return processusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processus introuvable"));
    }
}