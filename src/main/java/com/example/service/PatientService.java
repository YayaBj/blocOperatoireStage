package com.example.service;

import com.example.entity.Patient;
import com.example.repository.PatientRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public void createPatient(String carteNationalPatient, String nomPatient, String prenomPatient, LocalDate dateNaissance) {
        validerPatient(carteNationalPatient, nomPatient, prenomPatient, dateNaissance);

        String cn = carteNationalPatient.trim().toUpperCase().replace("-", "");

        if (patientRepository.findByCnPatient(cn) != null) {
            throw new IllegalArgumentException("Le patient est déjà dans la base de données");
        }

        var patient = new Patient(cn, nomPatient.trim(), prenomPatient.trim(), dateNaissance);
        patientRepository.saveAndFlush(patient);
    }

    @Transactional
    public void updatePatient(Patient patient, String carteNationalPatient, String nomPatient, String prenomPatient, LocalDate dateNaissance) {
        validerPatient(carteNationalPatient, nomPatient, prenomPatient, dateNaissance);

        String cn = carteNationalPatient.trim().toUpperCase().replace("-", "");

        Patient existingPatient = patientRepository.findByCnPatient(cn);

        if (existingPatient != null && !existingPatient.getId().equals(patient.getId())) {
            throw new IllegalArgumentException("Ce CN est déjà utilisé par un autre patient");
        }

        patient.setCnPatient(cn);
        patient.setNomPatient(nomPatient.trim());
        patient.setPrenomPatient(prenomPatient.trim());
        patient.setDateNaissance(dateNaissance);

        patientRepository.saveAndFlush(patient);
    }

    private void validerPatient(String carteNationalPatient, String nomPatient, String prenomPatient, LocalDate dateNaissance) {
        if (carteNationalPatient == null || carteNationalPatient.trim().isBlank()) {
            throw new IllegalArgumentException("Le CN est obligatoire");
        }

        String cn = carteNationalPatient.trim().toUpperCase().replace("-", "");

        if (!cn.matches("^[A-Z]{1,2}[0-9]{5,7}$")) {
            throw new IllegalArgumentException("Format CN marocain invalide, exemple : BK123456");
        }

        if (nomPatient == null || nomPatient.trim().isBlank()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }

        if (prenomPatient == null || prenomPatient.trim().isBlank()) {
            throw new IllegalArgumentException("Le prénom est obligatoire");
        }

        if (dateNaissance == null) {
            throw new IllegalArgumentException("La date de naissance est obligatoire");
        }
    }

    @Transactional(readOnly = true)
    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public Patient getPatientByIdCN(String cnPatient) {
        return patientRepository.findByCnPatient(cnPatient);
    }

    @Transactional
    public void deletePatient(Patient patient) {
        patientRepository.delete(patient);
    }
}