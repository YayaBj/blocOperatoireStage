package com.example.service;

import com.example.entity.BoiteChirurgicale;
import com.example.entity.DemandeSterilisation;
import com.example.entity.Intervention;
import com.example.entity.enums.*;
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

    private final MouvementBoiteService mouvementBoiteService;

    public DemandeSterilisationService(DemandeSterilisationRepository demandeRepository,
                                       BoiteChirurgicaleRepository boiteRepository,
                                       InterventionRepository interventionRepository,
                                       MouvementBoiteService mouvementBoiteService) {
        this.demandeRepository = demandeRepository;
        this.boiteRepository = boiteRepository;
        this.interventionRepository = interventionRepository;
        this.mouvementBoiteService = mouvementBoiteService;
    }

    @Transactional
    public void createDemande(String codeDemande,
                              LocalDate dateSouhaitee,
                              PrioriteIntervention priorite,
                              Long boiteId,
                              Long interventionId,
                              String commentaire) {

        verifierDonneesDemande(codeDemande, priorite, boiteId);

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
        enregistrerMouvementDemande(
                demande,
                ZoneBoite.BLOC_OPERATOIRE,
                ZoneBoite.STOCK_SALE,
                TypeMouvementBoite.RETOUR_SALE,
                "Demande de stérilisation créée après utilisation de la boîte"
        );
    }

    private static void verifierDonneesDemande(String codeDemande, PrioriteIntervention priorite, Long boiteId) {
        if (codeDemande == null || codeDemande.trim().isBlank()) {
            throw new IllegalArgumentException("Le code de la demande est obligatoire");
        }

        if (priorite == null) {
            throw new IllegalArgumentException("La priorité est obligatoire");
        }

        if (boiteId == null) {
            throw new IllegalArgumentException("La boîte est obligatoire");
        }
    }

    @Transactional
    public void envoyerDemande(Long demandeId) {
        DemandeSterilisation demande = findById(demandeId);

        verifierStatut(
                demande,
                StatutDemandeSterilisation.BROUILLON,
                "Seule une demande brouillon peut être envoyée"
        );

        demande.setStatut(StatutDemandeSterilisation.ENVOYEE);

        enregistrerMouvementDemande(
                demande,
                ZoneBoite.STOCK_SALE,
                ZoneBoite.STOCK_SALE,
                TypeMouvementBoite.TRANSFERT_STERILISATION,
                "Demande envoyée au service de stérilisation"
        );

        demande.getBoiteChirurgicale().setStatut(StatutBoite.EN_STOCK_SALE);

        demandeRepository.saveAndFlush(demande);
    }

    @Transactional
    public void accepterDemande(Long demandeId) {
        DemandeSterilisation demande = findById(demandeId);

        verifierStatut(
                demande,
                StatutDemandeSterilisation.ENVOYEE,
                "Seule une demande envoyée peut être acceptée"
        );

        demande.setStatut(StatutDemandeSterilisation.ACCEPTEE);

        enregistrerMouvementDemande(
                demande,
                ZoneBoite.STOCK_SALE,
                ZoneBoite.STOCK_SALE,
                TypeMouvementBoite.TRANSFERT_STERILISATION,
                "Demande acceptée par le service de stérilisation"
        );

        demande.getBoiteChirurgicale().setStatut(StatutBoite.EN_STERILISATION);

        demandeRepository.saveAndFlush(demande);
    }

    @Transactional
    public void refuserDemande(Long demandeId) {
        DemandeSterilisation demande = findById(demandeId);

        verifierStatut(
                demande,
                StatutDemandeSterilisation.ENVOYEE,
                "Seule une demande envoyée peut être refusée"
        );

        demande.setStatut(StatutDemandeSterilisation.REFUSEE);

        enregistrerMouvementDemande(
                demande,
                ZoneBoite.STOCK_SALE,
                ZoneBoite.QUARANTAINE,
                TypeMouvementBoite.MISE_QUARANTAINE,
                "Demande refusée, boîte mise en quarantaine"
        );

        demande.getBoiteChirurgicale().setStatut(StatutBoite.INCIDENT);

        demandeRepository.saveAndFlush(demande);
    }

    @Transactional
    public void annulerDemande(Long demandeId) {
        DemandeSterilisation demande = findById(demandeId);

        if (demande.getStatut() == StatutDemandeSterilisation.TERMINEE) {
            throw new IllegalArgumentException("Impossible d’annuler une demande terminée");
        }

        demande.setStatut(StatutDemandeSterilisation.ANNULEE);

        enregistrerMouvementDemande(
                demande,
                ZoneBoite.STOCK_SALE,
                ZoneBoite.BLOC_OPERATOIRE,
                TypeMouvementBoite.SORTIE_STOCK,
                "Demande annulée, retour de la boîte au bloc opératoire"
        );

        demande.getBoiteChirurgicale().setStatut(StatutBoite.ACTIVE);

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


    @Transactional(readOnly = true)
    public DemandeSterilisation findByCodeDemande(String codeDemande) {
        return demandeRepository
                .findByCodeDemande(codeDemande)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Demande de stérilisation introuvable"
                        )
                );
    }

    private void enregistrerMouvementDemande(DemandeSterilisation demande,
                                             ZoneBoite ancienneZone,
                                             ZoneBoite nouvelleZone,
                                             TypeMouvementBoite typeMouvement,
                                             String commentaire) {
        mouvementBoiteService.enregistrerMouvement(
                demande.getBoiteChirurgicale().getId(),
                null,
                ancienneZone,
                nouvelleZone,
                typeMouvement,
                commentaire
        );
    }

    private void verifierStatut(DemandeSterilisation demande,
                                StatutDemandeSterilisation statutAttendu,
                                String messageErreur) {
        if (demande.getStatut() != statutAttendu) {
            throw new IllegalArgumentException(messageErreur);
        }
    }
}