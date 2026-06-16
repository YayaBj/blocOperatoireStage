package com.example.service.sterilisation.state;

import com.example.entity.enums.*;

public class AutoclaveState implements ProcessusState {

    @Override
    public StatutProcessusSterilisation statut() {
        return StatutProcessusSterilisation.AUTOCLAVE;
    }

    @Override
    public StatutProcessusSterilisation next() {
        return StatutProcessusSterilisation.VALIDATION;
    }

    @Override
    public boolean hasMovement() {
        return true;
    }

    @Override
    public ZoneBoite ancienneZone() {
        return ZoneBoite.CONDITIONNEMENT;
    }

    @Override
    public ZoneBoite nouvelleZone() {
        return ZoneBoite.AUTOCLAVE;
    }

    @Override
    public TypeMouvementBoite typeMouvement() {
        return TypeMouvementBoite.PASSAGE_AUTOCLAVE;
    }

    @Override
    public String commentaireMouvement() {
        return "Boîte envoyée à l’autoclave";
    }
}