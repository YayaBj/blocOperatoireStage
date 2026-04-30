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
        var patient = new Patient(carteNationalPatient, nomPatient, prenomPatient, dateNaissance);
        patientRepository.saveAndFlush(patient);
    }

    @Transactional
    public void updatePatient(Patient patient, String carteNationalPatient, String nomPatient, String prenomPatient, LocalDate dateNaissance) {
        patient.setCnPatient(carteNationalPatient);
        patient.setNomPatient(nomPatient);
        patient.setPrenomPatient(prenomPatient);
        patient.setDateNaissance(dateNaissance);
        patientRepository.saveAndFlush(patient);
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