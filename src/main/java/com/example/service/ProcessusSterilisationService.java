package com.example.service;

import com.example.entity.DemandeSterilisation;
import com.example.entity.HistoriqueProcessus;
import com.example.entity.Machine;
import com.example.entity.ProcessusSterilisation;
import com.example.entity.enums.*;
import com.example.repository.DemandeSterilisationRepository;
import com.example.repository.HistoriqueProcessusRepository;
import com.example.repository.MachineRepository;
import com.example.repository.ProcessusSterilisationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProcessusSterilisationService {

    private final MouvementBoiteService mouvementBoiteService;

    private final ProcessusSterilisationRepository processusRepository;
    private final DemandeSterilisationRepository demandeRepository;
    private final MachineRepository machineRepository;
    private final HistoriqueProcessusRepository historiqueRepository;

    public ProcessusSterilisationService(ProcessusSterilisationRepository processusRepository,
                                         DemandeSterilisationRepository demandeRepository,
                                         MachineRepository machineRepository,
                                         HistoriqueProcessusRepository historiqueRepository,
                                         MouvementBoiteService mouvementBoiteService) {
        this.processusRepository = processusRepository;
        this.demandeRepository = demandeRepository;
        this.machineRepository = machineRepository;
        this.historiqueRepository = historiqueRepository;
        this.mouvementBoiteService = mouvementBoiteService;
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

        HistoriqueProcessus historique =
                new HistoriqueProcessus(
                        LocalDateTime.now(),
                        StatutProcessusSterilisation.EN_ATTENTE,
                        "Processus créé",
                        processus
                );

        historiqueRepository.save(historique);
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
        enregistrerMouvementSelonEtape(processus, prochainStatut);

        historiqueRepository.save(
                new HistoriqueProcessus(
                        LocalDateTime.now(),
                        prochainStatut,
                        "Passage à l'étape " + prochainStatut,
                        processus
                )
        );

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
        enregistrerMouvementSelonEtape(processus, StatutProcessusSterilisation.ECHEC);
        processus.setCommentaire(commentaire);

        processus.getDemandeSterilisation().setStatut(StatutDemandeSterilisation.REFUSEE);

        libererMachines(processus);

        historiqueRepository.save(
                new HistoriqueProcessus(
                        LocalDateTime.now(),
                        StatutProcessusSterilisation.ECHEC,
                        commentaire == null || commentaire.isBlank()
                                ? "Processus marqué en échec"
                                : commentaire,
                        processus
                )
        );

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

    private void enregistrerMouvementSelonEtape(ProcessusSterilisation processus,
                                                StatutProcessusSterilisation statut) {

        Long boiteId = processus.getDemandeSterilisation()
                .getBoiteChirurgicale()
                .getId();

        Long processusId = processus.getId();

        switch (statut) {
            case LAVAGE -> mouvementBoiteService.enregistrerMouvement(
                    boiteId,
                    processusId,
                    ZoneBoite.STOCK_SALE,
                    ZoneBoite.LAVAGE,
                    TypeMouvementBoite.PASSAGE_LAVAGE,
                    "Boîte envoyée au lavage"
            );

            case CONDITIONNEMENT -> mouvementBoiteService.enregistrerMouvement(
                    boiteId,
                    processusId,
                    ZoneBoite.LAVAGE,
                    ZoneBoite.CONDITIONNEMENT,
                    TypeMouvementBoite.PASSAGE_CONDITIONNEMENT,
                    "Boîte envoyée au conditionnement"
            );

            case AUTOCLAVE -> mouvementBoiteService.enregistrerMouvement(
                    boiteId,
                    processusId,
                    ZoneBoite.CONDITIONNEMENT,
                    ZoneBoite.AUTOCLAVE,
                    TypeMouvementBoite.PASSAGE_AUTOCLAVE,
                    "Boîte envoyée à l’autoclave"
            );

            case TERMINE -> mouvementBoiteService.enregistrerMouvement(
                    boiteId,
                    processusId,
                    ZoneBoite.AUTOCLAVE,
                    ZoneBoite.STOCK_STERILE,
                    TypeMouvementBoite.RETOUR_STOCK_STERILE,
                    "Boîte retournée au stock stérile"
            );

            case ECHEC -> mouvementBoiteService.enregistrerMouvement(
                    boiteId,
                    processusId,
                    null,
                    ZoneBoite.QUARANTAINE,
                    TypeMouvementBoite.MISE_QUARANTAINE,
                    "Boîte mise en quarantaine après échec"
            );

            default -> {
            }
        }
    }
}