package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "boite_materiel")
public class BoiteMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "boite_chirurgicale_id", nullable = false)
    private BoiteChirurgicale boiteChirurgicale;

    @ManyToOne
    @JoinColumn(name = "unite_materiel_id", nullable = false)
    private UniteMateriel uniteMateriel;

    public BoiteMateriel() {}

    public BoiteMateriel(BoiteChirurgicale boiteChirurgicale, UniteMateriel uniteMateriel) {
        this.boiteChirurgicale = boiteChirurgicale;
        this.uniteMateriel = uniteMateriel;
    }

    public Long getId() {
        return id;
    }

    public BoiteChirurgicale getBoiteChirurgicale() {
        return boiteChirurgicale;
    }

    public void setBoiteChirurgicale(BoiteChirurgicale boiteChirurgicale) {
        this.boiteChirurgicale = boiteChirurgicale;
    }

    public UniteMateriel getUniteMateriel() {
        return uniteMateriel;
    }

    public void setUniteMateriel(UniteMateriel uniteMateriel) {
        this.uniteMateriel = uniteMateriel;
    }
}