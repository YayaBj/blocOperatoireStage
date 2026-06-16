package com.example.service;

import com.example.entity.Machine;
import com.example.entity.enums.StatutMachine;
import com.example.entity.enums.TypeMachine;
import com.example.repository.MachineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MachineService {

    private final MachineRepository machineRepository;

    public MachineService(MachineRepository machineRepository) {
        this.machineRepository = machineRepository;
    }

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

    @Transactional
    public void deleteMachine(Machine machine) {
        Machine machineDb = findById(machine.getId());

        machineRepository.delete(machineDb);
    }

    @Transactional
    public void marquerUtilisation(Long machineId, String cycle) {
        Machine machine = findById(machineId);

        machine.setCycleEnCours(cycle);
        machine.setDerniereUtilisation(LocalDateTime.now());
        machine.setStatut(StatutMachine.ACTIVE);

        machineRepository.saveAndFlush(machine);
    }

    @Transactional(readOnly = true)
    public List<Machine> findAll() {
        return machineRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Machine> findMachinesDisponiblesParType(TypeMachine typeMachine) {
        return machineRepository.findByTypeMachine(typeMachine).stream()
                .filter(machine -> machine.getStatut() == StatutMachine.IDLE
                        || machine.getStatut() == StatutMachine.ACTIVE)
                .toList();
    }

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

    private Machine findById(Long machineId) {
        return machineRepository.findById(machineId)
                .orElseThrow(() -> new IllegalArgumentException("Machine introuvable"));
    }
}