package com.example.service.sterilisation.state;

import com.example.entity.enums.StatutProcessusSterilisation;

public class ValidationState implements ProcessusState {

    @Override
    public StatutProcessusSterilisation statut() {
        return StatutProcessusSterilisation.VALIDATION;
    }

    @Override
    public StatutProcessusSterilisation next() {
        return StatutProcessusSterilisation.TERMINE;
    }
}