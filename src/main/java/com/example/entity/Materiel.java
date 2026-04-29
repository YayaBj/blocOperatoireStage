package com.example.entity;

import com.example.entity.enums.EtatMateriel;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "materiel")
public class Materiel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_materiel", nullable = false)
    private String nomMateriel;

    @Column(name = "type_materiel", nullable = false)
    private String typeMateriel;

    @Column(name = "quantite_totale", nullable = false)
    private int quantiteTotale;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat", nullable = false)
    private EtatMateriel etat;

    // Relation avec Stock (1-1)
    @OneToOne
    @JoinColumn(name = "stock_id")
    private Stock stock;

    // Relation avec InterventionMateriel
    @OneToMany(mappedBy = "materiel")
    private List<InterventionMateriel> interventionMateriels;

    // Relation avec Sterilisation
    @OneToMany(mappedBy = "materiel")
    private List<Sterilisation> sterilisationList;

    // Constructeurs
    public Materiel() {}

    public Materiel(String nomMateriel, String typeMateriel, int quantiteTotale, EtatMateriel etat) {
        this.nomMateriel = nomMateriel;
        this.typeMateriel = typeMateriel;
        this.quantiteTotale = quantiteTotale;
        this.etat = etat;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public String getNomMateriel() {
        return nomMateriel;
    }

    public void setNomMateriel(String nomMateriel) {
        this.nomMateriel = nomMateriel;
    }

    public String getTypeMateriel() {
        return typeMateriel;
    }

    public void setTypeMateriel(String typeMateriel) {
        this.typeMateriel = typeMateriel;
    }

    public int getQuantiteTotale() {
        return quantiteTotale;
    }

    public void setQuantiteTotale(int quantiteTotale) {
        this.quantiteTotale = quantiteTotale;
    }

    public EtatMateriel getEtat() {
        return etat;
    }

    public void setEtat(EtatMateriel etat) {
        this.etat = etat;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public List<InterventionMateriel> getInterventionMateriels() {
        return interventionMateriels;
    }

    public void setInterventionMateriels(List<InterventionMateriel> interventionMateriels) {
        this.interventionMateriels = interventionMateriels;
    }

    public List<Sterilisation> getSterilisationList() {
        return sterilisationList;
    }

    public void setSterilisationList(List<Sterilisation> sterilisationList) {
        this.sterilisationList = sterilisationList;
    }
}