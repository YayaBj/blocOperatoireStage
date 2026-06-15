package com.example.entity;

import com.example.entity.enums.StatutProcessusSterilisation;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historique_processus")
public class HistoriqueProcessus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateAction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutProcessusSterilisation etape;

    @Column(length = 1000)
    private String commentaire;

    @ManyToOne
    @JoinColumn(name = "processus_id", nullable = false)
    private ProcessusSterilisation processus;

    public HistoriqueProcessus() {
    }

    public HistoriqueProcessus(
            LocalDateTime dateAction,
            StatutProcessusSterilisation etape,
            String commentaire,
            ProcessusSterilisation processus
    ) {
        this.dateAction = dateAction;
        this.etape = etape;
        this.commentaire = commentaire;
        this.processus = processus;
    }

    public LocalDateTime getDateAction() {
        return dateAction;
    }

    public void setDateAction(LocalDateTime dateAction) {
        this.dateAction = dateAction;
    }

    public StatutProcessusSterilisation getEtape() {
        return etape;
    }

    public void setEtape(StatutProcessusSterilisation etape) {
        this.etape = etape;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public ProcessusSterilisation getProcessus() {
        return processus;
    }

    public void setProcessus(ProcessusSterilisation processus) {
        this.processus = processus;
    }
}