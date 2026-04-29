package com.example.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "salle")
public class Salle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_salle", nullable = false)
    private String numeroSalle;

    @Column(name = "type_salle", nullable = false)
    private String typeSalle;

    @Column(name = "statut_salle", nullable = false)
    private String statutSalle;

    // Relation avec Intervention
    @OneToMany(mappedBy = "salle")
    private List<Intervention> interventions;

    // Constructeurs
    public Salle() {}

    public Salle(String numeroSalle, String typeSalle, String statutSalle) {
        this.numeroSalle = numeroSalle;
        this.typeSalle = typeSalle;
        this.statutSalle = statutSalle;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public String getNumeroSalle() {
        return numeroSalle;
    }

    public void setNumeroSalle(String numeroSalle) {
        this.numeroSalle = numeroSalle;
    }

    public String getTypeSalle() {
        return typeSalle;
    }

    public void setTypeSalle(String typeSalle) {
        this.typeSalle = typeSalle;
    }

    public String getStatutSalle() {
        return statutSalle;
    }

    public void setStatutSalle(String statutSalle) {
        this.statutSalle = statutSalle;
    }

    public List<Intervention> getInterventions() {
        return interventions;
    }

    public void setInterventions(List<Intervention> interventions) {
        this.interventions = interventions;
    }
}