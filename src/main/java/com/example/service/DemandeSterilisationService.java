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

    /**
     * Crée une nouvelle demande de stérilisation associée à une boîte chirurgicale
     * et éventuellement à une intervention.
     * La demande est créée avec le statut BROUILLON et un mouvement de la boîte
     * vers le stock sale est enregistré.
     *
     * @param codeDemande code unique de la demande
     * @param dateSouhaitee date souhaitée pour la stérilisation
     * @param priorite priorité de la demande
     * @param boiteId identifiant de la boîte chirurgicale concernée
     * @param interventionId identifiant éventuel de l'intervention associée
     * @param commentaire commentaire facultatif lié à la demande
     * @throws IllegalArgumentException si les données sont invalides,
     *                                  si le code existe déjà,
     *                                  si la boîte est introuvable
     *                                  ou si l'intervention est introuvable
     */
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

    /**
     * Vérifie la présence des informations obligatoires nécessaires
     * à la création d'une demande de stérilisation.
     *
     * @param codeDemande code de la demande
     * @param priorite priorité de la demande
     * @param boiteId identifiant de la boîte concernée
     * @throws IllegalArgumentException si une donnée obligatoire est absente
     */
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

    /**
     * Envoie une demande de stérilisation actuellement à l'état BROUILLON.
     * La demande passe à l'état ENVOYEE et la boîte associée est placée
     * en stock sale.
     *
     * @param demandeId identifiant de la demande à envoyer
     * @throws IllegalArgumentException si la demande est introuvable
     *                                  ou si elle n'est pas à l'état BROUILLON
     */
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

    /**
     * Accepte une demande de stérilisation précédemment envoyée.
     * La demande passe à l'état ACCEPTEE et la boîte associée
     * passe à l'état EN_STERILISATION.
     *
     * @param demandeId identifiant de la demande à accepter
     * @throws IllegalArgumentException si la demande est introuvable
     *                                  ou si elle n'est pas à l'état ENVOYEE
     */
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

    /**
     * Refuse une demande de stérilisation précédemment envoyée.
     * La demande passe à l'état REFUSEE et la boîte associée
     * est placée en quarantaine avec le statut INCIDENT.
     *
     * @param demandeId identifiant de la demande à refuser
     * @throws IllegalArgumentException si la demande est introuvable
     *                                  ou si elle n'est pas à l'état ENVOYEE
     */
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

    /**
     * Annule une demande de stérilisation tant qu'elle n'est pas terminée.
     * La demande passe à l'état ANNULEE et la boîte associée retourne
     * au bloc opératoire avec le statut ACTIVE.
     *
     * @param demandeId identifiant de la demande à annuler
     * @throws IllegalArgumentException si la demande est introuvable
     *                                  ou si elle est déjà terminée
     */
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

    /**
     * Retourne l'ensemble des demandes de stérilisation enregistrées.
     *
     * @return la liste de toutes les demandes de stérilisation
     */
    @Transactional(readOnly = true)
    public List<DemandeSterilisation> findAll() {
        return demandeRepository.findAll();
    }

    /**
     * Recherche une demande de stérilisation à partir de son identifiant.
     *
     * @param id identifiant de la demande
     * @return la demande de stérilisation correspondante
     * @throws IllegalArgumentException si aucune demande ne correspond à l'identifiant
     */
    @Transactional(readOnly = true)
    public DemandeSterilisation findById(Long id) {
        return demandeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
    }


    /**
     * Recherche une demande de stérilisation à partir de son code.
     *
     * @param codeDemande code de la demande recherchée
     * @return la demande de stérilisation correspondante
     * @throws IllegalArgumentException si aucune demande ne correspond au code fourni
     */
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

    /**
     * Enregistre un mouvement de la boîte chirurgicale associé
     * à un changement d'état de la demande de stérilisation.
     *
     * @param demande demande de stérilisation concernée
     * @param ancienneZone zone d'origine de la boîte
     * @param nouvelleZone nouvelle zone de la boîte
     * @param typeMouvement type de mouvement effectué
     * @param commentaire description du mouvement
     */
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

    /**
     * Vérifie qu'une demande possède le statut attendu avant
     * l'exécution d'une opération métier.
     *
     * @param demande demande à vérifier
     * @param statutAttendu statut requis pour l'opération
     * @param messageErreur message utilisé en cas de statut invalide
     * @throws IllegalArgumentException si le statut actuel ne correspond pas au statut attendu
     */
    private void verifierStatut(DemandeSterilisation demande,
                                StatutDemandeSterilisation statutAttendu,
                                String messageErreur) {
        if (demande.getStatut() != statutAttendu) {
            throw new IllegalArgumentException(messageErreur);
        }
    }
}