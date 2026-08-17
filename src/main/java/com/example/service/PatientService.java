package com.example.service;

import com.example.entity.Patient;
import com.example.repository.InterventionRepository;
import com.example.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final InterventionRepository interventionRepository;

    public PatientService(PatientRepository patientRepository,
                          InterventionRepository interventionRepository) {

        this.patientRepository = patientRepository;
        this.interventionRepository = interventionRepository;
    }

    /**
     * Crée un nouveau patient après validation de ses informations.
     * Le numéro de carte nationale est normalisé avant l'enregistrement
     * et son unicité dans la base de données est vérifiée.
     *
     * @param carteNationalPatient numéro de carte nationale du patient
     * @param nomPatient nom du patient
     * @param prenomPatient prénom du patient
     * @param dateNaissance date de naissance du patient
     * @throws IllegalArgumentException si les données sont invalides
     *                                  ou si le patient existe déjà
     */
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

    /**
     * Modifie les informations d'un patient existant.
     * Vérifie la validité des nouvelles données ainsi que l'unicité
     * du numéro de carte nationale.
     *
     * @param patient patient à modifier
     * @param carteNationalPatient nouveau numéro de carte nationale
     * @param nomPatient nouveau nom du patient
     * @param prenomPatient nouveau prénom du patient
     * @param dateNaissance nouvelle date de naissance
     * @throws IllegalArgumentException si les données sont invalides,
     *                                  si le patient est introuvable
     *                                  ou si le numéro de carte nationale est déjà utilisé
     */
    @Transactional
    public void updatePatient(Patient patient, String carteNationalPatient, String nomPatient, String prenomPatient, LocalDate dateNaissance) {
        validerPatient(carteNationalPatient, nomPatient, prenomPatient, dateNaissance);

        Patient patientDb = patientRepository.findById(patient.getId())
                .orElseThrow(() -> new IllegalArgumentException("Patient introuvable"));

        String cn = carteNationalPatient.trim().toUpperCase().replace("-", "");

        Patient existingPatient = patientRepository.findByCnPatient(cn);

        if (existingPatient != null && !existingPatient.getId().equals(patientDb.getId())) {
            throw new IllegalArgumentException("Ce CN est déjà utilisé par un autre patient");
        }

        patientDb.setCnPatient(cn);
        patientDb.setNomPatient(nomPatient.trim());
        patientDb.setPrenomPatient(prenomPatient.trim());
        patientDb.setDateNaissance(dateNaissance);

        patientRepository.saveAndFlush(patientDb);
    }

    /**
     * Vérifie la validité des informations d'un patient.
     * Contrôle notamment la présence des champs obligatoires et le format
     * du numéro de carte nationale marocain.
     *
     * @param carteNationalPatient numéro de carte nationale à vérifier
     * @param nomPatient nom du patient
     * @param prenomPatient prénom du patient
     * @param dateNaissance date de naissance du patient
     * @throws IllegalArgumentException si une information obligatoire est absente
     *                                  ou si le format du numéro de carte nationale est invalide
     */
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

    /**
     * Retourne l'ensemble des patients enregistrés.
     *
     * @return la liste de tous les patients
     */
    @Transactional(readOnly = true)
    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    /**
     * Recherche un patient à partir de son numéro de carte nationale.
     *
     * @param cnPatient numéro de carte nationale du patient recherché
     * @return le patient correspondant, ou null si aucun patient n'est trouvé
     */
    public Patient getPatientByIdCN(String cnPatient) {
        return patientRepository.findByCnPatient(cnPatient);
    }

    /**
     * Supprime un patient s'il n'est associé à aucune intervention.
     *
     * @param patient patient à supprimer
     * @throws IllegalArgumentException si le patient est introuvable
     *                                  ou s'il est lié à une ou plusieurs interventions
     */
    @Transactional
    public void deletePatient(Patient patient) {

        if (patient == null || patient.getId() == null) {
            throw new IllegalArgumentException("Patient introuvable");
        }

        Patient patientDb = patientRepository.findById(patient.getId())
                .orElseThrow(() -> new IllegalArgumentException("Patient introuvable"));

        if (interventionRepository.existsByPatientId(patientDb.getId())) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer ce patient car il est lié à une ou plusieurs interventions"
            );
        }

        patientRepository.delete(patientDb);
    }
}