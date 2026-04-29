package com.example.entity;

import com.example.entity.enums.StatutSterilisation;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "sterilisation")
public class Sterilisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;


    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutSterilisation statut;

    // Relation avec Materiel (ManyToOne)
    @ManyToOne
    @JoinColumn(name = "materiel_id", nullable = false)
    private Materiel materiel;

    // Constructeurs
    public Sterilisation() {}

    public Sterilisation(LocalDate dateDebut, StatutSterilisation statut, Materiel materiel) {
        this.dateDebut = dateDebut;
        this.statut = statut;
        this.materiel = materiel;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public StatutSterilisation getStatut() {
        return statut;
    }

    public void setStatut(StatutSterilisation statut) {
        this.statut = statut;
    }

    public Materiel getMateriel() {
        return materiel;
    }

    public void setMateriel(Materiel materiel) {
        this.materiel = materiel;
    }
}