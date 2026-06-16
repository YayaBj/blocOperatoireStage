package com.example.entity;

import com.example.entity.enums.StatutMachine;
import com.example.entity.enums.TypeMachine;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "machine")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="nom", nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name="type_machine", nullable = false)
    private TypeMachine typeMachine;

    @Column(name="temps_processus_minutes", nullable = false)
    private int tempsProcessusMinutes;

    @Column(name="cycle_en_cours")
    private String cycleEnCours;

    @Enumerated(EnumType.STRING)
    @Column(name="statut", nullable = false)
    private StatutMachine statut;

    @Column(name="derniere_utilisation")
    private LocalDateTime derniereUtilisation;

    public Machine() {}

    public Machine(String nom, TypeMachine typeMachine, int tempsProcessusMinutes,
                   String cycleEnCours, StatutMachine statut) {
        this.nom = nom;
        this.typeMachine = typeMachine;
        this.tempsProcessusMinutes = tempsProcessusMinutes;
        this.cycleEnCours = cycleEnCours;
        this.statut = statut;
    }

    public Long getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public TypeMachine getTypeMachine() { return typeMachine; }
    public void setTypeMachine(TypeMachine typeMachine) { this.typeMachine = typeMachine; }

    public int getTempsProcessusMinutes() { return tempsProcessusMinutes; }
    public void setTempsProcessusMinutes(int tempsProcessusMinutes) {
        this.tempsProcessusMinutes = tempsProcessusMinutes;
    }

    public String getCycleEnCours() { return cycleEnCours; }
    public void setCycleEnCours(String cycleEnCours) { this.cycleEnCours = cycleEnCours; }

    public StatutMachine getStatut() { return statut; }
    public void setStatut(StatutMachine statut) { this.statut = statut; }

    public LocalDateTime getDerniereUtilisation() { return derniereUtilisation; }
    public void setDerniereUtilisation(LocalDateTime derniereUtilisation) {
        this.derniereUtilisation = derniereUtilisation;
    }

    public boolean estUtilisable() {
        return statut != StatutMachine.MAINTENANCE && statut != StatutMachine.ERROR;
    }
}