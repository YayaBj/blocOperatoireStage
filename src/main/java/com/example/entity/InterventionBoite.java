package com.example.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "intervention_boite")
public class InterventionBoite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "intervention_id", nullable = false)
    private Intervention intervention;

    @ManyToOne
    @JoinColumn(name = "boite_chirurgicale_id", nullable = false)
    private BoiteChirurgicale boiteChirurgicale;

    public InterventionBoite() {}

    public InterventionBoite(Intervention intervention, BoiteChirurgicale boiteChirurgicale) {
        this.intervention = intervention;
        this.boiteChirurgicale = boiteChirurgicale;
    }

    public Long getId() {
        return id;
    }

    public Intervention getIntervention() {
        return intervention;
    }

    public void setIntervention(Intervention intervention) {
        this.intervention = intervention;
    }

    public BoiteChirurgicale getBoiteChirurgicale() {
        return boiteChirurgicale;
    }

    public void setBoiteChirurgicale(BoiteChirurgicale boiteChirurgicale) {
        this.boiteChirurgicale = boiteChirurgicale;
    }
}