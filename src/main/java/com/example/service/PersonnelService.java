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
        var personnel = new Personnel(matricule, nomPersonnel, prenomPersonnel, specialite);
        personnelRepository.saveAndFlush(personnel);
    }

    @Transactional
    public void updatePersonnel(Personnel personnel, String matricule, String nomPersonnel, String prenomPersonnel, String specialite) {
        personnel.setMatricule(matricule);
        personnel.setNomPersonnel(nomPersonnel);
        personnel.setPrenomPersonnel(prenomPersonnel);
        personnel.setSpecialite(specialite);
        personnelRepository.saveAndFlush(personnel);
    }

    @Transactional(readOnly = true)
    public List<Personnel> findAll() {
        return personnelRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Personnel getPersonnelByMatricule(String matricule) {
        return personnelRepository.findByMatricule(matricule);
    }

    @Transactional
    public void deletePersonnel(Personnel personnel) {
        personnelRepository.delete(personnel);
    }
}