package com.example.entity;

import com.example.entity.enums.GraviteIncident;
import com.example.entity.enums.TypeIncidentSterilisation;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "incident_sterilisation")
public class IncidentSterilisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="date_incident", nullable = false)
    private LocalDateTime dateIncident;

    @Enumerated(EnumType.STRING)
    @Column(name="type_incident", nullable = false)
    private TypeIncidentSterilisation typeIncident;

    @Enumerated(EnumType.STRING)
    @Column(name="gravite", nullable = false)
    private GraviteIncident gravite;

    @Column(name="description", length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "processus_sterilisation_id", nullable = false)
    private ProcessusSterilisation processusSterilisation;

    @ManyToOne
    @JoinColumn(name = "machine_id")
    private Machine machine;

    @ManyToOne
    @JoinColumn(name = "unite_materiel_id")
    private UniteMateriel uniteMateriel;

    @ManyToOne
    @JoinColumn(name = "unite_remplacement_id")
    private UniteMateriel uniteRemplacement;

    public IncidentSterilisation() {}

    public IncidentSterilisation(
            LocalDateTime dateIncident,
            TypeIncidentSterilisation typeIncident,
            GraviteIncident gravite,
            String description,
            ProcessusSterilisation processusSterilisation,
            Machine machine,
            UniteMateriel uniteMateriel
    ) {
        this.dateIncident = dateIncident;
        this.typeIncident = typeIncident;
        this.gravite = gravite;
        this.description = description;
        this.processusSterilisation = processusSterilisation;
        this.machine = machine;
        this.uniteMateriel = uniteMateriel;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDateIncident() {
        return dateIncident;
    }

    public void setDateIncident(LocalDateTime dateIncident) {
        this.dateIncident = dateIncident;
    }

    public TypeIncidentSterilisation getTypeIncident() {
        return typeIncident;
    }

    public void setTypeIncident(TypeIncidentSterilisation typeIncident) {
        this.typeIncident = typeIncident;
    }

    public GraviteIncident getGravite() {
        return gravite;
    }

    public void setGravite(GraviteIncident gravite) {
        this.gravite = gravite;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProcessusSterilisation getProcessusSterilisation() {
        return processusSterilisation;
    }

    public void setProcessusSterilisation(ProcessusSterilisation processusSterilisation) {
        this.processusSterilisation = processusSterilisation;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public UniteMateriel getUniteMateriel() {
        return uniteMateriel;
    }

    public void setUniteMateriel(UniteMateriel uniteMateriel) {
        this.uniteMateriel = uniteMateriel;
    }

    public UniteMateriel getUniteRemplacement() {
        return uniteRemplacement;
    }

    public void setUniteRemplacement(UniteMateriel uniteRemplacement) {
        this.uniteRemplacement = uniteRemplacement;
    }
}