package com.example.service;

import com.example.entity.Materiel;
import com.example.entity.Sterilisation;
import com.example.entity.UniteMateriel;
import com.example.entity.enums.EtatMateriel;
import com.example.entity.enums.StatutSterilisation;
import com.example.repository.SterilisationRepository;
import com.example.repository.UniteMaterielRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SterilisationService {

    private final SterilisationRepository sterilisationRepository;
    private final UniteMaterielRepository uniteMaterielRepository;

    public SterilisationService(SterilisationRepository sterilisationRepository,
                                UniteMaterielRepository uniteMaterielRepository) {
        this.sterilisationRepository = sterilisationRepository;
        this.uniteMaterielRepository = uniteMaterielRepository;
    }

    @Transactional(readOnly = true)
    public List<Sterilisation> findSterilisationsEnCours() {
        return sterilisationRepository.findByStatut(StatutSterilisation.EN_COURS);
    }

    @Transactional
    public void validerSterilisation(Sterilisation sterilisation) {
        Sterilisation sterilisationDb = sterilisationRepository.findById(sterilisation.getId())
                .orElseThrow(() -> new IllegalArgumentException("Stérilisation introuvable"));

        UniteMateriel unite = sterilisationDb.getUniteMateriel();

        sterilisationDb.setStatut(StatutSterilisation.TERMINE);
        sterilisationDb.setDateFin(LocalDate.now());

        unite.setEtat(EtatMateriel.STERILE);

        incrementerStockDisponible(unite);

        uniteMaterielRepository.save(unite);
        sterilisationRepository.saveAndFlush(sterilisationDb);
    }

    private void incrementerStockDisponible(UniteMateriel unite) {
        Materiel materiel = unite.getMateriel();

        if (materiel != null && materiel.getStock() != null) {
            materiel.getStock().setQuantiteDisponible(
                    materiel.getStock().getQuantiteDisponible() + 1
            );
        }
    }
}