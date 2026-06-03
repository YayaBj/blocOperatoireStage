package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lot_sterilisation_unite")
public class LotSterilisationUnite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lot_sterilisation_id", nullable = false)
    private LotSterilisation lotSterilisation;

    @ManyToOne
    @JoinColumn(name = "unite_materiel_id", nullable = false)
    private UniteMateriel uniteMateriel;

    public LotSterilisationUnite() {}

    public LotSterilisationUnite(LotSterilisation lotSterilisation, UniteMateriel uniteMateriel) {
        this.lotSterilisation = lotSterilisation;
        this.uniteMateriel = uniteMateriel;
    }

    public Long getId() {
        return id;
    }

    public LotSterilisation getLotSterilisation() {
        return lotSterilisation;
    }

    public void setLotSterilisation(LotSterilisation lotSterilisation) {
        this.lotSterilisation = lotSterilisation;
    }

    public UniteMateriel getUniteMateriel() {
        return uniteMateriel;
    }

    public void setUniteMateriel(UniteMateriel uniteMateriel) {
        this.uniteMateriel = uniteMateriel;
    }
}