package com.example.service;

import com.example.entity.BoiteChirurgicale;
import com.example.entity.MouvementBoite;
import com.example.entity.ProcessusSterilisation;
import com.example.entity.enums.TypeMouvementBoite;
import com.example.entity.enums.ZoneBoite;
import com.example.repository.BoiteChirurgicaleRepository;
import com.example.repository.MouvementBoiteRepository;
import com.example.repository.ProcessusSterilisationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MouvementBoiteService {

    private final MouvementBoiteRepository mouvementBoiteRepository;
    private final BoiteChirurgicaleRepository boiteChirurgicaleRepository;
    private final ProcessusSterilisationRepository processusSterilisationRepository;

    public MouvementBoiteService(MouvementBoiteRepository mouvementBoiteRepository,
                                 BoiteChirurgicaleRepository boiteChirurgicaleRepository,
                                 ProcessusSterilisationRepository processusSterilisationRepository) {
        this.mouvementBoiteRepository = mouvementBoiteRepository;
        this.boiteChirurgicaleRepository = boiteChirurgicaleRepository;
        this.processusSterilisationRepository = processusSterilisationRepository;
    }

    /**
     * Enregistre un nouveau mouvement de boîte chirurgicale entre deux zones.
     * Le mouvement peut éventuellement être associé à un processus de stérilisation.
     *
     * @param boiteId identifiant de la boîte concernée
     * @param processusId identifiant éventuel du processus de stérilisation associé
     * @param ancienneZone zone d'origine de la boîte
     * @param nouvelleZone nouvelle zone de la boîte
     * @param typeMouvement type de mouvement effectué
     * @param commentaire commentaire décrivant le mouvement
     * @throws IllegalArgumentException si les données obligatoires sont absentes,
     *                                  si la boîte est introuvable
     *                                  ou si le processus fourni est introuvable
     */
    @Transactional
    public void enregistrerMouvement(Long boiteId,
                                     Long processusId,
                                     ZoneBoite ancienneZone,
                                     ZoneBoite nouvelleZone,
                                     TypeMouvementBoite typeMouvement,
                                     String commentaire) {

        verifierMouvement(boiteId, nouvelleZone, typeMouvement);

        BoiteChirurgicale boite = findBoite(boiteId);
        ProcessusSterilisation processus = findProcessusIfPresent(processusId);

        MouvementBoite mouvement = new MouvementBoite(
                LocalDateTime.now(),
                ancienneZone,
                nouvelleZone,
                typeMouvement,
                commentaire,
                boite,
                processus
        );

        mouvementBoiteRepository.saveAndFlush(mouvement);
    }

    /**
     * Vérifie la présence des informations obligatoires nécessaires
     * à l'enregistrement d'un mouvement de boîte.
     *
     * @param boiteId identifiant de la boîte concernée
     * @param nouvelleZone zone de destination de la boîte
     * @param typeMouvement type de mouvement à enregistrer
     * @throws IllegalArgumentException si une information obligatoire est absente
     */
    private static void verifierMouvement(Long boiteId, ZoneBoite nouvelleZone, TypeMouvementBoite typeMouvement) {
        if (boiteId == null) {
            throw new IllegalArgumentException("La boîte est obligatoire");
        }

        if (nouvelleZone == null) {
            throw new IllegalArgumentException("La nouvelle zone est obligatoire");
        }

        if (typeMouvement == null) {
            throw new IllegalArgumentException("Le type de mouvement est obligatoire");
        }
    }

    /**
     * Retourne l'ensemble des mouvements de boîtes enregistrés.
     *
     * @return la liste de tous les mouvements de boîtes
     */
    @Transactional(readOnly = true)
    public List<MouvementBoite> findAll() {
        return mouvementBoiteRepository.findAll();
    }

    /**
     * Retourne l'historique des mouvements associés à une boîte chirurgicale,
     * trié chronologiquement par date de mouvement.
     *
     * @param boiteId identifiant de la boîte concernée
     * @return la liste des mouvements de la boîte
     */
    @Transactional(readOnly = true)
    public List<MouvementBoite> findByBoite(Long boiteId) {
        return mouvementBoiteRepository.findByBoiteChirurgicaleIdOrderByDateMouvementAsc(boiteId);
    }

    /**
     * Retourne l'ensemble des mouvements associés à un processus de stérilisation,
     * triés chronologiquement par date de mouvement.
     *
     * @param processusId identifiant du processus concerné
     * @return la liste des mouvements associés au processus
     */
    @Transactional(readOnly = true)
    public List<MouvementBoite> findByProcessus(Long processusId) {
        return mouvementBoiteRepository.findByProcessusSterilisationIdOrderByDateMouvementAsc(processusId);
    }

    /**
     * Recherche une boîte chirurgicale à partir de son identifiant.
     *
     * @param boiteId identifiant de la boîte recherchée
     * @return la boîte chirurgicale correspondante
     * @throws IllegalArgumentException si la boîte est introuvable
     */
    private BoiteChirurgicale findBoite(Long boiteId) {
        return boiteChirurgicaleRepository.findById(boiteId)
                .orElseThrow(() -> new IllegalArgumentException("Boîte introuvable"));
    }

    /**
     * Recherche un processus de stérilisation lorsqu'un identifiant est fourni.
     *
     * @param processusId identifiant éventuel du processus
     * @return le processus correspondant ou null si aucun identifiant n'est fourni
     * @throws IllegalArgumentException si l'identifiant fourni ne correspond
     *                                  à aucun processus
     */
    private ProcessusSterilisation findProcessusIfPresent(Long processusId) {
        if (processusId == null) {
            return null;
        }

        return processusSterilisationRepository.findById(processusId)
                .orElseThrow(() -> new IllegalArgumentException("Processus introuvable"));
    }
}