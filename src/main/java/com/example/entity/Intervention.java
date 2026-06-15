package com.example.entity;

import com.example.entity.enums.PrioriteIntervention;
import com.example.entity.enums.StatutIntervention;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "intervention")
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_intervention", nullable = false)
    private String typeIntervention;

    @Enumerated(EnumType.STRING)
    @Column(name = "priorite", nullable = false)
    private PrioriteIntervention priorite;

    @Column(name = "date_heure_debut", nullable = false)
    private LocalDateTime dateHeureDebut;

    @Column(name = "duree_prevue", nullable = false)
    private int dureePrevue;

    @Column(name = "duree_reelle")
    private int dureeReelle;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutIntervention statutIntervention;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @OneToMany(mappedBy = "intervention", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AffectationPersonnel> affectations = new ArrayList<>();

    @OneToMany(mappedBy = "intervention", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterventionBoite> interventionBoites = new ArrayList<>();

    public Intervention() {}

    public Intervention(String typeIntervention, PrioriteIntervention priorite,
                        LocalDateTime dateHeureDebut, int dureePrevue,
                        StatutIntervention statutIntervention, Patient patient, Salle salle) {
        this.typeIntervention = typeIntervention;
        this.priorite = priorite;
        this.dateHeureDebut = dateHeureDebut;
        this.dureePrevue = dureePrevue;
        this.statutIntervention = statutIntervention;
        this.patient = patient;
        this.salle = salle;
    }


    public Long getId() {
        return id;
    }

    public String getTypeIntervention() {
        return typeIntervention;
    }

    public void setTypeIntervention(String typeIntervention) {
        this.typeIntervention = typeIntervention;
    }

    public PrioriteIntervention getPriorite() {
        return priorite;
    }

    public void setPriorite(PrioriteIntervention priorite) {
        this.priorite = priorite;
    }

    public LocalDateTime getDateHeureDebut() {
        return dateHeureDebut;
    }

    public void setDateHeureDebut(LocalDateTime dateHeureDebut) {
        this.dateHeureDebut = dateHeureDebut;
    }

    public int getDureePrevue() {
        return dureePrevue;
    }

    public void setDureePrevue(int dureePrevue) {
        this.dureePrevue = dureePrevue;
    }

    public int getDureeReelle() {
        return dureeReelle;
    }

    public void setDureeReelle(int dureeReelle) {
        this.dureeReelle = dureeReelle;
    }

    public StatutIntervention getStatutIntervention() {
        return statutIntervention;
    }

    public void setStatutIntervention(StatutIntervention statut) {
        this.statutIntervention = statut;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Salle getSalle() {
        return salle;
    }

    public void setSalle(Salle salle) {
        this.salle = salle;
    }

    public List<AffectationPersonnel> getAffectations() {
        return affectations;
    }

    public void setAffectations(List<AffectationPersonnel> affectations) {
        this.affectations = affectations;
    }

    public List<InterventionBoite> getInterventionBoites() {
        return interventionBoites;
    }

    public void setInterventionBoites(List<InterventionBoite> interventionBoites) {
        this.interventionBoites = interventionBoites;
    }
}