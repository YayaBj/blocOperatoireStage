package com.example.service;

import com.example.entity.UniteMateriel;
import com.example.entity.enums.EtatMateriel;
import com.example.repository.UniteMaterielRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UniteMaterielService {

    private final UniteMaterielRepository uniteMaterielRepository;

    public UniteMaterielService(UniteMaterielRepository uniteMaterielRepository) {
        this.uniteMaterielRepository = uniteMaterielRepository;
    }

    /**
     * Retourne l'ensemble des unités de matériel enregistrées.
     *
     * @return la liste de toutes les unités de matériel
     */
    @Transactional(readOnly = true)
    public List<UniteMateriel> findAll() {
        return uniteMaterielRepository.findAll();
    }
}