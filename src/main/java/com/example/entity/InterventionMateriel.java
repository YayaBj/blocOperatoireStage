package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "intervention_materiel")
public class InterventionMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relation avec Intervention
    @ManyToOne
    @JoinColumn(name = "intervention_id", nullable = false)
    private Intervention intervention;

    // Relation avec UniteMateriel
    @ManyToOne
    @JoinColumn(name = "unite_materiel_id", nullable = false)
    private UniteMateriel uniteMateriel;

    // Constructeurs
    public InterventionMateriel() {}

    public InterventionMateriel(Intervention intervention, UniteMateriel uniteMateriel) {
        this.intervention = intervention;
        this.uniteMateriel = uniteMateriel;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public Intervention getIntervention() {
        return intervention;
    }

    public void setIntervention(Intervention intervention) {
        this.intervention = intervention;
    }

    public UniteMateriel getUniteMateriel() {
        return uniteMateriel;
    }

    public void setUniteMateriel(UniteMateriel uniteMateriel) {
        this.uniteMateriel = uniteMateriel;
    }
}