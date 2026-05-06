package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stock")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantite_totale", nullable = false)
    private int quantiteTotale;

    @Column(name = "quantite_disponible", nullable = false)
    private int quantiteDisponible;

    @Column(name = "seuil_alerte")
    private int seuilAlerte;

    @OneToOne(mappedBy = "stock")
    private Materiel materiel;

    public Stock() {}

    public Stock(int quantiteTotale, int quantiteDisponible, int seuilAlerte) {
        this.quantiteTotale = quantiteTotale;
        this.quantiteDisponible = quantiteDisponible;
        this.seuilAlerte = seuilAlerte;
    }

    public Long getId() {
        return id;
    }

    public int getQuantiteTotale() {
        return quantiteTotale;
    }

    public void setQuantiteTotale(int quantiteTotale) {
        this.quantiteTotale = quantiteTotale;
    }

    public int getQuantiteDisponible() {
        return quantiteDisponible;
    }

    public void setQuantiteDisponible(int quantiteDisponible) {
        this.quantiteDisponible = quantiteDisponible;
    }

    public int getSeuilAlerte() {
        return seuilAlerte;
    }

    public void setSeuilAlerte(int seuilAlerte) {
        this.seuilAlerte = seuilAlerte;
    }

    public Materiel getMateriel() {
        return materiel;
    }

    public void setMateriel(Materiel materiel) {
        this.materiel = materiel;
    }
}