package com.example.service;

import com.example.entity.Personnel;
import com.example.repository.PersonnelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PersonnelService {

    private final PersonnelRepository personnelRepository;

    PersonnelService(PersonnelRepository personnelRepository) {
        this.personnelRepository = personnelRepository;
    }

    @Transactional
    public void createPersonnel(String matricule, String nomPersonnel, String prenomPersonnel, String specialite) {
        verifierPersonnel(matricule, nomPersonnel, prenomPersonnel, specialite);

        String mat = matricule.trim().toUpperCase();

        if (personnelRepository.findByMatricule(mat) != null) {
            throw new IllegalArgumentException("Ce matricule existe déjà");
        }

        Personnel personnel = new Personnel(
                mat,
                nomPersonnel.trim(),
                prenomPersonnel.trim(),
                specialite.trim()
        );

        personnelRepository.saveAndFlush(personnel);
    }

    @Transactional
    public void updatePersonnel(Personnel personnel, String matricule, String nomPersonnel, String prenomPersonnel, String specialite) {
        verifierPersonnel(matricule, nomPersonnel, prenomPersonnel, specialite);

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

        personnelRepository.saveAndFlush(personnelDb);
    }

    private void verifierPersonnel(String matricule, String nomPersonnel, String prenomPersonnel, String specialite) {
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
    }

    @Transactional(readOnly = true)
    public List<Personnel> findAll() {
        return personnelRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Personnel findByMatricule(String matricule) {
        return personnelRepository.findByMatricule(matricule);
    }

    @Transactional
    public void deletePersonnel(Personnel personnel) {
        Personnel personnelDb = personnelRepository.findById(personnel.getId())
                .orElseThrow(() -> new IllegalArgumentException("Personnel introuvable"));

        personnelRepository.delete(personnelDb);
    }
}