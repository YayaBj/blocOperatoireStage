package com.example.service;

import com.example.entity.BoiteChirurgicale;
import com.example.entity.DemandeSterilisation;
import com.example.entity.Intervention;
import com.example.entity.enums.PrioriteIntervention;
import com.example.entity.enums.StatutBoite;
import com.example.entity.enums.StatutDemandeSterilisation;
import com.example.repository.BoiteChirurgicaleRepository;
import com.example.repository.DemandeSterilisationRepository;
import com.example.repository.InterventionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DemandeSterilisationService {

    private final DemandeSterilisationRepository demandeRepository;
    private final BoiteChirurgicaleRepository boiteRepository;
    private final InterventionRepository interventionRepository;

    public DemandeSterilisationService(DemandeSterilisationRepository demandeRepository,
                                       BoiteChirurgicaleRepository boiteRepository,
                                       InterventionRepository interventionRepository) {
        this.demandeRepository = demandeRepository;
        this.boiteRepository = boiteRepository;
        this.interventionRepository = interventionRepository;
    }

    @Transactional
    public void createDemande(String codeDemande,
                              LocalDate dateSouhaitee,
                              PrioriteIntervention priorite,
                              Long boiteId,
                              Long interventionId,
                              String commentaire) {

        if (codeDemande == null || codeDemande.trim().isBlank()) {
            throw new IllegalArgumentException("Le code de la demande est obligatoire");
        }

        if (priorite == null) {
            throw new IllegalArgumentException("La priorité est obligatoire");
        }

        if (boiteId == null) {
            throw new IllegalArgumentException("La boîte est obligatoire");
        }

        String code = codeDemande.trim().toUpperCase();

        if (demandeRepository.existsByCodeDemandeIgnoreCase(code)) {
            throw new IllegalArgumentException("Ce code de demande existe déjà");
        }

        BoiteChirurgicale boite = boiteRepository.findById(boiteId)
                .orElseThrow(() -> new IllegalArgumentException("Boîte introuvable"));

        Intervention intervention = null;

        if (interventionId != null) {
            intervention = interventionRepository.findById(interventionId)
                    .orElseThrow(() -> new IllegalArgumentException("Intervention introuvable"));
        }

        DemandeSterilisation demande = new DemandeSterilisation(
                code,
                LocalDateTime.now(),
                dateSouhaitee,
                priorite,
                StatutDemandeSterilisation.BROUILLON,
                boite,
                intervention,
                commentaire
        );

        demandeRepository.saveAndFlush(demande);
    }

    @Transactional
    public void envoyerDemande(Long demandeId) {
        DemandeSterilisation demande = findById(demandeId);

        if (demande.getStatut() != StatutDemandeSterilisation.BROUILLON) {
            throw new IllegalArgumentException("Seule une demande brouillon peut être envoyée");
        }

        demande.setStatut(StatutDemandeSterilisation.ENVOYEE);
        demande.getBoiteChirurgicale().setStatut(StatutBoite.EN_STOCK_SALE);

        demandeRepository.saveAndFlush(demande);
    }

    @Transactional
    public void accepterDemande(Long demandeId) {
        DemandeSterilisation demande = findById(demandeId);

        if (demande.getStatut() != StatutDemandeSterilisation.ENVOYEE) {
            throw new IllegalArgumentException("Seule une demande envoyée peut être acceptée");
        }

        demande.setStatut(StatutDemandeSterilisation.ACCEPTEE);
        demande.getBoiteChirurgicale().setStatut(StatutBoite.EN_STERILISATION);

        demandeRepository.saveAndFlush(demande);
    }

    @Transactional
    public void refuserDemande(Long demandeId) {
        DemandeSterilisation demande = findById(demandeId);

        if (demande.getStatut() != StatutDemandeSterilisation.ENVOYEE) {
            throw new IllegalArgumentException("Seule une demande envoyée peut être refusée");
        }

        demande.setStatut(StatutDemandeSterilisation.REFUSEE);

        demandeRepository.saveAndFlush(demande);
    }

    @Transactional
    public void annulerDemande(Long demandeId) {
        DemandeSterilisation demande = findById(demandeId);

        if (demande.getStatut() == StatutDemandeSterilisation.TERMINEE) {
            throw new IllegalArgumentException("Impossible d’annuler une demande terminée");
        }

        demande.setStatut(StatutDemandeSterilisation.ANNULEE);

        demandeRepository.saveAndFlush(demande);
    }

    @Transactional(readOnly = true)
    public List<DemandeSterilisation> findAll() {
        return demandeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public DemandeSterilisation findById(Long id) {
        return demandeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
    }
}