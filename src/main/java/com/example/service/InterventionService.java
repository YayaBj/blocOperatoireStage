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

    private Patient findPatient(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient introuvable"));
    }

    private Salle findSalleDisponible(Long salleId) {
        Salle salle = salleRepository.findById(salleId)
                .orElseThrow(() -> new IllegalArgumentException("Salle introuvable"));

        if (salle.getStatutSalle() != StatutSalle.DISPONIBLE) {
            throw new IllegalArgumentException("La salle sélectionnée n’est pas disponible");
        }

        return salle;
    }

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

    @Transactional(readOnly = true)
    public List<Intervention> findAll() {
        return interventionRepository.findAll();
    }

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

    private boolean chevauche(LocalDateTime nouveauDebut, LocalDateTime nouveauFin, Intervention existante) {
        LocalDateTime ancienDebut = existante.getDateHeureDebut();
        LocalDateTime ancienFin = ancienDebut.plusMinutes(existante.getDureePrevue());

        return nouveauDebut.isBefore(ancienFin) && nouveauFin.isAfter(ancienDebut);
    }

    private List<StatutIntervention> statutsBloquants() {
        return List.of(
                StatutIntervention.PLANIFIEE,
                StatutIntervention.EN_COURS
        );
    }

    @Transactional(readOnly = true)
    public List<BoiteChirurgicale> findBoitesDisponibles() {
        return boiteChirurgicaleRepository.findAll().stream()
                .filter(boite ->
                        boite.getStatut() == StatutBoite.EN_STOCK_STERILE
                                || boite.getStatut() == StatutBoite.ACTIVE
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public Intervention findById(Long id) {
        return interventionRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Intervention introuvable"));
    }

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


    @Transactional
    public void deleteIntervention(Long id) {
        Intervention intervention = findById(id);
        interventionRepository.delete(intervention);
    }

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

    @Transactional
    public void redimensionnerIntervention(Long interventionId, LocalDateTime nouveauDebut, LocalDateTime nouvelleFin) {
        deplacerIntervention(interventionId, nouveauDebut, nouvelleFin);
    }

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