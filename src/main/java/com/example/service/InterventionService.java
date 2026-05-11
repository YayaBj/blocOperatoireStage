package com.example.service;

import com.example.entity.*;
import com.example.entity.enums.*;
import com.example.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class InterventionService {

    private final InterventionRepository interventionRepository;
    private final PatientRepository patientRepository;
    private final SalleRepository salleRepository;
    private final PersonnelRepository personnelRepository;
    private final UniteMaterielRepository uniteMaterielRepository;
    private final AffectationPersonnelRepository affectationPersonnelRepository;
    private final InterventionMaterielRepository interventionMaterielRepository;
    private final SterilisationRepository sterilisationRepository;

    public InterventionService(
            InterventionRepository interventionRepository,
            PatientRepository patientRepository,
            SalleRepository salleRepository,
            PersonnelRepository personnelRepository,
            UniteMaterielRepository uniteMaterielRepository,
            AffectationPersonnelRepository affectationPersonnelRepository,
            InterventionMaterielRepository interventionMaterielRepository,
            SterilisationRepository sterilisationRepository
    ) {
        this.interventionRepository = interventionRepository;
        this.patientRepository = patientRepository;
        this.salleRepository = salleRepository;
        this.personnelRepository = personnelRepository;
        this.uniteMaterielRepository = uniteMaterielRepository;
        this.affectationPersonnelRepository = affectationPersonnelRepository;
        this.interventionMaterielRepository = interventionMaterielRepository;
        this.sterilisationRepository = sterilisationRepository;
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
            List<Long> uniteMaterielIds
    ) {
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

        LocalDateTime dateHeureFin = dateHeureDebut.plusMinutes(dureePrevue);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient introuvable"));

        Salle salle = salleRepository.findById(salleId)
                .orElseThrow(() -> new IllegalArgumentException("Salle introuvable"));

        if (salle.getStatutSalle() != StatutSalle.DISPONIBLE) {
            throw new IllegalArgumentException("La salle sélectionnée n’est pas disponible");
        }

        verifierDisponibilitePatient(patientId, dateHeureDebut, dateHeureFin);
        verifierDisponibiliteSalle(salleId, dateHeureDebut, dateHeureFin);
        verifierDisponibilitePersonnel(personnelsAvecRoles, dateHeureDebut, dateHeureFin);
        verifierDisponibiliteMateriel(uniteMaterielIds, dateHeureDebut, dateHeureFin);

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

        List<UniteMateriel> unites = uniteMaterielRepository.findAllById(uniteMaterielIds);

        if (unites.size() != uniteMaterielIds.size()) {
            throw new IllegalArgumentException("Une ou plusieurs unités de matériel sont introuvables");
        }

        for (UniteMateriel unite : unites) {
            InterventionMateriel interventionMateriel = new InterventionMateriel(intervention, unite);
            interventionMaterielRepository.save(interventionMateriel);
        }
    }

    @Transactional
    public void terminerIntervention(Long interventionId) {
        Intervention intervention = interventionRepository.findById(interventionId)
                .orElseThrow(() -> new IllegalArgumentException("Intervention introuvable"));

        if (intervention.getStatutIntervention() == StatutIntervention.TERMINEE) {
            throw new IllegalArgumentException("Cette intervention est déjà terminée");
        }

        intervention.setStatutIntervention(StatutIntervention.TERMINEE);

        List<InterventionMateriel> materielsUtilises =
                interventionMaterielRepository.findByInterventionId(interventionId);

        for (InterventionMateriel interventionMateriel : materielsUtilises) {
            UniteMateriel unite = interventionMateriel.getUniteMateriel();

            unite.setEtat(EtatMateriel.EN_STERILISATION);

            Sterilisation sterilisation = new Sterilisation(
                    LocalDate.now(),
                    StatutSterilisation.EN_COURS,
                    unite
            );

            decrementerStockDisponible(unite);

            sterilisationRepository.save(sterilisation);
            uniteMaterielRepository.save(unite);
        }

        interventionRepository.saveAndFlush(intervention);
    }

    @Transactional(readOnly = true)
    public List<Intervention> findAll() {
        return interventionRepository.findAll();
    }

    private void verifierDisponibilitePatient(Long patientId, LocalDateTime debut, LocalDateTime fin) {
        List<Intervention> interventions = interventionRepository.findByPatientIdAndStatutInterventionIn(
                patientId,
                statutsBloquants()
        );

        boolean conflit = interventions.stream()
                .anyMatch(intervention -> chevauche(debut, fin, intervention));

        if (conflit) {
            throw new IllegalArgumentException("Le patient a déjà une intervention sur ce créneau");
        }
    }

    private void verifierDisponibiliteSalle(Long salleId, LocalDateTime debut, LocalDateTime fin) {
        List<Intervention> interventions = interventionRepository.findBySalleIdAndStatutInterventionIn(
                salleId,
                statutsBloquants()
        );

        boolean conflit = interventions.stream()
                .anyMatch(intervention -> chevauche(debut, fin, intervention));

        if (conflit) {
            throw new IllegalArgumentException("La salle est déjà occupée sur ce créneau");
        }
    }

    private void verifierDisponibilitePersonnel(
            Map<Long, RoleIntervention> personnelsAvecRoles,
            LocalDateTime debut,
            LocalDateTime fin
    ) {
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
                .anyMatch(affectation -> chevauche(debut, fin, affectation.getIntervention()));

        if (conflit) {
            throw new IllegalArgumentException("Un membre du personnel est déjà affecté sur ce créneau");
        }
    }

    private void verifierDisponibiliteMateriel(
            List<Long> uniteMaterielIds,
            LocalDateTime debut,
            LocalDateTime fin
    ) {
        if (uniteMaterielIds == null || uniteMaterielIds.isEmpty()) {
            throw new IllegalArgumentException("Au moins une unité de matériel doit être sélectionnée");
        }

        List<Long> idsDistincts = uniteMaterielIds.stream().distinct().toList();

        if (idsDistincts.size() != uniteMaterielIds.size()) {
            throw new IllegalArgumentException("Une unité de matériel est sélectionnée plusieurs fois");
        }

        List<UniteMateriel> unites = uniteMaterielRepository.findAllById(uniteMaterielIds);

        if (unites.size() != uniteMaterielIds.size()) {
            throw new IllegalArgumentException("Une ou plusieurs unités de matériel sont introuvables");
        }

        for (UniteMateriel unite : unites) {
            if (unite.getEtat() != EtatMateriel.STERILE) {
                throw new IllegalArgumentException(
                        "L’unité " + unite.getCodeInventaire() + " n’est pas stérile"
                );
            }
        }

        List<InterventionMateriel> materielsUtilises =
                interventionMaterielRepository.findByUniteMaterielIdInAndInterventionStatutInterventionIn(
                        uniteMaterielIds,
                        statutsBloquants()
                );

        boolean conflit = materielsUtilises.stream()
                .anyMatch(interventionMateriel -> chevauche(debut, fin, interventionMateriel.getIntervention()));

        if (conflit) {
            throw new IllegalArgumentException("Une unité de matériel est déjà réservée sur ce créneau");
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

    private void decrementerStockDisponible(UniteMateriel unite) {
        Materiel materiel = unite.getMateriel();

        if (materiel != null && materiel.getStock() != null) {
            int disponible = materiel.getStock().getQuantiteDisponible();

            if (disponible > 0) {
                materiel.getStock().setQuantiteDisponible(disponible - 1);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<UniteMateriel> findUnitesSteriles() {
        return uniteMaterielRepository.findByEtat(EtatMateriel.STERILE);
    }
}