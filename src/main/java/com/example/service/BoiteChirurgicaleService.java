package com.example.service;

import com.example.entity.*;
import com.example.entity.enums.*;
import com.example.repository.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BoiteChirurgicaleService {

    private final BoiteChirurgicaleRepository boiteChirurgicaleRepository;
    private final UniteMaterielRepository uniteMaterielRepository;
    private final BoiteMaterielRepository boiteMaterielRepository;
    private final InterventionRepository interventionRepository;
    private final IncidentSterilisationRepository incidentSterilisationRepository;
    private final MouvementBoiteService mouvementBoiteService;

    public BoiteChirurgicaleService(
            BoiteChirurgicaleRepository boiteChirurgicaleRepository,
            UniteMaterielRepository uniteMaterielRepository,
            BoiteMaterielRepository boiteMaterielRepository,
            InterventionRepository interventionRepository,
            IncidentSterilisationRepository incidentSterilisationRepository,
            MouvementBoiteService mouvementBoiteService
    ) {
        this.boiteChirurgicaleRepository = boiteChirurgicaleRepository;
        this.uniteMaterielRepository = uniteMaterielRepository;
        this.boiteMaterielRepository = boiteMaterielRepository;
        this.interventionRepository = interventionRepository;
        this.incidentSterilisationRepository = incidentSterilisationRepository;
        this.mouvementBoiteService = mouvementBoiteService;
    }

    /**
     * Crée une nouvelle boîte chirurgicale et lui associe les unités de matériel sélectionnées.
     * Vérifie notamment l'unicité du code, l'absence de doublons et la disponibilité
     * des unités avant l'enregistrement.
     *
     * @param codeBoite code unique de la boîte
     * @param nom nom de la boîte
     * @param priorite niveau de priorité de la boîte
     * @param departement département auquel la boîte est rattachée
     * @param specialite spécialité associée à la boîte
     * @param uniteMaterielIds identifiants des unités de matériel à ajouter
     * @throws IllegalArgumentException si les données sont invalides, si le code existe déjà
     *                                  ou si une unité ne peut pas être affectée
     */
    @Transactional
    public void createBoite(String codeBoite,
                            String nom,
                            PrioriteIntervention priorite,
                            String departement,
                            String specialite,
                            List<Long> uniteMaterielIds) {

        verifierDoublonsUnites(codeBoite, nom, priorite, uniteMaterielIds);

        String code = codeBoite.trim().toUpperCase();

        if (boiteChirurgicaleRepository.existsByCodeBoiteIgnoreCase(code)) {
            throw new IllegalArgumentException("Ce code de boîte existe déjà");
        }

        verifierDoublonsUnites(uniteMaterielIds);

        List<UniteMateriel> unites = findUnites(uniteMaterielIds);

        verifierUnitesSteriles(unites);

        for (UniteMateriel unite : unites) {

            boolean dejaDansUneBoite =
                    boiteMaterielRepository.existsByUniteMaterielId(
                            unite.getId()
                    );

            if (dejaDansUneBoite) {
                throw new IllegalArgumentException(
                        "L'unité "
                                + unite.getCodeInventaire()
                                + " est déjà affectée à une boîte"
                );
            }
        }

        BoiteChirurgicale boite = new BoiteChirurgicale(
                code,
                nom.trim(),
                priorite,
                StatutBoite.ACTIVE,
                departement,
                specialite,
                LocalDateTime.now()
        );

        for (UniteMateriel unite : unites) {
            BoiteMateriel boiteMateriel = new BoiteMateriel(boite, unite);
            boite.getMateriels().add(boiteMateriel);
        }

        boiteChirurgicaleRepository.saveAndFlush(boite);
    }

    /**
     * Recherche les unités de matériel correspondant aux identifiants fournis.
     *
     * @param uniteMaterielIds identifiants des unités recherchées
     * @return la liste des unités de matériel trouvées
     * @throws IllegalArgumentException si une ou plusieurs unités sont introuvables
     */
    @NotNull
    private List<UniteMateriel> findUnites(List<Long> uniteMaterielIds) {
        List<UniteMateriel> unites = uniteMaterielRepository.findAllById(uniteMaterielIds);

        if (unites.size() != uniteMaterielIds.size()) {
            throw new IllegalArgumentException(
                    "Une ou plusieurs unités de matériel sont introuvables"
            );
        }
        return unites;
    }

    /**
     * Vérifie la présence des informations obligatoires nécessaires à la création
     * ou à la modification d'une boîte chirurgicale.
     *
     * @param codeBoite code de la boîte
     * @param nom nom de la boîte
     * @param priorite priorité de la boîte
     * @param uniteMaterielIds unités sélectionnées
     * @throws IllegalArgumentException si une information obligatoire est absente
     */
    private void verifierDoublonsUnites(String codeBoite, String nom, PrioriteIntervention priorite, List<Long> uniteMaterielIds) {
        if (codeBoite == null || codeBoite.trim().isBlank()) {
            throw new IllegalArgumentException("Le code de la boîte est obligatoire");
        }

        if (nom == null || nom.trim().isBlank()) {
            throw new IllegalArgumentException("Le nom de la boîte est obligatoire");
        }

        if (priorite == null) {
            throw new IllegalArgumentException("La priorité est obligatoire");
        }

        if (uniteMaterielIds == null || uniteMaterielIds.isEmpty()) {
            throw new IllegalArgumentException("La boîte doit contenir au moins un matériel");
        }

    }

    /**
     * Retourne les associations entre une boîte chirurgicale et les unités
     * de matériel qu'elle contient.
     *
     * @param boite boîte chirurgicale concernée
     * @return la liste des matériels associés à la boîte
     * @throws IllegalArgumentException si la boîte est introuvable
     */
    @Transactional(readOnly = true)
    public List<BoiteMateriel> findMaterielsByBoite(BoiteChirurgicale boite) {
        BoiteChirurgicale boiteDb = boiteChirurgicaleRepository.findById(boite.getId())
                .orElseThrow(() -> new IllegalArgumentException("Boîte introuvable"));

        return new ArrayList<>(boiteDb.getMateriels());
    }

    /**
     * Retourne l'ensemble des boîtes chirurgicales enregistrées.
     *
     * @return la liste de toutes les boîtes chirurgicales
     */
    @Transactional(readOnly = true)
    public List<BoiteChirurgicale> findAll() {
        return boiteChirurgicaleRepository.findAll();
    }

    /**
     * Met à jour les informations et la composition d'une boîte chirurgicale.
     * Les unités sélectionnées doivent être stériles et ne pas appartenir
     * à une autre boîte.
     *
     * @param boite boîte chirurgicale à modifier
     * @param codeBoite nouveau code de la boîte
     * @param nom nouveau nom
     * @param priorite nouvelle priorité
     * @param departement nouveau département
     * @param specialite nouvelle spécialité
     * @param uniteMaterielIds identifiants des unités composant la boîte
     * @throws IllegalArgumentException si la boîte ou une unité est invalide
     *                                  ou si le code est déjà utilisé
     */
    @Transactional
    public void updateBoite(BoiteChirurgicale boite,
                            String codeBoite,
                            String nom,
                            PrioriteIntervention priorite,
                            String departement,
                            String specialite,
                            List<Long> uniteMaterielIds) {

        if (boite == null || boite.getId() == null) {
            throw new IllegalArgumentException("Boîte introuvable");
        }

        BoiteChirurgicale boiteDb = boiteChirurgicaleRepository.findById(boite.getId())
                .orElseThrow(() -> new IllegalArgumentException("Boîte introuvable"));

        verifierDoublonsUnites(codeBoite, nom, priorite, uniteMaterielIds);

        String code = codeBoite.trim().toUpperCase();

        BoiteChirurgicale existing = boiteChirurgicaleRepository.findByCodeBoiteIgnoreCase(code);

        if (existing != null && !existing.getId().equals(boiteDb.getId())) {
            throw new IllegalArgumentException("Ce code de boîte est déjà utilisé");
        }

        verifierDoublonsUnites(uniteMaterielIds);

        List<UniteMateriel> unites = findUnites(uniteMaterielIds);

        verifierUnitesSteriles(unites);
        
        for (UniteMateriel unite : unites) {
            boolean dejaDansAutreBoite =
                    boiteMaterielRepository.existsByUniteMaterielIdAndBoiteChirurgicaleIdNot(
                            unite.getId(),
                            boiteDb.getId()
                    );

            if (dejaDansAutreBoite) {
                throw new IllegalArgumentException(
                        "L'unité " + unite.getCodeInventaire()
                                + " est déjà affectée à une autre boîte"
                );
            }
        }

        boiteDb.setCodeBoite(code);
        boiteDb.setNom(nom.trim());
        boiteDb.setPriorite(priorite);
        boiteDb.setDepartement(departement);
        boiteDb.setSpecialite(specialite);

        boiteDb.getMateriels().clear();

        for (UniteMateriel unite : unites) {
            BoiteMateriel boiteMateriel = new BoiteMateriel(boiteDb, unite);
            boiteDb.getMateriels().add(boiteMateriel);
        }

        boiteChirurgicaleRepository.saveAndFlush(boiteDb);
    }

    /**
     * Vérifie qu'une même unité de matériel n'a pas été sélectionnée plusieurs fois.
     *
     * @param uniteMaterielIds identifiants des unités sélectionnées
     * @throws IllegalArgumentException si une unité apparaît plusieurs fois
     */
    private static void verifierDoublonsUnites(List<Long> uniteMaterielIds) {
        List<Long> idsDistincts = uniteMaterielIds.stream()
                .distinct()
                .toList();

        if (idsDistincts.size() != uniteMaterielIds.size()) {
            throw new IllegalArgumentException(
                    "Une unité de matériel est sélectionnée plusieurs fois"
            );
        }
    }

    /**
     * Supprime une boîte chirurgicale lorsqu'elle n'est utilisée
     * dans aucune intervention.
     *
     * @param boite boîte chirurgicale à supprimer
     * @throws IllegalArgumentException si la boîte est introuvable
     *                                  ou si elle est utilisée dans une intervention
     */
    @Transactional
    public void deleteBoite(BoiteChirurgicale boite) {

        if (boite == null || boite.getId() == null) {
            throw new IllegalArgumentException("Boîte introuvable");
        }

        BoiteChirurgicale boiteDb = boiteChirurgicaleRepository.findById(boite.getId())
                .orElseThrow(() -> new IllegalArgumentException("Boîte introuvable"));

        if (interventionRepository.existsByInterventionBoitesBoiteChirurgicaleId(boiteDb.getId())) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer cette boîte car elle est utilisée dans une intervention"
            );
        }

        boiteChirurgicaleRepository.delete(boiteDb);
    }

    /**
     * Vérifie que toutes les unités sélectionnées sont dans l'état stérile.
     *
     * @param unites unités de matériel à contrôler
     * @throws IllegalArgumentException si au moins une unité n'est pas stérile
     */
    private void verifierUnitesSteriles(List<UniteMateriel> unites) {
        for (UniteMateriel unite : unites) {
            if (unite.getEtat() != EtatMateriel.STERILE) {
                throw new IllegalArgumentException(
                        "L'unité " + unite.getCodeInventaire()
                                + " ne peut pas être ajoutée à une boîte car elle est "
                                + unite.getEtat()
                );
            }
        }
    }

    /**
     * Retourne les unités de matériel actuellement présentes dans une boîte.
     *
     * @param boiteId identifiant de la boîte chirurgicale
     * @return la liste des unités contenues dans la boîte
     */
    @Transactional(readOnly = true)
    public List<UniteMateriel> findUnitesByBoite(Long boiteId) {
        return boiteMaterielRepository
                .findByBoiteChirurgicaleId(boiteId)
                .stream()
                .map(BoiteMateriel::getUniteMateriel)
                .toList();
    }

    /**
     * Recherche les unités pouvant remplacer le matériel concerné par un incident.
     * Seules les unités stériles, non affectées à une boîte et correspondant
     * à la même référence de matériel sont proposées.
     *
     * @param incidentId identifiant de l'incident concerné
     * @return la liste des unités disponibles pour le remplacement
     * @throws IllegalArgumentException si l'incident est invalide, introuvable
     *                                  ou si le matériel a déjà été remplacé
     */
    @Transactional(readOnly = true)
    public List<UniteMateriel> findUnitesDisponiblesPourRemplacement(
            Long incidentId
    ) {
        if (incidentId == null) {
            throw new IllegalArgumentException("L’incident est obligatoire");
        }

        IncidentSterilisation incident =
                incidentSterilisationRepository.findById(incidentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Incident introuvable")
                        );

        if (incident.getUniteRemplacement() != null) {
            throw new IllegalArgumentException(
                    "Ce matériel a déjà été remplacé"
            );
        }
        UniteMateriel uniteRetiree = incident.getUniteMateriel();

        if (uniteRetiree == null) {
            throw new IllegalArgumentException(
                    "Cet incident ne concerne pas une unité de matériel"
            );
        }

        Long materielId = uniteRetiree.getMateriel().getId();

        return uniteMaterielRepository.findAll().stream()
                .filter(unite -> unite.getEtat() == EtatMateriel.STERILE)
                .filter(unite -> unite.getMateriel() != null)
                .filter(unite ->
                        unite.getMateriel().getId().equals(materielId)
                )
                .filter(unite ->
                        !boiteMaterielRepository
                                .existsByUniteMaterielId(unite.getId())
                )
                .toList();
    }

    /**
     * Effectue un ou plusieurs remplacements de matériels manquants dans une boîte.
     * Met ensuite à jour le statut de la boîte et enregistre les mouvements associés
     * selon l'état du processus de stérilisation.
     *
     * @param boiteId identifiant de la boîte à compléter
     * @param remplacements association entre l'identifiant d'un incident
     *                      et l'identifiant de l'unité utilisée en remplacement
     * @throws IllegalArgumentException si la boîte, les incidents ou les remplacements
     *                                  fournis sont invalides
     */
    @Transactional
    public void remplacerMateriels(
            Long boiteId,
            Map<Long, Long> remplacements
    ) {
        if (boiteId == null) {
            throw new IllegalArgumentException(
                    "La boîte est obligatoire"
            );
        }

        if (remplacements == null || remplacements.isEmpty()) {
            throw new IllegalArgumentException(
                    "Aucun remplacement n’a été sélectionné"
            );
        }

        verifierRemplacements(remplacements);

        BoiteChirurgicale boite =
                boiteChirurgicaleRepository.findById(boiteId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Boîte introuvable"
                                )
                        );

        ProcessusSterilisation processusConcerne =
                trouverProcessusDesRemplacements(
                        boiteId,
                        remplacements.keySet()
                );

        for (Map.Entry<Long, Long> remplacement
                : remplacements.entrySet()) {

            remplacerMaterielInterne(
                    boite,
                    remplacement.getKey(),
                    remplacement.getValue()
            );
        }

        List<IncidentSterilisation> incidentsBoite =
                incidentSterilisationRepository
                        .findByProcessusSterilisationDemandeSterilisationBoiteChirurgicaleId(
                                boite.getId()
                        );

        boolean resteDesMaterielsAReplacer =
                incidentsBoite.stream()
                        .anyMatch(incident ->
                                incident.getUniteMateriel() != null
                                        && incident.getUniteRemplacement() == null
                        );

        enregistrerMouvementComplement(
                boite,
                processusConcerne,
                remplacements.size(),
                resteDesMaterielsAReplacer
        );

        if (resteDesMaterielsAReplacer) {
            boite.setStatut(StatutBoite.INCOMPLETE);

            boiteChirurgicaleRepository.saveAndFlush(boite);
            return;
        }

        mettreAJourStatutApresComplement(
                boite,
                processusConcerne
        );

        boiteChirurgicaleRepository.saveAndFlush(boite);
    }

    /**
     * Identifie le processus de stérilisation commun aux incidents sélectionnés
     * pour le remplacement et vérifie qu'ils concernent tous la même boîte
     * et le même processus.
     *
     * @param boiteId identifiant de la boîte concernée
     * @param incidentIds identifiants des incidents à traiter
     * @return le processus de stérilisation associé aux incidents
     * @throws IllegalArgumentException si un incident est introuvable,
     *                                  concerne une autre boîte ou un autre processus
     */
    private ProcessusSterilisation trouverProcessusDesRemplacements(
            Long boiteId,
            Set<Long> incidentIds
    ) {
        List<IncidentSterilisation> incidents =
                incidentIds.stream()
                        .map(incidentId ->
                                incidentSterilisationRepository
                                        .findById(incidentId)
                                        .orElseThrow(() ->
                                                new IllegalArgumentException(
                                                        "Incident introuvable : "
                                                                + incidentId
                                                )
                                        )
                        )
                        .toList();

        for (IncidentSterilisation incident : incidents) {
            BoiteChirurgicale boiteIncident =
                    incident.getProcessusSterilisation()
                            .getDemandeSterilisation()
                            .getBoiteChirurgicale();

            if (!boiteId.equals(boiteIncident.getId())) {
                throw new IllegalArgumentException(
                        "Un incident ne concerne pas la boîte sélectionnée"
                );
            }
        }

        List<Long> processusIds =
                incidents.stream()
                        .map(IncidentSterilisation::getProcessusSterilisation)
                        .map(ProcessusSterilisation::getId)
                        .distinct()
                        .toList();

        if (processusIds.size() != 1) {
            throw new IllegalArgumentException(
                    "Les remplacements doivent concerner un même processus"
            );
        }

        return incidents.getFirst()
                .getProcessusSterilisation();
    }

    /**
     * Détermine le nouveau statut d'une boîte après son complètement,
     * en fonction de l'état du processus de stérilisation associé.
     *
     * @param boite boîte chirurgicale complétée
     * @param processus processus de stérilisation concerné
     */
    private void mettreAJourStatutApresComplement(
            BoiteChirurgicale boite,
            ProcessusSterilisation processus
    ) {
        if (processus.getStatut()
                == StatutProcessusSterilisation.TERMINE) {

            boite.setStatut(
                    StatutBoite.EN_STOCK_STERILE
            );

            mouvementBoiteService.enregistrerMouvement(
                    boite.getId(),
                    processus.getId(),
                    ZoneBoite.QUARANTAINE,
                    ZoneBoite.STOCK_STERILE,
                    TypeMouvementBoite.RETOUR_STOCK_STERILE,
                    "Boîte complétée après incident et remise en stock stérile"
            );

            return;
        }

        if (processus.getStatut() == StatutProcessusSterilisation.ECHEC) {
            boite.setStatut(
                    StatutBoite.INCIDENT
            );

            return;
        }

        boite.setStatut(
                StatutBoite.EN_STERILISATION
        );
    }

    /**
     * Enregistre dans l'historique des mouvements le complètement d'une boîte.
     * Le commentaire généré dépend du nombre de remplacements réalisés
     * et de l'état restant de la boîte ou du processus.
     *
     * @param boite boîte chirurgicale concernée
     * @param processus processus de stérilisation associé
     * @param nombreRemplacements nombre de matériels remplacés
     * @param remplacementEncoreNecessaire indique si des matériels restent à remplacer
     */
    private void enregistrerMouvementComplement(
            BoiteChirurgicale boite,
            ProcessusSterilisation processus,
            int nombreRemplacements,
            boolean remplacementEncoreNecessaire
    ) {
        String commentaire;

        if (remplacementEncoreNecessaire) {
            commentaire =
                    nombreRemplacements
                            + " matériel(s) remplacé(s). "
                            + "La boîte reste incomplète.";
        } else if (processus.getStatut()
                == StatutProcessusSterilisation.ECHEC) {

            commentaire =
                    nombreRemplacements
                            + " matériel(s) remplacé(s). "
                            + "La boîte est complète, mais un nouveau cycle "
                            + "de stérilisation est obligatoire.";
        } else {
            commentaire =
                    nombreRemplacements
                            + " matériel(s) remplacé(s). "
                            + "La boîte est maintenant complète.";
        }

        mouvementBoiteService.enregistrerMouvement(
                boite.getId(),
                processus.getId(),
                ZoneBoite.QUARANTAINE,
                ZoneBoite.QUARANTAINE,
                TypeMouvementBoite.COMPLEMENT_BOITE,
                commentaire
        );
    }

    /**
     * Vérifie la validité d'un ensemble de remplacements de matériel.
     * Contrôle notamment la présence des identifiants et empêche l'utilisation
     * d'une même unité pour remplacer plusieurs matériels.
     *
     * @param remplacements remplacements à vérifier
     * @throws IllegalArgumentException si un incident ou une unité est invalide
     *                                  ou si une unité est utilisée plusieurs fois
     */
    private void verifierRemplacements(
            Map<Long, Long> remplacements
    ) {
        if (remplacements.containsKey(null)) {
            throw new IllegalArgumentException(
                    "Un incident de remplacement est invalide"
            );
        }

        if (remplacements.values().stream()
                .anyMatch(Objects::isNull)) {

            throw new IllegalArgumentException(
                    "Une unité de remplacement est manquante"
            );
        }

        Set<Long> unitesDistinctes =
                new HashSet<>(remplacements.values());

        if (unitesDistinctes.size() != remplacements.size()) {
            throw new IllegalArgumentException(
                    "Une même unité ne peut pas remplacer plusieurs matériels"
            );
        }
    }

    /**
     * Effectue le remplacement d'une unité de matériel pour un incident donné.
     * Vérifie que l'unité de remplacement est stérile, disponible et correspond
     * à la même référence de matériel que l'unité retirée.
     *
     * @param boite boîte chirurgicale à compléter
     * @param incidentId identifiant de l'incident ayant entraîné le remplacement
     * @param nouvelleUniteId identifiant de l'unité utilisée en remplacement
     * @throws IllegalArgumentException si l'incident ou l'unité est invalide,
     *                                  si l'unité est déjà affectée ou si elle ne
     *                                  correspond pas au matériel attendu
     */
    private void remplacerMaterielInterne(
            BoiteChirurgicale boite,
            Long incidentId,
            Long nouvelleUniteId
    ) {
        IncidentSterilisation incident =
                incidentSterilisationRepository.findById(incidentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Incident introuvable"
                                )
                        );

        UniteMateriel uniteRetiree =
                incident.getUniteMateriel();

        if (uniteRetiree == null) {
            throw new IllegalArgumentException(
                    "Cet incident ne concerne pas un matériel"
            );
        }

        Long boiteIncidentId = incident
                .getProcessusSterilisation()
                .getDemandeSterilisation()
                .getBoiteChirurgicale()
                .getId();

        if (!boite.getId().equals(boiteIncidentId)) {
            throw new IllegalArgumentException(
                    "Cet incident ne concerne pas cette boîte"
            );
        }

        if (incident.getUniteRemplacement() != null) {
            throw new IllegalArgumentException(
                    "Ce matériel a déjà été remplacé"
            );
        }

        UniteMateriel nouvelleUnite =
                uniteMaterielRepository.findById(nouvelleUniteId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Unité de remplacement introuvable"
                                )
                        );

        if (nouvelleUnite.getEtat() != EtatMateriel.STERILE) {
            throw new IllegalArgumentException(
                    "L’unité de remplacement doit être stérile"
            );
        }

        if (boiteMaterielRepository.existsByUniteMaterielId(
                nouvelleUnite.getId()
        )) {
            throw new IllegalArgumentException(
                    "L’unité "
                            + nouvelleUnite.getCodeInventaire()
                            + " est déjà affectée à une boîte"
            );
        }

        if (uniteRetiree.getMateriel() == null
                || nouvelleUnite.getMateriel() == null) {

            throw new IllegalArgumentException(
                    "Le matériel associé à une unité est introuvable"
            );
        }

        Long ancienMaterielId =
                uniteRetiree.getMateriel().getId();

        Long nouveauMaterielId =
                nouvelleUnite.getMateriel().getId();

        if (!ancienMaterielId.equals(nouveauMaterielId)) {
            throw new IllegalArgumentException(
                    "L’unité de remplacement doit correspondre au même matériel"
            );
        }

        BoiteMateriel nouvelleAssociation =
                new BoiteMateriel(boite, nouvelleUnite);

        boite.getMateriels().add(nouvelleAssociation);
        boiteMaterielRepository.save(nouvelleAssociation);

        incident.setUniteRemplacement(nouvelleUnite);
        incidentSterilisationRepository.save(incident);
    }
}