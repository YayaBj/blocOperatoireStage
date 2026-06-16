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
import com.example.service.sterilisation.state.ProcessusState;
import com.example.service.sterilisation.state.ProcessusStateFactory;
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

        ProcessusState stateActuel = ProcessusStateFactory.from(processus.getStatut());

        if (stateActuel.finalState()) {
            throw new IllegalArgumentException("Impossible d’avancer un processus terminé ou en échec");
        }

        ProcessusState prochainState = ProcessusStateFactory.from(stateActuel.next());

        processus.setStatut(prochainState.statut());

        enregistrerMouvement(processus, prochainState);

        historiqueRepository.save(
                new HistoriqueProcessus(
                        LocalDateTime.now(),
                        prochainState.statut(),
                        prochainState.commentaireHistorique(),
                        processus
                )
        );

        if (prochainState.statut() == StatutProcessusSterilisation.TERMINE) {
            terminerProcessus(processus);
        }

        processusRepository.saveAndFlush(processus);
    }

    @Transactional
    public void mettreEnEchec(Long processusId, String commentaire) {
        ProcessusSterilisation processus = findById(processusId);

        ProcessusState stateActuel = ProcessusStateFactory.from(processus.getStatut());

        if (stateActuel.finalState()) {
            throw new IllegalArgumentException("Impossible de mettre en échec un processus terminé ou déjà en échec");
        }

        ProcessusState echecState = ProcessusStateFactory.from(StatutProcessusSterilisation.ECHEC);

        processus.setStatut(echecState.statut());
        processus.setCommentaire(commentaire);

        enregistrerMouvement(processus, echecState);

        processus.getDemandeSterilisation().setStatut(StatutDemandeSterilisation.REFUSEE);

        libererMachines(processus);

        historiqueRepository.save(
                new HistoriqueProcessus(
                        LocalDateTime.now(),
                        echecState.statut(),
                        commentaire == null || commentaire.isBlank()
                                ? echecState.commentaireHistorique()
                                : commentaire,
                        processus
                )
        );

        processusRepository.saveAndFlush(processus);
    }

    private void terminerProcessus(ProcessusSterilisation processus) {
        processus.setDateFin(LocalDateTime.now());
        processus.getDemandeSterilisation().setStatut(StatutDemandeSterilisation.TERMINEE);

        mettreBoiteSterile(processus);

        libererMachines(processus);
    }

    private static void mettreBoiteSterile(ProcessusSterilisation processus) {
        processus.getDemandeSterilisation()
                .getBoiteChirurgicale()
                .setStatut(StatutBoite.EN_STOCK_STERILE);

        processus.getDemandeSterilisation()
                .getBoiteChirurgicale()
                .getMateriels()
                .forEach(boiteMateriel ->
                        boiteMateriel.getUniteMateriel()
                                .setEtat(EtatMateriel.STERILE)
                );
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
        if (!machine.estUtilisable()) {
            throw new IllegalArgumentException("La machine " + machine.getNom() + " est en maintenance ou en erreur");
        }
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

    private void enregistrerMouvement(ProcessusSterilisation processus, ProcessusState state) {
        if (!state.hasMovement()) {
            return;
        }

        mouvementBoiteService.enregistrerMouvement(
                processus.getDemandeSterilisation().getBoiteChirurgicale().getId(),
                processus.getId(),
                state.ancienneZone(),
                state.nouvelleZone(),
                state.typeMouvement(),
                state.commentaireMouvement()
        );
    }
}