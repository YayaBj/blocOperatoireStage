package com.example.entity;

import com.example.entity.enums.EtatPersonnel;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "personnel")
public class Personnel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matricule", nullable = false, unique = true)
    private String matricule;

    @Column(name = "nom_personnel", nullable = false)
    private String nomPersonnel;

    @Column(name = "prenom_personnel", nullable = false)
    private String prenomPersonnel;

    @Column(name = "specialite", nullable = false)
    private String specialite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtatPersonnel etat;

    @OneToMany(mappedBy = "personnel")
    private List<AffectationPersonnel> affectations;

    public Personnel() {}

    public Personnel(String matricule, String nomPersonnel, String prenomPersonnel, String specialite, EtatPersonnel etat) {
        this.matricule = matricule;
        this.nomPersonnel = nomPersonnel;
        this.prenomPersonnel = prenomPersonnel;
        this.specialite = specialite;
        this.etat = etat;
    }

    public Long getId() {
        return id;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
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

    public EtatPersonnel getEtat() {
        return etat;
    }

    public void setEtat(EtatPersonnel etat) {
        this.etat = etat;
    }
}