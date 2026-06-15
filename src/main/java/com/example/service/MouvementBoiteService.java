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

    @Transactional
    public void enregistrerMouvement(Long boiteId,
                                     Long processusId,
                                     ZoneBoite ancienneZone,
                                     ZoneBoite nouvelleZone,
                                     TypeMouvementBoite typeMouvement,
                                     String commentaire) {

        if (boiteId == null) {
            throw new IllegalArgumentException("La boîte est obligatoire");
        }

        if (nouvelleZone == null) {
            throw new IllegalArgumentException("La nouvelle zone est obligatoire");
        }

        if (typeMouvement == null) {
            throw new IllegalArgumentException("Le type de mouvement est obligatoire");
        }

        BoiteChirurgicale boite = boiteChirurgicaleRepository.findById(boiteId)
                .orElseThrow(() -> new IllegalArgumentException("Boîte introuvable"));

        ProcessusSterilisation processus = null;

        if (processusId != null) {
            processus = processusSterilisationRepository.findById(processusId)
                    .orElseThrow(() -> new IllegalArgumentException("Processus introuvable"));
        }

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

    @Transactional(readOnly = true)
    public List<MouvementBoite> findAll() {
        return mouvementBoiteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<MouvementBoite> findByBoite(Long boiteId) {
        return mouvementBoiteRepository.findByBoiteChirurgicaleIdOrderByDateMouvementAsc(boiteId);
    }

    @Transactional(readOnly = true)
    public List<MouvementBoite> findByProcessus(Long processusId) {
        return mouvementBoiteRepository.findByProcessusSterilisationIdOrderByDateMouvementAsc(processusId);
    }
}