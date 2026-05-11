package com.example.repository;

import com.example.entity.UniteMateriel;
import com.example.entity.enums.EtatMateriel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UniteMaterielRepository extends JpaRepository<UniteMateriel, Long> {

    List<UniteMateriel> findByEtat(EtatMateriel etat);
}