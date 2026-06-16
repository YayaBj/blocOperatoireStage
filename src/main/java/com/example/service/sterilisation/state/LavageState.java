package com.example.service.sterilisation.state;

import com.example.entity.enums.*;

public class LavageState implements ProcessusState {

    @Override
    public StatutProcessusSterilisation statut() {
        return StatutProcessusSterilisation.LAVAGE;
    }

    @Override
    public StatutProcessusSterilisation next() {
        return StatutProcessusSterilisation.CONDITIONNEMENT;
    }

    @Override
    public boolean hasMovement() {
        return true;
    }

    @Override
    public ZoneBoite ancienneZone() {
        return ZoneBoite.STOCK_SALE;
    }

    @Override
    public ZoneBoite nouvelleZone() {
        return ZoneBoite.LAVAGE;
    }

    @Override
    public TypeMouvementBoite typeMouvement() {
        return TypeMouvementBoite.PASSAGE_LAVAGE;
    }

    @Override
    public String commentaireMouvement() {
        return "Boîte envoyée au lavage";
    }
}