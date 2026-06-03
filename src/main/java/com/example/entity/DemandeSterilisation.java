package com.example.entity;

import com.example.entity.enums.PrioriteIntervention;
import com.example.entity.enums.StatutDemandeSterilisation;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "demande_sterilisation")
public class DemandeSterilisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_demande", nullable = false, unique = true)
    private String codeDemande;

    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande;

    @Column(name = "date_souhaitee")
    private LocalDate dateSouhaitee;

    @Enumerated(EnumType.STRING)
    @Column(name = "priorite", nullable = false)
    private PrioriteIntervention priorite;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutDemandeSterilisation statut;

    @ManyToOne
    @JoinColumn(name = "boite_chirurgicale_id", nullable = false)
    private BoiteChirurgicale boiteChirurgicale;

    @ManyToOne
    @JoinColumn(name = "intervention_id")
    private Intervention intervention;

    @Column(name = "commentaire")
    private String commentaire;

    public DemandeSterilisation() {}

    public DemandeSterilisation(String codeDemande,
                                LocalDateTime dateDemande,
                                LocalDate dateSouhaitee,
                                PrioriteIntervention priorite,
                                StatutDemandeSterilisation statut,
                                BoiteChirurgicale boiteChirurgicale,
                                Intervention intervention,
                                String commentaire) {
        this.codeDemande = codeDemande;
        this.dateDemande = dateDemande;
        this.dateSouhaitee = dateSouhaitee;
        this.priorite = priorite;
        this.statut = statut;
        this.boiteChirurgicale = boiteChirurgicale;
        this.intervention = intervention;
        this.commentaire = commentaire;
    }

    public Long getId() {
        return id;
    }

    public String getCodeDemande() {
        return codeDemande;
    }

    public void setCodeDemande(String codeDemande) {
        this.codeDemande = codeDemande;
    }

    public LocalDateTime getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(LocalDateTime dateDemande) {
        this.dateDemande = dateDemande;
    }

    public LocalDate getDateSouhaitee() {
        return dateSouhaitee;
    }

    public void setDateSouhaitee(LocalDate dateSouhaitee) {
        this.dateSouhaitee = dateSouhaitee;
    }

    public PrioriteIntervention getPriorite() {
        return priorite;
    }

    public void setPriorite(PrioriteIntervention priorite) {
        this.priorite = priorite;
    }

    public StatutDemandeSterilisation getStatut() {
        return statut;
    }

    public void setStatut(StatutDemandeSterilisation statut) {
        this.statut = statut;
    }

    public BoiteChirurgicale getBoiteChirurgicale() {
        return boiteChirurgicale;
    }

    public void setBoiteChirurgicale(BoiteChirurgicale boiteChirurgicale) {
        this.boiteChirurgicale = boiteChirurgicale;
    }

    public Intervention getIntervention() {
        return intervention;
    }

    public void setIntervention(Intervention intervention) {
        this.intervention = intervention;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}