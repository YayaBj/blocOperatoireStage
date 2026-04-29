package com.example.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "personnel")
public class Personnel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_personnel", nullable = false)
    private String nomPersonnel;

    @Column(name = "prenom_personnel", nullable = false)
    private String prenomPersonnel;

    @Column(name = "specialite", nullable = false)
    private String specialite;

    // Relation avec AffectationPersonnel
    @OneToMany(mappedBy = "personnel")
    private List<AffectationPersonnel> affectations;

    // Constructeurs
    public Personnel() {}

    public Personnel(String nomPersonnel, String prenomPersonnel, String specialite) {
        this.nomPersonnel = nomPersonnel;
        this.prenomPersonnel = prenomPersonnel;
        this.specialite = specialite;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public String getNomPersonnel() {
        return nomPersonnel;
    }

    public void setNomPersonnel(String nomPersonnel) {
        this.nomPersonnel = nomPersonnel;
    }

    public String getPrenomPersonnel() {
        return prenomPersonnel;
    }

    public void setPrenomPersonnel(String prenomPersonnel) {
        this.prenomPersonnel = prenomPersonnel;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public List<AffectationPersonnel> getAffectations() {
        return affectations;
    }

    public void setAffectations(List<AffectationPersonnel> affectations) {
        this.affectations = affectations;
    }
}