package com.example.entity;

import com.example.entity.enums.EtatMateriel;
import jakarta.persistence.*;

import java.util.ArrayList;
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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @OneToMany(mappedBy = "materiel", cascade = CascadeType.ALL)
    private List<UniteMateriel> unites = new ArrayList<>();

    @OneToMany(mappedBy = "materiel")
    private List<InterventionMateriel> interventionMateriels = new ArrayList<>();

    @OneToMany(mappedBy = "materiel")
    private List<Sterilisation> sterilisationList = new ArrayList<>();

    public Materiel() {}

    public Materiel(String nomMateriel, String typeMateriel, Stock stock) {
        this.nomMateriel = nomMateriel;
        this.typeMateriel = typeMateriel;
        this.stock = stock;
    }

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

    public List<UniteMateriel> getUnites() {
        return unites;
    }

    public void setUnites(List<UniteMateriel> unites) {
        this.unites = unites;
    }
}