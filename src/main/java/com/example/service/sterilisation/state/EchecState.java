package com.example.service.sterilisation.state;

import com.example.entity.enums.*;

public class EchecState implements ProcessusState {

    @Override
    public StatutProcessusSterilisation statut() {
        return StatutProcessusSterilisation.ECHEC;
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
    public ZoneBoite nouvelleZone() {
        return ZoneBoite.QUARANTAINE;
    }

    @Override
    public TypeMouvementBoite typeMouvement() {
        return TypeMouvementBoite.MISE_QUARANTAINE;
    }

    @Override
    public String commentaireMouvement() {
        return "Boîte mise en quarantaine après échec";
    }

    @Override
    public String commentaireHistorique() {
        return "Processus marqué en échec";
    }
}