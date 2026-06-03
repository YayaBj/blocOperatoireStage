package com.example.repository;

import com.example.entity.Machine;
import com.example.entity.enums.StatutMachine;
import com.example.entity.enums.TypeMachine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MachineRepository extends JpaRepository<Machine, Long> {

    boolean existsByNomIgnoreCase(String nom);

    List<Machine> findByTypeMachine(TypeMachine typeMachine);

    List<Machine> findByStatut(StatutMachine statut);
}