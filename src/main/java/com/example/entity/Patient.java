package com.example.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "patient")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_patient", nullable = false)
    private String nomPatient;

    @Column(name = "prenom_patient", nullable = false)
    private String prenomPatient;

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    // Relation avec Intervention
    @OneToMany(mappedBy = "patient")
    private List<Intervention> interventions;

    // Constructeurs
    public Patient() {}

    public Patient(String nomPatient, String prenomPatient, LocalDate dateNaissance) {
        this.nomPatient = nomPatient;
        this.prenomPatient = prenomPatient;
        this.dateNaissance = dateNaissance;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public String getNomPatient() {
        return nomPatient;
    }

    public void setNomPatient(String nomPatient) {
        this.nomPatient = nomPatient;
    }

    public String getPrenomPatient() {
        return prenomPatient;
    }

    public void setPrenomPatient(String prenomPatient) {
        this.prenomPatient = prenomPatient;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public List<Intervention> getInterventions() {
        return interventions;
    }

    public void setInterventions(List<Intervention> interventions) {
        this.interventions = interventions;
    }
}