package com.example.service.sterilisation.state;

import com.example.entity.enums.*;

public class ConditionnementState implements ProcessusState {

    @Override
    public StatutProcessusSterilisation statut() {
        return StatutProcessusSterilisation.CONDITIONNEMENT;
    }

    @Override
    public StatutProcessusSterilisation next() {
        return StatutProcessusSterilisation.AUTOCLAVE;
    }

    @Override
    public boolean hasMovement() {
        return true;
    }

    @Override
    public ZoneBoite ancienneZone() {
        return ZoneBoite.LAVAGE;
    }

    @Override
    public ZoneBoite nouvelleZone() {
        return ZoneBoite.CONDITIONNEMENT;
    }

    @Override
    public TypeMouvementBoite typeMouvement() {
        return TypeMouvementBoite.PASSAGE_CONDITIONNEMENT;
    }

    @Override
    public String commentaireMouvement() {
        return "Boîte envoyée au conditionnement";
    }
}