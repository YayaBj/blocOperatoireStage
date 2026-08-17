package com.example.service;

import com.example.entity.*;
import com.example.entity.enums.*;
import com.example.repository.*;
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
    private final UniteMaterielRepository uniteMaterielRepository;
    private final BoiteChirurgicaleRepository boiteChirurgicaleRepository;
    private final BoiteMaterielRepository boiteMaterielRepository;

    public IncidentSterilisationService(
            IncidentSterilisationRepository incidentRepository,
            ProcessusSterilisationRepository processusRepository,
            MachineRepository machineRepository,
            UniteMaterielRepository uniteMaterielRepository,
            BoiteChirurgicaleRepository boiteChirurgicaleRepository,
            BoiteMaterielRepository boiteMaterielRepository
    ) {
        this.incidentRepository = incidentRepository;
        this.processusRepository = processusRepository;
        this.machineRepository = machineRepository;
        this.uniteMaterielRepository = uniteMaterielRepository;
        this.boiteChirurgicaleRepository = boiteChirurgicaleRepository;
        this.boiteMaterielRepository = boiteMaterielRepository;
    }

    /**
     * Déclare un incident concernant une unité de matériel présente dans la boîte
     * associée au processus de stérilisation.
     * Met à jour l'état de l'unité, la retire de la boîte et marque celle-ci
     * comme incomplète. Le processus peut également être placé en échec.
     *
     * @param processusId identifiant du processus de stérilisation concerné
     * @param uniteMaterielId identifiant de l'unité de matériel concernée
     * @param typeIncident type de l'incident matériel
     * @param gravite niveau de gravité de l'incident
     * @param description description de l'incident
     * @param arreterProcessus indique si le processus doit être placé en échec
     * @throws IllegalArgumentException si les données sont invalides,
     *                                  si le processus ou l'unité est introuvable,
     *                                  si le type n'est pas un incident matériel
     *                                  ou si l'unité n'appartient pas à la boîte
     */
    @Transactional
    public void declarerIncidentMateriel(
            Long processusId,
            Long uniteMaterielId,
            TypeIncidentSterilisation typeIncident,
            GraviteIncident gravite,
            String description,
            boolean arreterProcessus
    ) {
        verifierDonneesIncident(
                processusId,
                typeIncident,
                gravite,
                description
        );

        if (uniteMaterielId == null) {
            throw new IllegalArgumentException(
                    "L’unité de matériel concernée est obligatoire"
            );
        }

        if (!estUnIncidentMateriel(typeIncident)) {
            throw new IllegalArgumentException(
                    "Le type sélectionné n’est pas un incident matériel"
            );
        }

        ProcessusSterilisation processus = findProcessus(processusId);

        UniteMateriel unite = uniteMaterielRepository.findById(uniteMaterielId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unité de matériel introuvable"
                        )
                );

        BoiteChirurgicale boite = processus
                .getDemandeSterilisation()
                .getBoiteChirurgicale();

        BoiteMateriel association =
                boiteMaterielRepository
                        .findByBoiteChirurgicaleIdAndUniteMaterielId(
                                boite.getId(),
                                unite.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Cette unité n’appartient pas à la boîte du processus"
                                )
                        );

        EtatMateriel nouvelEtat =
                determinerNouvelEtatMateriel(typeIncident);

        unite.setEtat(nouvelEtat);
        uniteMaterielRepository.save(unite);

        boiteMaterielRepository.delete(association);

        boite.setStatut(StatutBoite.INCOMPLETE);
        boiteChirurgicaleRepository.save(boite);

        if (arreterProcessus) {
            processus.setStatut(StatutProcessusSterilisation.ECHEC);
            processusRepository.save(processus);
        }

        IncidentSterilisation incident = new IncidentSterilisation(
                LocalDateTime.now(),
                typeIncident,
                gravite,
                description.trim(),
                processus,
                null,
                unite
        );

        incidentRepository.saveAndFlush(incident);
    }

    /**
     * Indique si un type d'incident correspond à un incident concernant
     * directement une unité de matériel.
     *
     * @param type type d'incident à vérifier
     * @return true si le type correspond à un incident matériel, false sinon
     */
    private boolean estUnIncidentMateriel(
            TypeIncidentSterilisation type
    ) {
        return type == TypeIncidentSterilisation.MATERIEL_MANQUANT
                || type == TypeIncidentSterilisation.MATERIEL_CASSE
                || type == TypeIncidentSterilisation.MATERIEL_ENDOMMAGE
                || type == TypeIncidentSterilisation.MATERIEL_PERDU;
    }

    /**
     * Détermine le nouvel état d'une unité de matériel en fonction
     * du type d'incident déclaré.
     *
     * @param type type d'incident matériel
     * @return le nouvel état à appliquer à l'unité de matériel
     * @throws IllegalArgumentException si le type fourni ne correspond pas
     *                                  à un incident matériel
     */
    private EtatMateriel determinerNouvelEtatMateriel(
            TypeIncidentSterilisation type
    ) {
        return switch (type) {
            case MATERIEL_CASSE ->
                    EtatMateriel.HS;

            case MATERIEL_ENDOMMAGE ->
                    EtatMateriel.ENDOMMAGE;

            case MATERIEL_PERDU ->
                    EtatMateriel.PERDU;

            case MATERIEL_MANQUANT ->
                    EtatMateriel.INDISPONIBLE;

            default -> throw new IllegalArgumentException(
                    "Ce type ne correspond pas à un incident matériel"
            );
        };
    }

    /**
     * Déclare un incident général lié au processus de stérilisation,
     * éventuellement associé à une machine.
     * Le processus concerné est automatiquement placé à l'état ECHEC.
     *
     * @param processusId identifiant du processus concerné
     * @param machineId identifiant éventuel de la machine concernée
     * @param typeIncident type de l'incident déclaré
     * @param gravite niveau de gravité de l'incident
     * @param description description de l'incident
     * @throws IllegalArgumentException si les données sont invalides,
     *                                  si le processus ou la machine est introuvable
     *                                  ou si le type correspond à un incident matériel
     */
    @Transactional
    public void declarerIncidentProcessus(
            Long processusId,
            Long machineId,
            TypeIncidentSterilisation typeIncident,
            GraviteIncident gravite,
            String description
    ) {
        verifierDonneesIncident(
                processusId,
                typeIncident,
                gravite,
                description
        );

        if (estUnIncidentMateriel(typeIncident)) {
            throw new IllegalArgumentException(
                    "Utilisez la déclaration d’incident matériel pour ce type"
            );
        }

        ProcessusSterilisation processus = findProcessus(processusId);
        processus.setStatut(StatutProcessusSterilisation.ECHEC);
        processusRepository.save(processus);
        Machine machine = findMachineIfPresent(machineId);

        IncidentSterilisation incident = new IncidentSterilisation(
                LocalDateTime.now(),
                typeIncident,
                gravite,
                description.trim(),
                processus,
                machine,
                null
        );

        incidentRepository.saveAndFlush(incident);
    }

    /**
     * Recherche une machine lorsqu'un identifiant est fourni.
     *
     * @param machineId identifiant éventuel de la machine
     * @return la machine correspondante ou null si aucun identifiant n'est fourni
     * @throws IllegalArgumentException si l'identifiant fourni ne correspond
     *                                  à aucune machine
     */
    @Nullable
    private Machine findMachineIfPresent(Long machineId) {
        if (machineId == null) {
            return null;
        }

        return machineRepository.findById(machineId)
                .orElseThrow(() -> new IllegalArgumentException("Machine introuvable"));
    }

    /**
     * Recherche un processus de stérilisation à partir de son identifiant.
     *
     * @param processusId identifiant du processus recherché
     * @return le processus de stérilisation correspondant
     * @throws IllegalArgumentException si le processus est introuvable
     */
    private ProcessusSterilisation findProcessus(Long processusId) {
        return processusRepository.findById(processusId)
                .orElseThrow(() -> new IllegalArgumentException("Processus introuvable"));
    }

    /**
     * Vérifie la présence et la validité des informations obligatoires
     * nécessaires à la déclaration d'un incident.
     *
     * @param processusId identifiant du processus concerné
     * @param typeIncident type de l'incident
     * @param gravite niveau de gravité
     * @param description description de l'incident
     * @throws IllegalArgumentException si une information obligatoire est absente
     */
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

    /**
     * Retourne l'ensemble des incidents de stérilisation enregistrés.
     *
     * @return la liste de tous les incidents de stérilisation
     */
    @Transactional(readOnly = true)
    public List<IncidentSterilisation> findAll() {
        return incidentRepository.findAll();
    }

    /**
     * Retourne les incidents associés à un processus de stérilisation.
     *
     * @param processusId identifiant du processus concerné
     * @return la liste des incidents associés au processus
     */
    @Transactional(readOnly = true)
    public List<IncidentSterilisation> findByProcessus(Long processusId) {
        return incidentRepository.findByProcessusSterilisationId(processusId);
    }

    /**
     * Recherche les incidents matériels d'une boîte pour lesquels
     * aucune unité de remplacement n'a encore été enregistrée.
     *
     * @param boiteId identifiant de la boîte chirurgicale concernée
     * @return la liste des incidents matériels nécessitant encore un remplacement
     * @throws IllegalArgumentException si l'identifiant de la boîte n'est pas fourni
     */
    @Transactional(readOnly = true)
    public List<IncidentSterilisation> findIncidentsNonRemplacesByBoite(Long boiteId) {

        if (boiteId == null) {
            throw new IllegalArgumentException("La boîte est obligatoire");
        }

        return incidentRepository
                .findByProcessusSterilisationDemandeSterilisationBoiteChirurgicaleId(boiteId)
                .stream()
                .filter(incident -> incident.getUniteMateriel() != null)
                .filter(incident -> incident.getUniteRemplacement() == null)
                .toList();
    }
}