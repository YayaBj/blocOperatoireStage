package com.example.service;

import com.example.entity.BoiteChirurgicale;
import com.example.entity.BoiteMateriel;
import com.example.entity.UniteMateriel;
import com.example.entity.enums.PrioriteIntervention;
import com.example.entity.enums.StatutBoite;
import com.example.repository.BoiteChirurgicaleRepository;
import com.example.repository.BoiteMaterielRepository;
import com.example.repository.UniteMaterielRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BoiteChirurgicaleService {

    private final BoiteChirurgicaleRepository boiteChirurgicaleRepository;
    private final UniteMaterielRepository uniteMaterielRepository;
    private final BoiteMaterielRepository boiteMaterielRepository;

    public BoiteChirurgicaleService(BoiteChirurgicaleRepository boiteChirurgicaleRepository,
                                    UniteMaterielRepository uniteMaterielRepository, BoiteMaterielRepository boiteMaterielRepository) {
        this.boiteChirurgicaleRepository = boiteChirurgicaleRepository;
        this.uniteMaterielRepository = uniteMaterielRepository;
        this.boiteMaterielRepository = boiteMaterielRepository;
    }

    @Transactional
    public void createBoite(String codeBoite,
                            String nom,
                            PrioriteIntervention priorite,
                            String departement,
                            String specialite,
                            List<Long> uniteMaterielIds) {

        verifBoite(codeBoite, nom, priorite, uniteMaterielIds);

        String code = codeBoite.trim().toUpperCase();

        if (boiteChirurgicaleRepository.existsByCodeBoiteIgnoreCase(code)) {
            throw new IllegalArgumentException("Ce code de boîte existe déjà");
        }

        List<UniteMateriel> unites = uniteMaterielRepository.findAllById(uniteMaterielIds);

        List<Long> idsDistincts = uniteMaterielIds.stream()
                .distinct()
                .toList();  

        if (idsDistincts.size() != uniteMaterielIds.size()) {
            throw new IllegalArgumentException(
                    "Une unité de matériel est sélectionnée plusieurs fois"
            );
        }

        if (unites.size() != uniteMaterielIds.size()) {
            throw new IllegalArgumentException(
                    "Une ou plusieurs unités de matériel sont introuvables"
            );
        }

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

    private void verifBoite(String codeBoite, String nom, PrioriteIntervention priorite, List<Long> uniteMaterielIds) {
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

    @Transactional(readOnly = true)
    public List<BoiteMateriel> findMaterielsByBoite(BoiteChirurgicale boite) {
        BoiteChirurgicale boiteDb = boiteChirurgicaleRepository.findById(boite.getId())
                .orElseThrow(() -> new IllegalArgumentException("Boîte introuvable"));

        return new ArrayList<>(boiteDb.getMateriels());
    }

    @Transactional(readOnly = true)
    public List<BoiteChirurgicale> findAll() {
        return boiteChirurgicaleRepository.findAll();
    }

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

        verifBoite(codeBoite, nom, priorite, uniteMaterielIds);

        String code = codeBoite.trim().toUpperCase();

        BoiteChirurgicale existing = boiteChirurgicaleRepository.findByCodeBoiteIgnoreCase(code);

        if (existing != null && !existing.getId().equals(boiteDb.getId())) {
            throw new IllegalArgumentException("Ce code de boîte est déjà utilisé");
        }

        List<UniteMateriel> unites = uniteMaterielRepository.findAllById(uniteMaterielIds);

        if (unites.size() != uniteMaterielIds.size()) {
            throw new IllegalArgumentException("Une ou plusieurs unités de matériel sont introuvables");
        }

        List<Long> idsDistincts = uniteMaterielIds.stream()
                .distinct()
                .toList();

        if (idsDistincts.size() != uniteMaterielIds.size()) {
            throw new IllegalArgumentException(
                    "Une unité de matériel est sélectionnée plusieurs fois"
            );
        }

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

        if (unites.size() != uniteMaterielIds.size()) {
            throw new IllegalArgumentException("Une ou plusieurs unités de matériel sont introuvables");
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

    @Transactional
    public void deleteBoite(BoiteChirurgicale boite) {
        BoiteChirurgicale boiteDb = boiteChirurgicaleRepository.findById(boite.getId())
                .orElseThrow(() -> new IllegalArgumentException("Boîte introuvable"));

        boiteChirurgicaleRepository.delete(boiteDb);
    }
}