package com.example.entity;

import com.example.entity.enums.PrioriteIntervention;
import com.example.entity.enums.StatutBoite;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "boite_chirurgicale")
public class BoiteChirurgicale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_boite", nullable = false, unique = true)
    private String codeBoite;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "priorite", nullable = false)
    private PrioriteIntervention priorite;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutBoite statut;

    @Column(name = "departement")
    private String departement;

    @Column(name = "specialite")
    private String specialite;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @OneToMany(mappedBy = "boiteChirurgicale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoiteMateriel> materiels = new ArrayList<>();

    public BoiteChirurgicale() {}

    public BoiteChirurgicale(String codeBoite, String nom, PrioriteIntervention priorite,
                             StatutBoite statut, String departement, String specialite,
                             LocalDateTime dateCreation) {
        this.codeBoite = codeBoite;
        this.nom = nom;
        this.priorite = priorite;
        this.statut = statut;
        this.departement = departement;
        this.specialite = specialite;
        this.dateCreation = dateCreation;
    }

    public Long getId() {
        return id;
    }

    public String getCodeBoite() {
        return codeBoite;
    }

    public void setCodeBoite(String codeBoite) {
        this.codeBoite = codeBoite;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public PrioriteIntervention getPriorite() {
        return priorite;
    }

    public void setPriorite(PrioriteIntervention priorite) {
        this.priorite = priorite;
    }

    public StatutBoite getStatut() {
        return statut;
    }

    public void setStatut(StatutBoite statut) {
        this.statut = statut;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public List<BoiteMateriel> getMateriels() {
        return materiels;
    }

    public void setMateriels(List<BoiteMateriel> materiels) {
        this.materiels = materiels;
    }
}