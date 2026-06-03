package com.example.service;

import com.example.entity.UniteMateriel;
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

    @Transactional(readOnly = true)
    public List<UniteMateriel> findAll() {
        return uniteMaterielRepository.findAll();
    }
}