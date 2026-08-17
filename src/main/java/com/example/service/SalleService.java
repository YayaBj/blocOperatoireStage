package com.example.service;

import com.example.entity.Salle;
import com.example.entity.enums.StatutSalle;
import com.example.repository.InterventionRepository;
import com.example.repository.SalleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalleService {

    private final SalleRepository salleRepository;
    private final InterventionRepository interventionRepository;

    SalleService(SalleRepository salleRepository,
                 InterventionRepository interventionRepository) {
        this.salleRepository = salleRepository;
        this.interventionRepository = interventionRepository;
    }

    /**
     * Crée une nouvelle salle après validation de ses informations.
     * Le numéro de salle est normalisé en majuscules et son unicité
     * est vérifiée avant l'enregistrement.
     *
     * @param numeroSalle numéro unique de la salle
     * @param typeSalle type de la salle
     * @param statutSalle statut initial de la salle
     * @throws IllegalArgumentException si les données sont invalides
     *                                  ou si le numéro de salle existe déjà
     */
    @Transactional
    public void createSalle(String numeroSalle, String typeSalle, StatutSalle statutSalle) {
        verifierSalle(numeroSalle, typeSalle, statutSalle);

        String numero = numeroSalle.trim().toUpperCase();

        if (salleRepository.findByNumeroSalle(numero) != null) {
            throw new IllegalArgumentException("Cette salle existe déjà");
        }

        Salle salle = new Salle(
                numero,
                typeSalle.trim(),
                statutSalle
        );

        salleRepository.saveAndFlush(salle);
    }

    /**
     * Modifie les informations d'une salle existante.
     * Vérifie la validité des nouvelles données ainsi que l'unicité
     * du numéro de salle.
     *
     * @param salle salle à modifier
     * @param numeroSalle nouveau numéro de la salle
     * @param typeSalle nouveau type de la salle
     * @param statutSalle nouveau statut de la salle
     * @throws IllegalArgumentException si les données sont invalides,
     *                                  si la salle est introuvable
     *                                  ou si le numéro est déjà utilisé
     */
    @Transactional
    public void updateSalle(Salle salle, String numeroSalle, String typeSalle, StatutSalle statutSalle) {
        verifierSalle(numeroSalle, typeSalle, statutSalle);

        Salle salleDb = salleRepository.findById(salle.getId())
                .orElseThrow(() -> new IllegalArgumentException("Salle introuvable"));

        String numero = numeroSalle.trim().toUpperCase();

        Salle existing = salleRepository.findByNumeroSalle(numero);

        if (existing != null && !existing.getId().equals(salleDb.getId())) {
            throw new IllegalArgumentException("Ce numéro de salle est déjà utilisé");
        }

        salleDb.setNumeroSalle(numero);
        salleDb.setTypeSalle(typeSalle.trim());
        salleDb.setStatutSalle(statutSalle);

        salleRepository.saveAndFlush(salleDb);
    }

    /**
     * Vérifie la présence des informations obligatoires nécessaires
     * à la création ou à la modification d'une salle.
     *
     * @param numeroSalle numéro de la salle
     * @param typeSalle type de la salle
     * @param statutSalle statut de la salle
     * @throws IllegalArgumentException si une information obligatoire est absente
     */
    private void verifierSalle(String numeroSalle, String typeSalle, StatutSalle statutSalle) {
        if (numeroSalle == null || numeroSalle.trim().isBlank()) {
            throw new IllegalArgumentException("Le numéro de salle est obligatoire");
        }

        if (typeSalle == null || typeSalle.trim().isBlank()) {
            throw new IllegalArgumentException("Le type de salle est obligatoire");
        }

        if (statutSalle == null) {
            throw new IllegalArgumentException("Le statut de salle est obligatoire");
        }
    }

    /**
     * Retourne l'ensemble des salles enregistrées.
     *
     * @return la liste de toutes les salles
     */
    @Transactional(readOnly = true)
    public List<Salle> findAll() {
        return salleRepository.findAll();
    }

    /**
     * Recherche les salles correspondant à un statut donné.
     *
     * @param statutSalle statut des salles recherchées
     * @return la liste des salles ayant le statut demandé
     */
    @Transactional(readOnly = true)
    public List<Salle> findByStatut(StatutSalle statutSalle) {
        return salleRepository.findByStatutSalle(statutSalle);
    }

    /**
     * Recherche une salle à partir de son numéro.
     *
     * @param numeroSalle numéro de la salle recherchée
     * @return la salle correspondante, ou null si aucune salle n'est trouvée
     */
    @Transactional(readOnly = true)
    public Salle getSalleByNumeroSalle(String numeroSalle) {
        return salleRepository.findByNumeroSalle(numeroSalle);
    }

    /**
     * Supprime une salle si celle-ci n'est associée à aucune intervention.
     *
     * @param salle salle à supprimer
     * @throws IllegalArgumentException si la salle est introuvable
     *                                  ou si elle est liée à une intervention
     */
    @Transactional
    public void deleteSalle(Salle salle) {
        if (salle == null || salle.getId() == null) {
            throw new IllegalArgumentException("Salle introuvable");
        }

        Salle salleDb = salleRepository.findById(salle.getId())
                .orElseThrow(() -> new IllegalArgumentException("Salle introuvable"));

        if (interventionRepository.existsBySalleId(salleDb.getId())) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer cette salle car elle est liée à une intervention"
            );
        }

        salleRepository.delete(salleDb);
    }
}
