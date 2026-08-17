package com.example.service;

import com.example.entity.Machine;
import com.example.entity.enums.StatutMachine;
import com.example.entity.enums.TypeMachine;
import com.example.repository.MachineRepository;
import com.example.repository.ProcessusSterilisationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MachineService {

    private final MachineRepository machineRepository;
    private final ProcessusSterilisationRepository processusSterilisationRepository;

    public MachineService(MachineRepository machineRepository,
                          ProcessusSterilisationRepository processusSterilisationRepository) {
        this.machineRepository = machineRepository;
        this.processusSterilisationRepository = processusSterilisationRepository;
    }

    /**
     * Crée une nouvelle machine utilisée dans le processus de stérilisation.
     * Vérifie la validité des informations fournies ainsi que l'unicité
     * du nom avant l'enregistrement.
     *
     * @param nom nom de la machine
     * @param typeMachine type de la machine
     * @param tempsProcessusMinutes durée du processus en minutes
     * @param cycleEnCours cycle éventuellement associé à la machine
     * @param statut statut initial de la machine
     * @throws IllegalArgumentException si les données sont invalides
     *                                  ou si une machine portant ce nom existe déjà
     */
    @Transactional
    public void createMachine(String nom,
                              TypeMachine typeMachine,
                              int tempsProcessusMinutes,
                              String cycleEnCours,
                              StatutMachine statut) {

        verifierMachine(nom, typeMachine, tempsProcessusMinutes, statut);

        if (machineRepository.existsByNomIgnoreCase(nom.trim())) {
            throw new IllegalArgumentException("Cette machine existe déjà");
        }

        Machine machine = new Machine(
                nom.trim(),
                typeMachine,
                tempsProcessusMinutes,
                cycleEnCours,
                statut
        );

        machineRepository.saveAndFlush(machine);
    }

    /**
     * Modifie les informations d'une machine existante.
     * Vérifie les nouvelles données ainsi que l'unicité du nom de la machine.
     *
     * @param machine machine à modifier
     * @param nom nouveau nom de la machine
     * @param typeMachine nouveau type de la machine
     * @param tempsProcessusMinutes nouvelle durée du processus en minutes
     * @param cycleEnCours cycle actuellement associé à la machine
     * @param statut nouveau statut de la machine
     * @throws IllegalArgumentException si la machine est introuvable,
     *                                  si les données sont invalides
     *                                  ou si le nom est déjà utilisé
     */
    @Transactional
    public void updateMachine(Machine machine,
                              String nom,
                              TypeMachine typeMachine,
                              int tempsProcessusMinutes,
                              String cycleEnCours,
                              StatutMachine statut) {

        verifierMachine(nom, typeMachine, tempsProcessusMinutes, statut);

        Machine machineDb = findById(machine.getId());

        Machine existing = machineRepository.findByNomIgnoreCase(nom.trim());

        if (existing != null && !existing.getId().equals(machineDb.getId())) {
            throw new IllegalArgumentException("Ce nom de machine est déjà utilisé");
        }

        machineDb.setNom(nom.trim());
        machineDb.setTypeMachine(typeMachine);
        machineDb.setTempsProcessusMinutes(tempsProcessusMinutes);
        machineDb.setCycleEnCours(cycleEnCours);
        machineDb.setStatut(statut);

        machineRepository.saveAndFlush(machineDb);
    }

    /**
     * Supprime une machine si celle-ci n'est référencée par aucun
     * processus de stérilisation.
     *
     * @param machine machine à supprimer
     * @throws IllegalArgumentException si la machine est introuvable
     *                                  ou si elle est utilisée dans un processus
     *                                  de stérilisation
     */
    @Transactional
    public void deleteMachine(Machine machine) {
        if (machine == null || machine.getId() == null) {
            throw new IllegalArgumentException("Machine introuvable");
        }

        Machine machineDb = findById(machine.getId());

        if (processusSterilisationRepository.existsByMachineLavageIdOrMachineAutoclaveId(
                machineDb.getId(),
                machineDb.getId()
        )) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer cette machine car elle est référencée dans un ou plusieurs processus de stérilisation"
            );
        }

        machineRepository.delete(machineDb);
    }

    /**
     * Enregistre l'utilisation d'une machine pour un cycle donné.
     * Met à jour son cycle en cours, sa date de dernière utilisation
     * et son statut.
     *
     * @param machineId identifiant de la machine utilisée
     * @param cycle cycle associé à l'utilisation
     * @throws IllegalArgumentException si la machine est introuvable
     */
    @Transactional
    public void marquerUtilisation(Long machineId, String cycle) {
        Machine machine = findById(machineId);

        machine.setCycleEnCours(cycle);
        machine.setDerniereUtilisation(LocalDateTime.now());
        machine.setStatut(StatutMachine.ACTIVE);

        machineRepository.saveAndFlush(machine);
    }

    /**
     * Retourne l'ensemble des machines enregistrées.
     *
     * @return la liste de toutes les machines
     */
    @Transactional(readOnly = true)
    public List<Machine> findAll() {
        return machineRepository.findAll();
    }

    /**
     * Recherche les machines disponibles correspondant à un type donné.
     * Seules les machines ayant le statut IDLE ou ACTIVE sont retournées.
     *
     * @param typeMachine type de machine recherché
     * @return la liste des machines disponibles correspondant au type demandé
     */
    @Transactional(readOnly = true)
    public List<Machine> findMachinesDisponiblesParType(TypeMachine typeMachine) {
        return machineRepository.findByTypeMachine(typeMachine).stream()
                .filter(machine -> machine.getStatut() == StatutMachine.IDLE
                        || machine.getStatut() == StatutMachine.ACTIVE)
                .toList();
    }

    /**
     * Vérifie la validité des informations nécessaires à la création
     * ou à la modification d'une machine.
     *
     * @param nom nom de la machine
     * @param typeMachine type de la machine
     * @param tempsProcessusMinutes durée du processus en minutes
     * @param statut statut de la machine
     * @throws IllegalArgumentException si une information obligatoire est absente
     *                                  ou si la durée du processus est négative
     */
    private void verifierMachine(String nom,
                                 TypeMachine typeMachine,
                                 int tempsProcessusMinutes,
                                 StatutMachine statut) {

        if (nom == null || nom.trim().isBlank()) {
            throw new IllegalArgumentException("Le nom de la machine est obligatoire");
        }

        if (typeMachine == null) {
            throw new IllegalArgumentException("Le type de machine est obligatoire");
        }

        if (tempsProcessusMinutes < 0) {
            throw new IllegalArgumentException("Le temps de processus ne peut pas être négatif");
        }

        if (statut == null) {
            throw new IllegalArgumentException("Le statut de la machine est obligatoire");
        }
    }

    /**
     * Recherche une machine à partir de son identifiant.
     *
     * @param machineId identifiant de la machine recherchée
     * @return la machine correspondante
     * @throws IllegalArgumentException si la machine est introuvable
     */
    private Machine findById(Long machineId) {
        return machineRepository.findById(machineId)
                .orElseThrow(() -> new IllegalArgumentException("Machine introuvable"));
    }
}