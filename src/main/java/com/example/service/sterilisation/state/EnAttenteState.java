package com.example.service.sterilisation.state;

import com.example.entity.enums.StatutProcessusSterilisation;

public class EnAttenteState implements ProcessusState {

    @Override
    public StatutProcessusSterilisation statut() {
        return StatutProcessusSterilisation.EN_ATTENTE;
    }

    @Override
    public StatutProcessusSterilisation next() {
        return StatutProcessusSterilisation.LAVAGE;
    }
}