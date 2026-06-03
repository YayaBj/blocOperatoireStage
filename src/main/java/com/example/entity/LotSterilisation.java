package com.example.entity;

import com.example.entity.enums.StatutLotSterilisation;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lot_sterilisation")
public class LotSterilisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_lot", nullable = false, unique = true)
    private String codeLot;

    @Column(name = "date_creation", nullable = false)
    private LocalDate dateCreation;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutLotSterilisation statut;

    @ManyToOne
    @JoinColumn(name = "intervention_id", nullable = false)
    private Intervention intervention;

    @OneToMany(mappedBy = "lotSterilisation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LotSterilisationUnite> unites = new ArrayList<>();

    public LotSterilisation() {}

    public LotSterilisation(String codeLot, LocalDate dateCreation,
                            StatutLotSterilisation statut,
                            Intervention intervention) {
        this.codeLot = codeLot;
        this.dateCreation = dateCreation;
        this.statut = statut;
        this.intervention = intervention;
    }

    public Long getId() {
        return id;
    }

    public String getCodeLot() {
        return codeLot;
    }

    public void setCodeLot(String codeLot) {
        this.codeLot = codeLot;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public StatutLotSterilisation getStatut() {
        return statut;
    }

    public void setStatut(StatutLotSterilisation statut) {
        this.statut = statut;
    }

    public Intervention getIntervention() {
        return intervention;
    }

    public void setIntervention(Intervention intervention) {
        this.intervention = intervention;
    }

    public List<LotSterilisationUnite> getUnites() {
        return unites;
    }

    public void setUnites(List<LotSterilisationUnite> unites) {
        this.unites = unites;
    }
}