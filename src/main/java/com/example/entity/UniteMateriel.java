package com.example.entity;

import com.example.entity.enums.EtatMateriel;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "unite_materiel")
public class UniteMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_inventaire", nullable = false, unique = true)
    private String codeInventaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat", nullable = false)
    private EtatMateriel etat;

    @ManyToOne
    @JoinColumn(name = "materiel_id", nullable = false)
    private Materiel materiel;

    @OneToMany(mappedBy = "uniteMateriel")
    private List<InterventionMateriel> interventionMateriels = new ArrayList<>();

    @OneToMany(mappedBy = "uniteMateriel")
    private List<Sterilisation> sterilisations = new ArrayList<>();

    public UniteMateriel() {}

    public UniteMateriel(String codeInventaire, EtatMateriel etat, Materiel materiel) {
        this.codeInventaire = codeInventaire;
        this.etat = etat;
        this.materiel = materiel;
    }

    // getters / setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeInventaire() {
        return codeInventaire;
    }

    public void setCodeInventaire(String codeInventaire) {
        this.codeInventaire = codeInventaire;
    }

    public EtatMateriel getEtat() {
        return etat;
    }

    public void setEtat(EtatMateriel etat) {
        this.etat = etat;
    }

    public Materiel getMateriel() {
        return materiel;
    }

    public void setMateriel(Materiel materiel) {
        this.materiel = materiel;
    }
}