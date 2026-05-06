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
                    : EtatMateriel.UTILISE;

            String codeInventaire = generateCodeInventaire(nomMateriel, i);

            UniteMateriel unite = new UniteMateriel(codeInventaire, etat, materiel);
            materiel.getUnites().add(unite);
        }

        materielRepository.saveAndFlush(materiel);
    }

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

    @Transactional
    public void updateMateriel(Materiel materiel, String nomMateriel, String typeMateriel, int seuilAlerte) {
        Materiel materielComplet = materielRepository.findById(materiel.getId())
                .orElseThrow(() -> new IllegalArgumentException("Matériel introuvable"));

        if (seuilAlerte < 0 || seuilAlerte > materielComplet.getStock().getQuantiteTotale()) {
            throw new IllegalArgumentException("Le seuil d’alerte doit être entre 0 et la quantité totale");
        }

        materielComplet.setNomMateriel(nomMateriel);
        materielComplet.setTypeMateriel(typeMateriel);
        materielComplet.getStock().setSeuilAlerte(seuilAlerte);

        materielRepository.saveAndFlush(materielComplet);
    }

    @Transactional(readOnly = true)
    public List<Materiel> findAll() {
        return materielRepository.findAll();
    }

    @Transactional
    public void deleteMateriel(Materiel materiel) {
        materielRepository.delete(materiel);
    }

    @Transactional
    public void ajouterStock(Materiel materiel, int quantiteAjoutee) {
        if (quantiteAjoutee <= 0) {
            throw new IllegalArgumentException("La quantité ajoutée doit être supérieure à 0");
        }

        Materiel materielComplet = materielRepository.findById(materiel.getId())
                .orElseThrow(() -> new IllegalArgumentException("Matériel introuvable"));

        int ancienneQuantite = materielComplet.getStock().getQuantiteTotale();

        for (int i = 1; i <= quantiteAjoutee; i++) {
            String codeInventaire = generateCodeInventaire(
                    materielComplet.getNomMateriel(),
                    ancienneQuantite + i
            );

            UniteMateriel unite = new UniteMateriel(
                    codeInventaire,
                    EtatMateriel.STERILE,
                    materielComplet
            );

            materielComplet.getUnites().add(unite);
        }

        materielComplet.getStock().setQuantiteTotale(ancienneQuantite + quantiteAjoutee);
        materielComplet.getStock().setQuantiteDisponible(
                materielComplet.getStock().getQuantiteDisponible() + quantiteAjoutee
        );

        materielRepository.saveAndFlush(materielComplet);
    }

    @Transactional(readOnly = true)
    public List<UniteMateriel> findUnitesByMateriel(Materiel materiel) {
        Materiel materielComplet = materielRepository.findById(materiel.getId())
                .orElseThrow(() -> new IllegalArgumentException("Matériel introuvable"));
        return new ArrayList<>(materielComplet.getUnites());
    }
}