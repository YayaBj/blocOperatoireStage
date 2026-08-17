package com.example.service;

import com.example.entity.Personnel;
import com.example.entity.enums.EtatPersonnel;
import com.example.repository.AffectationPersonnelRepository;
import com.example.repository.PersonnelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PersonnelService {

    private final PersonnelRepository personnelRepository;
    private final AffectationPersonnelRepository affectationPersonnelRepository;

    public PersonnelService(PersonnelRepository personnelRepository,
                            AffectationPersonnelRepository affectationPersonnelRepository) {
        this.personnelRepository = personnelRepository;
        this.affectationPersonnelRepository = affectationPersonnelRepository;
    }

    /**
     * Crée un nouveau membre du personnel après validation de ses informations.
     * Le matricule est normalisé en majuscules et son unicité est vérifiée
     * avant l'enregistrement.
     *
     * @param matricule matricule unique du membre du personnel
     * @param nomPersonnel nom du membre du personnel
     * @param prenomPersonnel prénom du membre du personnel
     * @param specialite spécialité du membre du personnel
     * @param etatPersonnel état actuel du membre du personnel
     * @throws IllegalArgumentException si les données sont invalides
     *                                  ou si le matricule existe déjà
     */
    @Transactional
    public void createPersonnel(String matricule, String nomPersonnel, String prenomPersonnel, String specialite, EtatPersonnel etatPersonnel) {
        verifierPersonnel(matricule, nomPersonnel, prenomPersonnel, specialite, etatPersonnel);

        String mat = matricule.trim().toUpperCase();

        if (personnelRepository.findByMatricule(mat) != null) {
            throw new IllegalArgumentException("Ce matricule existe déjà");
        }

        Personnel personnel = new Personnel(
                mat,
                nomPersonnel.trim(),
                prenomPersonnel.trim(),
                specialite.trim(),
                etatPersonnel
        );

        personnelRepository.saveAndFlush(personnel);
    }

    /**
     * Modifie les informations d'un membre du personnel existant.
     * Vérifie la validité des nouvelles données ainsi que l'unicité
     * du matricule.
     *
     * @param personnel membre du personnel à modifier
     * @param matricule nouveau matricule
     * @param nomPersonnel nouveau nom
     * @param prenomPersonnel nouveau prénom
     * @param specialite nouvelle spécialité
     * @param etatPersonnel nouvel état du membre du personnel
     * @throws IllegalArgumentException si les données sont invalides,
     *                                  si le personnel est introuvable
     *                                  ou si le matricule est déjà utilisé
     */
    @Transactional
    public void updatePersonnel(Personnel personnel, String matricule, String nomPersonnel, String prenomPersonnel, String specialite, EtatPersonnel etatPersonnel) {
        verifierPersonnel(matricule, nomPersonnel, prenomPersonnel, specialite, etatPersonnel);

        Personnel personnelDb = personnelRepository.findById(personnel.getId())
                .orElseThrow(() -> new IllegalArgumentException("Personnel introuvable"));

        String mat = matricule.trim().toUpperCase();

        Personnel existing = personnelRepository.findByMatricule(mat);

        if (existing != null && !existing.getId().equals(personnelDb.getId())) {
            throw new IllegalArgumentException("Ce matricule est déjà utilisé par un autre personnel");
        }

        personnelDb.setMatricule(mat);
        personnelDb.setNomPersonnel(nomPersonnel.trim());
        personnelDb.setPrenomPersonnel(prenomPersonnel.trim());
        personnelDb.setSpecialite(specialite.trim());
        personnelDb.setEtat(etatPersonnel);

        personnelRepository.saveAndFlush(personnelDb);
    }

    /**
     * Vérifie la présence des informations obligatoires nécessaires
     * à la création ou à la modification d'un membre du personnel.
     *
     * @param matricule matricule du membre du personnel
     * @param nomPersonnel nom du membre du personnel
     * @param prenomPersonnel prénom du membre du personnel
     * @param specialite spécialité du membre du personnel
     * @param etatPersonnel état du membre du personnel
     * @throws IllegalArgumentException si une information obligatoire est absente
     */
    private void verifierPersonnel(String matricule, String nomPersonnel, String prenomPersonnel, String specialite, EtatPersonnel etatPersonnel) {
        if (matricule == null || matricule.trim().isBlank()) {
            throw new IllegalArgumentException("Le matricule est obligatoire");
        }

        if (nomPersonnel == null || nomPersonnel.trim().isBlank()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }

        if (prenomPersonnel == null || prenomPersonnel.trim().isBlank()) {
            throw new IllegalArgumentException("Le prénom est obligatoire");
        }

        if (specialite == null || specialite.trim().isBlank()) {
            throw new IllegalArgumentException("La spécialité est obligatoire");
        }

        if (etatPersonnel == null) {
            throw new IllegalArgumentException("L'état est obligatoire");
        }
    }

    /**
     * Retourne l'ensemble des membres du personnel enregistrés.
     *
     * @return la liste de tout le personnel
     */
    @Transactional(readOnly = true)
    public List<Personnel> findAll() {
        return personnelRepository.findAll();
    }

    /**
     * Recherche un membre du personnel à partir de son matricule.
     *
     * @param matricule matricule du membre du personnel recherché
     * @return le personnel correspondant, ou null si aucun résultat n'est trouvé
     */
    @Transactional(readOnly = true)
    public Personnel findByMatricule(String matricule) {
        return personnelRepository.findByMatricule(matricule);
    }

    /**
     * Supprime un membre du personnel s'il n'est affecté
     * à aucune intervention.
     *
     * @param personnel membre du personnel à supprimer
     * @throws IllegalArgumentException si le personnel est introuvable
     *                                  ou s'il est affecté à une ou plusieurs interventions
     */
    @Transactional
    public void deletePersonnel(Personnel personnel) {
        if (personnel == null || personnel.getId() == null) {
            throw new IllegalArgumentException("Personnel introuvable");
        }

        Personnel personnelDb = personnelRepository.findById(personnel.getId())
                .orElseThrow(() -> new IllegalArgumentException("Personnel introuvable"));

        if (affectationPersonnelRepository.existsByPersonnelId(personnelDb.getId())) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer ce personnel car il est affecté à une ou plusieurs interventions"
            );
        }

        personnelRepository.delete(personnelDb);
    }

    /**
     * Recherche les membres du personnel correspondant à un état donné.
     *
     * @param etat état du personnel recherché
     * @return la liste des membres du personnel ayant cet état
     * @throws IllegalArgumentException si l'état fourni est null
     */
    @Transactional(readOnly = true)
    public List<Personnel> findByEtat(EtatPersonnel etat) {

        if (etat == null) {
            throw new IllegalArgumentException("L'état est obligatoire");
        }

        return personnelRepository.findByEtat(etat);
    }
}