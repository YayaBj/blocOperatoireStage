package com.example.service.sterilisation.state;

import com.example.entity.enums.*;

public class TermineState implements ProcessusState {

    @Override
    public StatutProcessusSterilisation statut() {
        return StatutProcessusSterilisation.TERMINE;
    }

    @Override
    public boolean finalState() {
        return true;
    }

    @Override
    public boolean hasMovement() {
        return true;
    }

    @Override
    public ZoneBoite ancienneZone() {
        return ZoneBoite.AUTOCLAVE;
    }

    @Override
    public ZoneBoite nouvelleZone() {
        return ZoneBoite.STOCK_STERILE;
    }

    @Override
    public TypeMouvementBoite typeMouvement() {
        return TypeMouvementBoite.RETOUR_STOCK_STERILE;
    }

    @Override
    public String commentaireMouvement() {
        return "Boîte retournée au stock stérile";
    }
}