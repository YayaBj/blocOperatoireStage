package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stock")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantite_disponible", nullable = false)
    private int quantiteDisponible;

    // Relation avec Materiel (1-1)
    @OneToOne(mappedBy = "stock")
    private Materiel materiel;

    // Constructeurs
    public Stock() {}

    public Stock(int quantiteDisponible) {
        this.quantiteDisponible = quantiteDisponible;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public int getQuantiteDisponible() {
        return quantiteDisponible;
    }

    public void setQuantiteDisponible(int quantiteDisponible) {
        this.quantiteDisponible = quantiteDisponible;
    }

    public Materiel getMateriel() {
        return materiel;
    }

    public void setMateriel(Materiel materiel) {
        this.materiel = materiel;
    }
}