package com.example.entity;

import com.example.entity.enums.TypeMouvementBoite;
import com.example.entity.enums.ZoneBoite;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mouvement_boite")
public class MouvementBoite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_mouvement", nullable = false)
    private LocalDateTime dateMouvement;

    @Enumerated(EnumType.STRING)
    @Column(name = "ancienne_zone")
    private ZoneBoite ancienneZone;

    @Enumerated(EnumType.STRING)
    @Column(name = "nouvelle_zone", nullable = false)
    private ZoneBoite nouvelleZone;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_mouvement", nullable = false)
    private TypeMouvementBoite typeMouvement;

    @Column(name = "commentaire")
    private String commentaire;

    @ManyToOne
    @JoinColumn(name = "boite_chirurgicale_id", nullable = false)
    private BoiteChirurgicale boiteChirurgicale;

    @ManyToOne
    @JoinColumn(name = "processus_sterilisation_id")
    private ProcessusSterilisation processusSterilisation;

    public MouvementBoite() {}

    public MouvementBoite(LocalDateTime dateMouvement,
                          ZoneBoite ancienneZone,
                          ZoneBoite nouvelleZone,
                          TypeMouvementBoite typeMouvement,
                          String commentaire,
                          BoiteChirurgicale boiteChirurgicale,
                          ProcessusSterilisation processusSterilisation) {
        this.dateMouvement = dateMouvement;
        this.ancienneZone = ancienneZone;
        this.nouvelleZone = nouvelleZone;
        this.typeMouvement = typeMouvement;
        this.commentaire = commentaire;
        this.boiteChirurgicale = boiteChirurgicale;
        this.processusSterilisation = processusSterilisation;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDateMouvement() {
        return dateMouvement;
    }

    public void setDateMouvement(LocalDateTime dateMouvement) {
        this.dateMouvement = dateMouvement;
    }

    public ZoneBoite getAncienneZone() {
        return ancienneZone;
    }

    public void setAncienneZone(ZoneBoite ancienneZone) {
        this.ancienneZone = ancienneZone;
    }

    public ZoneBoite getNouvelleZone() {
        return nouvelleZone;
    }

    public void setNouvelleZone(ZoneBoite nouvelleZone) {
        this.nouvelleZone = nouvelleZone;
    }

    public TypeMouvementBoite getTypeMouvement() {
        return typeMouvement;
    }

    public void setTypeMouvement(TypeMouvementBoite typeMouvement) {
        this.typeMouvement = typeMouvement;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public BoiteChirurgicale getBoiteChirurgicale() {
        return boiteChirurgicale;
    }

    public void setBoiteChirurgicale(BoiteChirurgicale boiteChirurgicale) {
        this.boiteChirurgicale = boiteChirurgicale;
    }

    public ProcessusSterilisation getProcessusSterilisation() {
        return processusSterilisation;
    }

    public void setProcessusSterilisation(ProcessusSterilisation processusSterilisation) {
        this.processusSterilisation = processusSterilisation;
    }
}