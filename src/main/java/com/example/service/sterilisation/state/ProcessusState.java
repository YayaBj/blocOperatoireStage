package com.example.service.sterilisation.state;

import com.example.entity.enums.StatutProcessusSterilisation;
import com.example.entity.enums.TypeMouvementBoite;
import com.example.entity.enums.ZoneBoite;

public interface ProcessusState {

    StatutProcessusSterilisation statut();

    default StatutProcessusSterilisation next() {
        throw new IllegalArgumentException("Impossible d’avancer depuis l’état " + statut());
    }

    default boolean finalState() {
        return false;
    }

    default boolean hasMovement() {
        return false;
    }

    default ZoneBoite ancienneZone() {
        return null;
    }

    default ZoneBoite nouvelleZone() {
        return null;
    }

    default TypeMouvementBoite typeMouvement() {
        return null;
    }

    default String commentaireMouvement() {
        return null;
    }

    default String commentaireHistorique() {
        return "Passage à l'étape " + statut();
    }
}