package com.example.service;

import com.example.entity.*;
import com.example.entity.enums.*;
import com.example.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class InterventionService {

    private final DemandeSterilisationService demandeSterilisationService;

    private final InterventionRepository interventionRepository;
    private final PatientRepository patientRepository;
    private final SalleRepository salleRepository;
    private final PersonnelRepository personnelRepository;
    private final AffectationPersonnelRepository affectationPersonnelRepository;
    private final BoiteChirurgicaleRepository boiteChirurgicaleRepository;
    private final InterventionBoiteRepository interventionBoiteRepository;

    public InterventionService(
            DemandeSterilisationService demandeSterilisationService,
            InterventionRepository interventionRepository,
            PatientRepository patientRepository,
            SalleRepository salleRepository,
            PersonnelRepository personnelRepository,
            AffectationPersonnelRepository affectationPersonnelRepository,
            BoiteChirurgicaleRepository boiteChirurgicaleRepository,
            InterventionBoiteRepository interventionBoiteRepository) {
        this.demandeSterilisationService = demandeSterilisationService;
        this.interventionRepository = interventionRepository;
        this.patientRepository = patientRepository;
        this.salleRepository = salleRepository;
        this.personnelRepository = personnelRepository;
        this.affectationPersonnelRepository = affectationPersonnelRepository;
        this.boiteChirurgicaleRepository = boiteChirurgicaleRepository;
        this.interventionBoiteRepository = interventionBoiteRepository;
    }

    /**
     * Crée et planifie une nouvelle intervention chirurgicale.
     * Vérifie la disponibilité du patient, de la salle, du personnel et des boîtes
     * avant d'enregistrer l'intervention et les différentes affectations.
     *
     * @param typeIntervention type de l'intervention
     * @param priorite niveau de priorité de l'intervention
     * @param dateHeureDebut date et heure prévues de début
     * @param dureePrevue durée prévue de l'intervention en minutes
     * @param patientId identifiant du patient concerné
     * @param salleId identifiant de la salle sélectionnée
     * @param personnelsAvecRoles personnels affectés et leurs rôles respectifs
     * @param boiteIds identifiants des boîtes chirurgicales sélectionnées
     * @throws IllegalArgumentException si les données sont invalides,
     *                                  si une ressource est introuvable
     *                                  ou si une ressource n'est pas disponible
     */
    @Transactional
    public void createIntervention(
            String typeIntervention,
            PrioriteIntervention priorite,
            LocalDateTime dateHeureDebut,
            int dureePrevue,
            Long patientId,
            Long salleId,
            Map<Long, RoleIntervention> personnelsAvecRoles,
            List<Long> boiteIds
    ) {
        verifierDonneesIntervention(typeIntervention, priorite, dateHeureDebut, dureePrevue);

        LocalDateTime dateHeureFin = dateHeureDebut.plusMinutes(dureePrevue);

        Patient patient = findPatient(patientId);
        Salle salle = findSalleDisponible(salleId);

        verifierDisponibilites(
                patient.getId(),
                salle.getId(),
                personnelsAvecRoles,
                boiteIds,
                dateHeureDebut,
                dateHeureFin,
                null
        );

        Intervention intervention = new Intervention(
                typeIntervention.trim(),
                priorite,
                dateHeureDebut,
                dureePrevue,
                StatutIntervention.PLANIFIEE,
                patient,
                salle
        );

        interventionRepository.saveAndFlush(intervention);

        affecterPersonnel(personnelsAvecRoles, intervention);
        reserverBoitesPourIntervention(boiteIds, intervention);
    }

    /**
     * Vérifie la validité des informations obligatoires d'une intervention.
     *
     * @param typeIntervention type de l'intervention
     * @param priorite priorité de l'intervention
     * @param dateHeureDebut date et heure prévues de début
     * @param dureePrevue durée prévue en minutes
     * @throws IllegalArgumentException si une donnée obligatoire est invalide
     */
    private void verifierDonneesIntervention(String typeIntervention,
                                             PrioriteIntervention priorite,
                                             LocalDateTime dateHeureDebut,
                                             int dureePrevue) {
        if (typeIntervention == null || typeIntervention.isBlank()) {
            throw new IllegalArgumentException("Le type d’intervention est obligatoire");
        }

        if (priorite == null) {
            throw new IllegalArgumentException("La priorité est obligatoire");
        }

        if (dateHeureDebut == null) {
            throw new IllegalArgumentException("La date de début est obligatoire");
        }

        if (dureePrevue <= 0) {
            throw new IllegalArgumentException("La durée prévue doit être supérieure à 0");
        }
    }

    /**
     * Recherche un patient à partir de son identifiant.
     *
     * @param patientId identifiant du patient
     * @return le patient correspondant
     * @throws IllegalArgumentException si le patient est introuvable
     */
    private Patient findPatient(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient introuvable"));
    }

    /**
     * Recherche une salle et vérifie qu'elle est actuellement disponible.
     *
     * @param salleId identifiant de la salle
     * @return la salle disponible correspondante
     * @throws IllegalArgumentException si la salle est introuvable ou indisponible
     */
    private Salle findSalleDisponible(Long salleId) {
        Salle salle = salleRepository.findById(salleId)
                .orElseThrow(() -> new IllegalArgumentException("Salle introuvable"));

        if (salle.getStatutSalle() != StatutSalle.DISPONIBLE) {
            throw new IllegalArgumentException("La salle sélectionnée n’est pas disponible");
        }

        return salle;
    }

    /**
     * Vérifie la disponibilité de l'ensemble des ressources nécessaires
     * à une intervention sur un créneau donné.
     *
     * @param patientId identifiant du patient
     * @param salleId identifiant de la salle
     * @param personnelsAvecRoles personnels sélectionnés et leurs rôles
     * @param boiteIds identifiants des boîtes sélectionnées
     * @param debut début du créneau
     * @param fin fin du créneau
     * @param interventionIgnoreeId intervention à ignorer lors d'une modification,
     *                              ou null lors d'une création
     * @throws IllegalArgumentException si une ressource présente un conflit de disponibilité
     */
    private void verifierDisponibilites(Long patientId,
                                        Long salleId,
                                        Map<Long, RoleIntervention> personnelsAvecRoles,
                                        List<Long> boiteIds,
                                        LocalDateTime debut,
                                        LocalDateTime fin,
                                        Long interventionIgnoreeId) {

        verifierDisponibilitePatient(patientId, debut, fin, interventionIgnoreeId);
        verifierDisponibiliteSalle(salleId, debut, fin, interventionIgnoreeId);
        verifierDisponibilitePersonnel(personnelsAvecRoles, debut, fin, interventionIgnoreeId);
        verifierDisponibiliteBoites(boiteIds, debut, fin, interventionIgnoreeId);
    }

    /**
     * Associe les boîtes chirurgicales sélectionnées à une intervention
     * et met à jour leur statut.
     *
     * @param boiteIds identifiants des boîtes à réserver
     * @param intervention intervention concernée
     * @throws IllegalArgumentException si une ou plusieurs boîtes sont introuvables
     */
    private void reserverBoitesPourIntervention(List<Long> boiteIds, Intervention intervention) {
        List<BoiteChirurgicale> boites = boiteChirurgicaleRepository.findAllById(boiteIds);

        if (boites.size() != boiteIds.size()) {
            throw new IllegalArgumentException("Une ou plusieurs boîtes sont introuvables");
        }

        for (BoiteChirurgicale boite : boites) {
            InterventionBoite interventionBoite = new InterventionBoite(intervention, boite);

            boite.setStatut(StatutBoite.EN_STOCK_SALE);

            interventionBoiteRepository.save(interventionBoite);
            boiteChirurgicaleRepository.save(boite);
        }
    }

    /**
     * Affecte les membres du personnel sélectionnés à une intervention
     * en enregistrant le rôle de chacun.
     *
     * @param personnelsAvecRoles personnels à affecter et leurs rôles
     * @param intervention intervention concernée
     * @throws IllegalArgumentException si un membre du personnel est introuvable
     */
    private void affecterPersonnel(Map<Long, RoleIntervention> personnelsAvecRoles, Intervention intervention) {
        for (Map.Entry<Long, RoleIntervention> entry : personnelsAvecRoles.entrySet()) {
            Personnel personnel = personnelRepository.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Personnel introuvable"));

            AffectationPersonnel affectation = new AffectationPersonnel(
                    entry.getValue(),
                    intervention,
                    personnel
            );

            affectationPersonnelRepository.save(affectation);
        }
    }

    /**
     * Termine une intervention chirurgicale et déclenche le traitement
     * des boîtes utilisées afin de lancer leur processus de stérilisation.
     *
     * @param interventionId identifiant de l'intervention à terminer
     * @throws IllegalArgumentException si l'intervention est introuvable,
     *                                  déjà terminée ou annulée
     */
    @Transactional
    public void terminerIntervention(Long interventionId) {
        Intervention intervention = findById(interventionId);

        if (intervention.getStatutIntervention() == StatutIntervention.TERMINEE) {
            throw new IllegalArgumentException("Cette intervention est déjà terminée");
        }

        if (intervention.getStatutIntervention() == StatutIntervention.ANNULEE) {
            throw new IllegalArgumentException("Impossible de terminer une intervention annulée");
        }

        intervention.setStatutIntervention(StatutIntervention.TERMINEE);

        List<InterventionBoite> boites =
                interventionBoiteRepository.findByInterventionId(interventionId);

        for (InterventionBoite interventionBoite : boites) {
            traiterBoiteApresIntervention(interventionBoite, intervention);
        }

        interventionRepository.saveAndFlush(intervention);
    }

    /**
     * Traite une boîte après la fin d'une intervention.
     * Met à jour l'état de la boîte et de son matériel, crée automatiquement
     * une demande de stérilisation puis l'envoie au service de stérilisation.
     *
     * @param interventionBoite association entre l'intervention et la boîte
     * @param intervention intervention terminée
     */
    private void traiterBoiteApresIntervention(InterventionBoite interventionBoite, Intervention intervention) {
        BoiteChirurgicale boite = interventionBoite.getBoiteChirurgicale();

        changerEtatBoiteEtMateriels(
                boite,
                StatutBoite.EN_STOCK_SALE,
                EtatMateriel.EN_STERILISATION
        );

        String codeDemande =
                "DS-" + intervention.getId() + "-" + boite.getId();

        demandeSterilisationService.createDemande(
                codeDemande,
                LocalDate.now(),
                intervention.getPriorite(),
                boite.getId(),
                intervention.getId(),
                "Demande générée automatiquement après intervention"
        );

        DemandeSterilisation demande = demandeSterilisationService.findByCodeDemande(codeDemande);

        demandeSterilisationService.envoyerDemande(demande.getId());
    }

    /**
     * Retourne l'ensemble des interventions enregistrées.
     *
     * @return la liste de toutes les interventions
     */
    @Transactional(readOnly = true)
    public List<Intervention> findAll() {
        return interventionRepository.findAll();
    }

    /**
     * Vérifie qu'un patient ne possède pas déjà une intervention
     * sur le créneau demandé.
     *
     * @param patientId identifiant du patient
     * @param debut début du nouveau créneau
     * @param fin fin du nouveau créneau
     * @param interventionIgnoreeId intervention à ignorer lors d'une modification
     * @throws IllegalArgumentException si le patient possède déjà une intervention
     *                                  sur ce créneau
     */
    private void verifierDisponibilitePatient(Long patientId,
                                              LocalDateTime debut,
                                              LocalDateTime fin,
                                              Long interventionIgnoreeId) {
        List<Intervention> interventions = interventionRepository.findByPatientIdAndStatutInterventionIn(
                patientId,
                statutsBloquants()
        );

        boolean conflit = interventions.stream()
                .filter(intervention -> !intervention.getId().equals(interventionIgnoreeId))
                .anyMatch(intervention -> chevauche(debut, fin, intervention));

        if (conflit) {
            throw new IllegalArgumentException("Le patient a déjà une intervention sur ce créneau");
        }
    }

    /**
     * Vérifie qu'une salle n'est pas déjà utilisée par une autre intervention
     * sur le créneau demandé.
     *
     * @param salleId identifiant de la salle
     * @param debut début du nouveau créneau
     * @param fin fin du nouveau créneau
     * @param interventionIgnoreeId intervention à ignorer lors d'une modification
     * @throws IllegalArgumentException si la salle est déjà occupée
     */
    private void verifierDisponibiliteSalle(Long salleId,
                                            LocalDateTime debut,
                                            LocalDateTime fin,
                                            Long interventionIgnoreeId) {
        List<Intervention> interventions = interventionRepository.findBySalleIdAndStatutInterventionIn(
                salleId,
                statutsBloquants()
        );

        boolean conflit = interventions.stream()
                .filter(intervention -> !intervention.getId().equals(interventionIgnoreeId))
                .anyMatch(intervention -> chevauche(debut, fin, intervention));

        if (conflit) {
            throw new IllegalArgumentException("La salle est déjà occupée sur ce créneau");
        }
    }

    /**
     * Vérifie que les membres du personnel sélectionnés possèdent tous un rôle
     * et ne sont pas déjà affectés à une autre intervention sur le même créneau.
     *
     * @param personnelsAvecRoles personnels sélectionnés et leurs rôles
     * @param debut début du nouveau créneau
     * @param fin fin du nouveau créneau
     * @param interventionIgnoreeId intervention à ignorer lors d'une modification
     * @throws IllegalArgumentException si aucun personnel n'est sélectionné,
     *                                  si un rôle est absent ou si un conflit existe
     */
    private void verifierDisponibilitePersonnel(Map<Long, RoleIntervention> personnelsAvecRoles,
                                                LocalDateTime debut,
                                                LocalDateTime fin,
                                                Long interventionIgnoreeId) {
        if (personnelsAvecRoles == null || personnelsAvecRoles.isEmpty()) {
            throw new IllegalArgumentException("Au moins un membre du personnel doit être affecté");
        }

        if (personnelsAvecRoles.containsValue(null)) {
            throw new IllegalArgumentException("Chaque membre du personnel doit avoir un rôle");
        }

        List<Long> personnelIds = personnelsAvecRoles.keySet().stream().toList();

        List<AffectationPersonnel> affectations =
                affectationPersonnelRepository.findByPersonnelIdInAndInterventionStatutInterventionIn(
                        personnelIds,
                        statutsBloquants()
                );

        boolean conflit = affectations.stream()
                .filter(affectation -> !affectation.getIntervention().getId().equals(interventionIgnoreeId))
                .anyMatch(affectation -> chevauche(debut, fin, affectation.getIntervention()));

        if (conflit) {
            throw new IllegalArgumentException("Un membre du personnel est déjà affecté sur ce créneau");
        }
    }

    /**
     * Vérifie que les boîtes sélectionnées existent, sont disponibles
     * et ne sont pas déjà réservées pour une autre intervention
     * sur le même créneau.
     *
     * @param boiteIds identifiants des boîtes sélectionnées
     * @param debut début du nouveau créneau
     * @param fin fin du nouveau créneau
     * @param interventionIgnoreeId intervention à ignorer lors d'une modification
     * @throws IllegalArgumentException si les boîtes sont invalides,
     *                                  indisponibles ou déjà réservées
     */
    private void verifierDisponibiliteBoites(List<Long> boiteIds,
                                             LocalDateTime debut,
                                             LocalDateTime fin,
                                             Long interventionIgnoreeId) {
        if (boiteIds == null || boiteIds.isEmpty()) {
            throw new IllegalArgumentException("Au moins une boîte chirurgicale doit être sélectionnée");
        }

        List<Long> idsDistincts = boiteIds.stream().distinct().toList();

        if (idsDistincts.size() != boiteIds.size()) {
            throw new IllegalArgumentException("Une boîte chirurgicale est sélectionnée plusieurs fois");
        }

        List<BoiteChirurgicale> boites = boiteChirurgicaleRepository.findAllById(boiteIds);

        if (boites.size() != boiteIds.size()) {
            throw new IllegalArgumentException("Une ou plusieurs boîtes sont introuvables");
        }

        if(interventionIgnoreeId == null) {
            for (BoiteChirurgicale boite : boites) {
                if (boite.getStatut() != StatutBoite.EN_STOCK_STERILE
                        && boite.getStatut() != StatutBoite.ACTIVE) {
                    throw new IllegalArgumentException(
                            "La boîte " + boite.getCodeBoite() + " n’est pas disponible"
                    );
                }
            }
        }

        List<InterventionBoite> boitesUtilisees =
                interventionBoiteRepository.findByBoiteChirurgicaleIdInAndInterventionStatutInterventionIn(
                        boiteIds,
                        statutsBloquants()
                );

        boolean conflit = boitesUtilisees.stream()
                .filter(interventionBoite -> !interventionBoite.getIntervention().getId().equals(interventionIgnoreeId))
                .anyMatch(interventionBoite -> chevauche(debut, fin, interventionBoite.getIntervention()));

        if (conflit) {
            throw new IllegalArgumentException("Une boîte chirurgicale est déjà réservée sur ce créneau");
        }
    }

    /**
     * Vérifie si un nouveau créneau chevauche celui d'une intervention existante.
     *
     * @param nouveauDebut début du nouveau créneau
     * @param nouveauFin fin du nouveau créneau
     * @param existante intervention existante à comparer
     * @return true si les deux créneaux se chevauchent, false sinon
     */
    private boolean chevauche(LocalDateTime nouveauDebut, LocalDateTime nouveauFin, Intervention existante) {
        LocalDateTime ancienDebut = existante.getDateHeureDebut();
        LocalDateTime ancienFin = ancienDebut.plusMinutes(existante.getDureePrevue());

        return nouveauDebut.isBefore(ancienFin) && nouveauFin.isAfter(ancienDebut);
    }

    /**
     * Retourne les statuts d'intervention considérés comme bloquants
     * lors de la vérification des disponibilités.
     *
     * @return la liste des statuts bloquant une ressource
     */
    private List<StatutIntervention> statutsBloquants() {
        return List.of(
                StatutIntervention.PLANIFIEE,
                StatutIntervention.EN_COURS
        );
    }

    /**
     * Recherche les boîtes chirurgicales actuellement disponibles
     * pour être affectées à une intervention.
     *
     * @return la liste des boîtes en stock stérile ou actives
     */
    @Transactional(readOnly = true)
    public List<BoiteChirurgicale> findBoitesDisponibles() {
        return boiteChirurgicaleRepository.findAll().stream()
                .filter(boite ->
                        boite.getStatut() == StatutBoite.EN_STOCK_STERILE
                                || boite.getStatut() == StatutBoite.ACTIVE
                )
                .toList();
    }

    /**
     * Recherche une intervention à partir de son identifiant.
     *
     * @param id identifiant de l'intervention
     * @return l'intervention correspondante
     * @throws IllegalArgumentException si l'intervention est introuvable
     */
    @Transactional(readOnly = true)
    public Intervention findById(Long id) {
        return interventionRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Intervention introuvable"));
    }

    /**
     * Annule une intervention et remet les boîtes qui lui étaient associées
     * dans un état disponible et stérile.
     *
     * @param id identifiant de l'intervention à annuler
     * @throws IllegalArgumentException si l'intervention est introuvable
     *                                  ou déjà terminée
     */
    @Transactional
    public void annulerIntervention(Long id) {
        Intervention intervention = findById(id);

        if (intervention.getStatutIntervention() == StatutIntervention.TERMINEE) {
            throw new IllegalArgumentException("Impossible d’annuler une intervention terminée");
        }

        List<InterventionBoite> boitesIntervention =
                interventionBoiteRepository.findByInterventionId(id);

        for (InterventionBoite interventionBoite : boitesIntervention) {
            changerEtatBoiteEtMateriels(
                    interventionBoite.getBoiteChirurgicale(),
                    StatutBoite.EN_STOCK_STERILE,
                    EtatMateriel.STERILE
            );
        }

        intervention.setStatutIntervention(StatutIntervention.ANNULEE);
        interventionRepository.saveAndFlush(intervention);
    }


    /**
     * Supprime une intervention enregistrée.
     *
     * @param id identifiant de l'intervention à supprimer
     * @throws IllegalArgumentException si l'intervention est introuvable
     */
    @Transactional
    public void deleteIntervention(Long id) {
        Intervention intervention = findById(id);
        interventionRepository.delete(intervention);
    }

    /**
     * Démarre une intervention planifiée.
     * L'intervention passe à l'état EN_COURS et les boîtes associées
     * ainsi que leurs matériels sont marqués comme étant utilisés.
     *
     * @param interventionId identifiant de l'intervention à démarrer
     * @throws IllegalArgumentException si l'intervention est introuvable,
     *                                  annulée, terminée ou déjà en cours
     */
    @Transactional
    public void demarrerIntervention(Long interventionId) {
        Intervention intervention = findById(interventionId);

        if (intervention.getStatutIntervention() == StatutIntervention.ANNULEE) {
            throw new IllegalArgumentException("Impossible de démarrer une intervention annulée");
        }

        if (intervention.getStatutIntervention() == StatutIntervention.TERMINEE) {
            throw new IllegalArgumentException("Cette intervention est déjà terminée");
        }

        if (intervention.getStatutIntervention() == StatutIntervention.EN_COURS) {
            throw new IllegalArgumentException("Cette intervention est déjà en cours");
        }

        intervention.setStatutIntervention(StatutIntervention.EN_COURS);

        List<InterventionBoite> boites =
                interventionBoiteRepository.findByInterventionId(interventionId);

        for (InterventionBoite interventionBoite : boites) {
            changerEtatBoiteEtMateriels(
                    interventionBoite.getBoiteChirurgicale(),
                    StatutBoite.EN_STOCK_SALE,
                    EtatMateriel.EN_UTILISATION
            );
        }

        interventionRepository.saveAndFlush(intervention);
    }

    /**
     * Modifie le créneau d'une intervention existante.
     * Vérifie la validité du nouveau créneau et la disponibilité
     * de toutes les ressources avant d'appliquer la modification.
     *
     * @param interventionId identifiant de l'intervention à déplacer
     * @param nouveauDebut nouvelle date et heure de début
     * @param nouvelleFin nouvelle date et heure de fin
     * @throws IllegalArgumentException si le nouveau créneau est invalide,
     *                                  si l'intervention ne peut pas être modifiée
     *                                  ou si une ressource n'est pas disponible
     */
    @Transactional
    public void deplacerIntervention(Long interventionId,
                                     LocalDateTime nouveauDebut,
                                     LocalDateTime nouvelleFin) {

        Intervention intervention = findById(interventionId);

        verifierModificationPlanningPossible(
                intervention,
                nouveauDebut,
                nouvelleFin
        );

        int nouvelleDuree =
                (int) Duration.between(nouveauDebut, nouvelleFin).toMinutes();

        Map<Long, RoleIntervention> personnelsAvecRoles =
                affectationPersonnelRepository.findByInterventionId(interventionId)
                        .stream()
                        .collect(
                                java.util.stream.Collectors.toMap(
                                        affectation -> affectation.getPersonnel().getId(),
                                        AffectationPersonnel::getRoleDansIntervention
                                )
                        );

        List<Long> boiteIds =
                interventionBoiteRepository.findByInterventionId(interventionId)
                        .stream()
                        .map(interventionBoite ->
                                interventionBoite.getBoiteChirurgicale().getId())
                        .toList();

        verifierDisponibilites(
                intervention.getPatient().getId(),
                intervention.getSalle().getId(),
                personnelsAvecRoles,
                boiteIds,
                nouveauDebut,
                nouvelleFin,
                interventionId
        );

        intervention.setDateHeureDebut(nouveauDebut);
        intervention.setDureePrevue(nouvelleDuree);

        interventionRepository.saveAndFlush(intervention);
    }

    /**
     * Modifie les horaires et la durée d'une intervention à partir
     * d'un nouveau début et d'une nouvelle fin.
     *
     * @param interventionId identifiant de l'intervention
     * @param nouveauDebut nouvelle date et heure de début
     * @param nouvelleFin nouvelle date et heure de fin
     * @throws IllegalArgumentException si la modification n'est pas autorisée
     *                                  ou si le nouveau créneau provoque un conflit
     */
    @Transactional
    public void redimensionnerIntervention(Long interventionId, LocalDateTime nouveauDebut, LocalDateTime nouvelleFin) {
        deplacerIntervention(interventionId, nouveauDebut, nouvelleFin);
    }

    /**
     * Vérifie qu'une intervention peut être déplacée ou redimensionnée
     * et que le nouveau créneau est valide.
     *
     * @param intervention intervention à modifier
     * @param nouveauDebut nouvelle date et heure de début
     * @param nouvelleFin nouvelle date et heure de fin
     * @throws IllegalArgumentException si le créneau est invalide
     *                                  ou si l'intervention est terminée ou annulée
     */
    private void verifierModificationPlanningPossible(Intervention intervention, LocalDateTime nouveauDebut, LocalDateTime nouvelleFin) {
        if (nouveauDebut == null || nouvelleFin == null) {
            throw new IllegalArgumentException("Le nouveau créneau est invalide");
        }

        if (!nouvelleFin.isAfter(nouveauDebut)) {
            throw new IllegalArgumentException("La fin doit être après le début");
        }

        if (intervention.getStatutIntervention() == StatutIntervention.TERMINEE) {
            throw new IllegalArgumentException("Impossible de modifier une intervention terminée");
        }

        if (intervention.getStatutIntervention() == StatutIntervention.ANNULEE) {
            throw new IllegalArgumentException("Impossible de modifier une intervention annulée");
        }
    }

    /**
     * Modifie simultanément le statut d'une boîte chirurgicale
     * et l'état de toutes les unités de matériel qu'elle contient.
     *
     * @param boite boîte chirurgicale concernée
     * @param statutBoite nouveau statut de la boîte
     * @param etatMateriel nouvel état à appliquer aux unités de matériel
     */
    private void changerEtatBoiteEtMateriels(BoiteChirurgicale boite,
                                             StatutBoite statutBoite,
                                             EtatMateriel etatMateriel) {
        boite.setStatut(statutBoite);

        boite.getMateriels().forEach(boiteMateriel ->
                boiteMateriel.getUniteMateriel().setEtat(etatMateriel)
        );

        boiteChirurgicaleRepository.save(boite);
    }

}