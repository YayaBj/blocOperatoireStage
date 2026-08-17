package com.example.service;

import com.example.entity.*;
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

    /**
     * Crée un nouveau processus de stérilisation à partir d'une demande acceptée.
     * Vérifie les machines sélectionnées, initialise le processus à l'état EN_ATTENTE,
     * met la demande en cours et marque les machines comme actives.
     *
     * @param demandeId identifiant de la demande de stérilisation
     * @param machineLavageId identifiant de la machine de lavage
     * @param machineAutoclaveId identifiant de la machine de stérilisation
     * @param commentaire commentaire éventuel associé au processus
     * @throws IllegalArgumentException si la demande est introuvable ou invalide,
     *                                  si un processus existe déjà pour cette demande,
     *                                  ou si une machine est introuvable, du mauvais type
     *                                  ou inutilisable
     */
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

    /**
     * Fait progresser un processus de stérilisation vers son état suivant.
     * Enregistre le mouvement éventuel de la boîte ainsi qu'une nouvelle entrée
     * dans l'historique du processus.
     *
     * @param processusId identifiant du processus à faire avancer
     * @throws IllegalArgumentException si le processus est introuvable
     *                                  ou s'il se trouve déjà dans un état final
     */
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

    /**
     * Place un processus de stérilisation à l'état ECHEC.
     * Met à jour la demande associée, libère les machines utilisées
     * et enregistre l'échec dans l'historique.
     *
     * @param processusId identifiant du processus concerné
     * @param commentaire commentaire décrivant la cause de l'échec
     * @throws IllegalArgumentException si le processus est introuvable
     *                                  ou s'il se trouve déjà dans un état final
     */
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

    /**
     * Finalise un processus de stérilisation arrivé à son terme.
     * Enregistre la date de fin, termine la demande, stérilise les unités
     * ayant suivi correctement le cycle, remet éventuellement la boîte
     * en stock stérile et libère les machines.
     *
     * @param processus processus de stérilisation à finaliser
     */
    private void terminerProcessus(ProcessusSterilisation processus) {

        processus.setDateFin(LocalDateTime.now());

        processus.getDemandeSterilisation()
                .setStatut(StatutDemandeSterilisation.TERMINEE);

        steriliserMateriels(processus);

        if (processus.getDemandeSterilisation()
                .getBoiteChirurgicale()
                .getStatut() != StatutBoite.INCOMPLETE) {

            remettreBoiteEnStockSterile(processus);
        }

        libererMachines(processus);
    }

    /**
     * Place la boîte chirurgicale associée au processus
     * dans le stock stérile.
     *
     * @param processus processus contenant la boîte concernée
     */
    private static void remettreBoiteEnStockSterile(ProcessusSterilisation processus) {

        processus.getDemandeSterilisation()
                .getBoiteChirurgicale()
                .setStatut(StatutBoite.EN_STOCK_STERILE);
    }

    /**
     * Met à l'état STERILE les unités de matériel de la boîte
     * qui sont encore à l'état EN_STERILISATION.
     *
     * @param processus processus contenant les matériels à mettre à jour
     */
    private static void steriliserMateriels(ProcessusSterilisation processus) {

        processus.getDemandeSterilisation()
                .getBoiteChirurgicale()
                .getMateriels()
                .forEach(boiteMateriel -> {

                    UniteMateriel unite = boiteMateriel.getUniteMateriel();

                    if (unite.getEtat() == EtatMateriel.EN_STERILISATION) {
                        unite.setEtat(EtatMateriel.STERILE);
                    }
                });
    }

    /**
     * Libère les machines de lavage et de stérilisation utilisées par le processus.
     * Leur statut repasse à IDLE et leur cycle en cours est supprimé.
     *
     * @param processus processus utilisant les machines à libérer
     */
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

    /**
     * Vérifie qu'une machine peut être utilisée pour un processus de stérilisation.
     *
     * @param machine machine à contrôler
     * @throws IllegalArgumentException si la machine est en maintenance
     *                                  ou en erreur
     */
    private void verifierMachineUtilisable(Machine machine) {
        if (!machine.estUtilisable()) {
            throw new IllegalArgumentException("La machine " + machine.getNom() + " est en maintenance ou en erreur");
        }
    }

    /**
     * Retourne l'ensemble des processus de stérilisation enregistrés.
     *
     * @return la liste de tous les processus de stérilisation
     */
    @Transactional(readOnly = true)
    public List<ProcessusSterilisation> findAll() {
        return processusRepository.findAll();
    }

    /**
     * Recherche un processus de stérilisation à partir de son identifiant.
     *
     * @param id identifiant du processus recherché
     * @return le processus de stérilisation correspondant
     * @throws IllegalArgumentException si le processus est introuvable
     */
    @Transactional(readOnly = true)
    public ProcessusSterilisation findById(Long id) {
        return processusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processus introuvable"));
    }

    /**
     * Enregistre le mouvement de la boîte associé à un changement d'état
     * lorsque l'état concerné prévoit un déplacement.
     *
     * @param processus processus de stérilisation concerné
     * @param state état du processus contenant les informations du mouvement
     */
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