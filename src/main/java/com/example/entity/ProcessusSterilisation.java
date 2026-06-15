package com.example.entity;

import com.example.entity.enums.StatutProcessusSterilisation;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "processus_sterilisation")
public class ProcessusSterilisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutProcessusSterilisation statut;

    @ManyToOne
    @JoinColumn(name = "demande_sterilisation_id", nullable = false)
    private DemandeSterilisation demandeSterilisation;

    @ManyToOne
    @JoinColumn(name = "machine_lavage_id")
    private Machine machineLavage;

    @ManyToOne
    @JoinColumn(name = "machine_autoclave_id")
    private Machine machineAutoclave;

    @Column(name = "commentaire")
    private String commentaire;

    @OneToMany(
            mappedBy = "processus",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<HistoriqueProcessus> historique = new ArrayList<>();

    @OneToMany(mappedBy = "processusSterilisation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IncidentSterilisation> incidents = new ArrayList<>();

    public ProcessusSterilisation() {}

    public ProcessusSterilisation(LocalDateTime dateCreation,
                                  StatutProcessusSterilisation statut,
                                  DemandeSterilisation demandeSterilisation,
                                  Machine machineLavage,
                                  Machine machineAutoclave,
                                  String commentaire) {
        this.dateCreation = dateCreation;
        this.statut = statut;
        this.demandeSterilisation = demandeSterilisation;
        this.machineLavage = machineLavage;
        this.machineAutoclave = machineAutoclave;
        this.commentaire = commentaire;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public StatutProcessusSterilisation getStatut() {
        return statut;
    }

    public void setStatut(StatutProcessusSterilisation statut) {
        this.statut = statut;
    }

    public DemandeSterilisation getDemandeSterilisation() {
        return demandeSterilisation;
    }

    public void setDemandeSterilisation(DemandeSterilisation demandeSterilisation) {
        this.demandeSterilisation = demandeSterilisation;
    }

    public Machine getMachineLavage() {
        return machineLavage;
    }

    public void setMachineLavage(Machine machineLavage) {
        this.machineLavage = machineLavage;
    }

    public Machine getMachineAutoclave() {
        return machineAutoclave;
    }

    public void setMachineAutoclave(Machine machineAutoclave) {
        this.machineAutoclave = machineAutoclave;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public List<HistoriqueProcessus> getHistorique() {
        return historique;
    }

    public void setHistorique(List<HistoriqueProcessus> historique) {
        this.historique = historique;
    }

    public List<IncidentSterilisation> getIncidents() {
        return incidents;
    }

    public void setIncidents(List<IncidentSterilisation> incidents) {
        this.incidents = incidents;
    }
}