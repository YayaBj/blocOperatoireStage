package com.example.service;

import com.example.entity.Materiel;
import com.example.entity.Stock;
import com.example.entity.UniteMateriel;
import com.example.entity.enums.EtatMateriel;
import com.example.repository.MaterielRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MaterielService {

    private final MaterielRepository materielRepository;

    MaterielService(MaterielRepository materielRepository) {
        this.materielRepository = materielRepository;
    }

    /**
     * Crée une nouvelle référence de matériel avec son stock et ses unités physiques.
     * Les unités générées sont initialisées comme stériles ou indisponibles
     * selon la quantité disponible renseignée.
     *
     * @param nomMateriel nom du matériel
     * @param typeMateriel type du matériel
     * @param quantiteTotale quantité totale d'unités à créer
     * @param quantiteDisponible nombre d'unités initialement disponibles
     * @param seuilAlerte seuil à partir duquel une alerte de stock peut être déclenchée
     * @throws IllegalArgumentException si le matériel existe déjà
     *                                  ou si les quantités renseignées sont invalides
     */
    @Transactional
    public void createMateriel(String nomMateriel, String typeMateriel,
                               int quantiteTotale, int quantiteDisponible, int seuilAlerte) {

        if (materielRepository.existsByNomMaterielIgnoreCaseAndTypeMaterielIgnoreCase(nomMateriel, typeMateriel)) {
            throw new IllegalArgumentException("Ce matériel existe déjà");
        }

        verificationQuantite(quantiteTotale, quantiteDisponible, seuilAlerte);

        Stock stock = new Stock(quantiteTotale, quantiteDisponible, seuilAlerte);
        Materiel materiel = new Materiel(nomMateriel, typeMateriel, stock);

        for (int i = 1; i <= quantiteTotale; i++) {
            EtatMateriel etat = i <= quantiteDisponible
                    ? EtatMateriel.STERILE
                    : EtatMateriel.INDISPONIBLE;

            String codeInventaire = generateCodeInventaire(nomMateriel, i);

            materiel.getUnites().add(
                    creerUnite(
                            codeInventaire,
                            etat,
                            materiel
                    )
            );
        }

        materielRepository.saveAndFlush(materiel);
    }

    /**
     * Vérifie la cohérence des quantités et du seuil d'alerte d'un matériel.
     *
     * @param quantiteTotale quantité totale du matériel
     * @param quantiteDisponible quantité actuellement disponible
     * @param seuilAlerte seuil d'alerte du stock
     * @throws IllegalArgumentException si la quantité totale n'est pas positive,
     *                                  si la quantité disponible est invalide
     *                                  ou si le seuil d'alerte est hors limites
     */
    private void verificationQuantite(int quantiteTotale, int quantiteDisponible, int seuilAlerte) {
        if (quantiteTotale <= 0) {
            throw new IllegalArgumentException("La quantité totale doit être supérieure à 0");
        }

        if (quantiteDisponible < 0 || quantiteDisponible > quantiteTotale) {
            throw new IllegalArgumentException("La quantité disponible doit être entre 0 et la quantité totale");
        }

        if (seuilAlerte < 0 || seuilAlerte > quantiteTotale) {
            throw new IllegalArgumentException("Le seuil d’alerte doit être entre 0 et la quantité totale");
        }
    }

    /**
     * Génère un code d'inventaire unique à partir du nom du matériel
     * et de l'index de l'unité.
     *
     * @param nomMateriel nom du matériel utilisé pour générer le préfixe
     * @param index numéro de l'unité dans le stock
     * @return le code d'inventaire généré
     */
    private String generateCodeInventaire(String nomMateriel, int index) {
        String prefix = nomMateriel
                .trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]", "");

        if (prefix.length() > 6) {
            prefix = prefix.substring(0, 6);
        }

        return prefix + "-" + String.format("%03d", index);
    }

    /**
     * Modifie les informations d'une référence de matériel ainsi que son seuil d'alerte.
     * Vérifie également qu'aucun autre matériel ne possède la même combinaison
     * de nom et de type.
     *
     * @param materiel matériel à modifier
     * @param nomMateriel nouveau nom du matériel
     * @param typeMateriel nouveau type du matériel
     * @param seuilAlerte nouveau seuil d'alerte
     * @throws IllegalArgumentException si le matériel est introuvable,
     *                                  si le seuil d'alerte est invalide
     *                                  ou si un matériel identique existe déjà
     */
    @Transactional
    public void updateMateriel(Materiel materiel, String nomMateriel, String typeMateriel, int seuilAlerte) {
        Materiel materielComplet = findById(materiel.getId());

        if (seuilAlerte < 0 || seuilAlerte > materielComplet.getStock().getQuantiteTotale()) {
            throw new IllegalArgumentException("Le seuil d’alerte doit être entre 0 et la quantité totale");
        }

        Materiel existing =
                materielRepository
                        .findByNomMaterielIgnoreCaseAndTypeMaterielIgnoreCase(
                                nomMateriel,
                                typeMateriel
                        );

        if (existing != null &&
                !existing.getId().equals(materielComplet.getId())) {

            throw new IllegalArgumentException(
                    "Ce matériel existe déjà"
            );
        }

        materielComplet.setNomMateriel(nomMateriel);
        materielComplet.setTypeMateriel(typeMateriel);
        materielComplet.getStock().setSeuilAlerte(seuilAlerte);

        materielRepository.saveAndFlush(materielComplet);
    }

    /**
     * Retourne l'ensemble des références de matériel enregistrées.
     *
     * @return la liste de tous les matériels
     */
    @Transactional(readOnly = true)
    public List<Materiel> findAll() {
        return materielRepository.findAll();
    }

    /**
     * Supprime une référence de matériel.
     *
     * @param materiel matériel à supprimer
     */
    @Transactional
    public void deleteMateriel(Materiel materiel) {
        materielRepository.delete(materiel);
    }

    /**
     * Ajoute de nouvelles unités physiques au stock d'un matériel.
     * Chaque nouvelle unité reçoit automatiquement un code d'inventaire
     * et est initialisée avec l'état STERILE.
     *
     * @param materiel matériel dont le stock doit être augmenté
     * @param quantiteAjoutee nombre de nouvelles unités à ajouter
     * @throws IllegalArgumentException si la quantité ajoutée n'est pas positive
     *                                  ou si le matériel est introuvable
     */
    @Transactional
    public void ajouterStock(Materiel materiel, int quantiteAjoutee) {
        if (quantiteAjoutee <= 0) {
            throw new IllegalArgumentException("La quantité ajoutée doit être supérieure à 0");
        }

        Materiel materielComplet = findById(materiel.getId());

        int ancienneQuantite = materielComplet.getStock().getQuantiteTotale();

        for (int i = 1; i <= quantiteAjoutee; i++) {
            String codeInventaire = generateCodeInventaire(
                    materielComplet.getNomMateriel(),
                    ancienneQuantite + i
            );

            materielComplet.getUnites().add(
                    creerUnite(
                            codeInventaire,
                            EtatMateriel.STERILE,
                            materielComplet
                    )
            );
        }

        materielComplet.getStock().setQuantiteTotale(ancienneQuantite + quantiteAjoutee);
        materielComplet.getStock().setQuantiteDisponible(
                materielComplet.getStock().getQuantiteDisponible() + quantiteAjoutee
        );

        materielRepository.saveAndFlush(materielComplet);
    }

    /**
     * Retourne l'ensemble des unités physiques associées à une référence de matériel.
     *
     * @param materiel matériel dont les unités doivent être récupérées
     * @return la liste des unités associées au matériel
     * @throws IllegalArgumentException si le matériel est introuvable
     */
    @Transactional(readOnly = true)
    public List<UniteMateriel> findUnitesByMateriel(Materiel materiel) {
        Materiel materielComplet = findById(materiel.getId());
        return new ArrayList<>(materielComplet.getUnites());
    }

    /**
     * Recherche une référence de matériel à partir de son identifiant.
     *
     * @param id identifiant du matériel recherché
     * @return le matériel correspondant
     * @throws IllegalArgumentException si le matériel est introuvable
     */
    private Materiel findById(Long id) {
        return materielRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Matériel introuvable"));
    }

    /**
     * Crée une nouvelle unité physique associée à une référence de matériel.
     *
     * @param codeInventaire code d'inventaire de l'unité
     * @param etat état initial de l'unité
     * @param materiel référence de matériel à laquelle l'unité appartient
     * @return la nouvelle unité de matériel
     */
    private UniteMateriel creerUnite(
            String codeInventaire,
            EtatMateriel etat,
            Materiel materiel) {

        return new UniteMateriel(
                codeInventaire,
                etat,
                materiel
        );
    }
}