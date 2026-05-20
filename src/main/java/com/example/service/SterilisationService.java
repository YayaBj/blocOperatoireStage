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
    public List<Sterilisation> findAll() {
        return sterilisationRepository.findAll();
    }

    @Transactional
    public void avancerSterilisation(Long sterilisationId) {
        Sterilisation sterilisation = sterilisationRepository.findById(sterilisationId)
                .orElseThrow(() -> new IllegalArgumentException("Stérilisation introuvable"));

        if (sterilisation.getStatut() == StatutSterilisation.TERMINEE) {
            throw new IllegalArgumentException("Cette stérilisation est déjà terminée");
        }

        if (sterilisation.getStatut() == StatutSterilisation.ECHEC) {
            throw new IllegalArgumentException("Cette stérilisation est en échec");
        }

        StatutSterilisation prochainStatut = getNextStatut(sterilisation.getStatut());
        sterilisation.setStatut(prochainStatut);

        if (prochainStatut == StatutSterilisation.TERMINEE) {
            UniteMateriel unite = sterilisation.getUniteMateriel();

            unite.setEtat(EtatMateriel.STERILE);
            sterilisation.setDateFin(LocalDate.now());

            incrementerStockDisponible(unite);

            uniteMaterielRepository.save(unite);
        }

        sterilisationRepository.saveAndFlush(sterilisation);
    }

    public StatutSterilisation getNextStatut(StatutSterilisation statut) {
        return switch (statut) {
            case EN_ATTENTE_COLLECTE -> StatutSterilisation.EN_TRANSPORT;
            case EN_TRANSPORT -> StatutSterilisation.EN_LAVAGE;
            case EN_LAVAGE -> StatutSterilisation.CONTROLE_QUALITE;
            case CONTROLE_QUALITE -> StatutSterilisation.EN_EMBALLAGE;
            case EN_EMBALLAGE -> StatutSterilisation.EN_AUTOCLAVE;
            case EN_AUTOCLAVE -> StatutSterilisation.VALIDATION_CYCLE;
            case VALIDATION_CYCLE -> StatutSterilisation.TERMINEE;
            default -> statut;
        };
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