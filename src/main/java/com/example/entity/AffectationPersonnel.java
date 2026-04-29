package com.example.entity;

import com.example.entity.enums.RoleIntervention;
import jakarta.persistence.*;

@Entity
@Table(name = "affectation_personnel")
public class AffectationPersonnel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_dans_intervention", nullable = false)
    private RoleIntervention roleDansIntervention;

    // Relation avec Intervention
    @ManyToOne
    @JoinColumn(name = "intervention_id", nullable = false)
    private Intervention intervention;

    // Relation avec Personnel
    @ManyToOne
    @JoinColumn(name = "personnel_id", nullable = false)
    private Personnel personnel;

    // Constructeurs
    public AffectationPersonnel() {}

    public AffectationPersonnel(RoleIntervention roleDansIntervention,
                                Intervention intervention,
                                Personnel personnel) {
        this.roleDansIntervention = roleDansIntervention;
        this.intervention = intervention;
        this.personnel = personnel;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public RoleIntervention getRoleDansIntervention() {
        return roleDansIntervention;
    }

    public void setRoleDansIntervention(RoleIntervention roleDansIntervention) {
        this.roleDansIntervention = roleDansIntervention;
    }

    public Intervention getIntervention() {
        return intervention;
    }

    public void setIntervention(Intervention intervention) {
        this.intervention = intervention;
    }

    public Personnel getPersonnel() {
        return personnel;
    }

    public void setPersonnel(Personnel personnel) {
        this.personnel = personnel;
    }
}