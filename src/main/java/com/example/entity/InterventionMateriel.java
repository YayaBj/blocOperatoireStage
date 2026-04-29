package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "intervention_materiel")
public class InterventionMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantite", nullable = false)
    private int quantite;

    // Relation avec Intervention
    @ManyToOne
    @JoinColumn(name = "intervention_id", nullable = false)
    private Intervention intervention;

    // Relation avec Materiel
    @ManyToOne
    @JoinColumn(name = "materiel_id", nullable = false)
    private Materiel materiel;

    // Constructeurs
    public InterventionMateriel() {}

    public InterventionMateriel(int quantite,
                                Intervention intervention,
                                Materiel materiel) {
        this.quantite = quantite;
        this.intervention = intervention;
        this.materiel = materiel;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public Intervention getIntervention() {
        return intervention;
    }

    public void setIntervention(Intervention intervention) {
        this.intervention = intervention;
    }

    public Materiel getMateriel() {
        return materiel;
    }

    public void setMateriel(Materiel materiel) {
        this.materiel = materiel;
    }
}