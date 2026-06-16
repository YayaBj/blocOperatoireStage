package com.example.service.sterilisation.state;

import com.example.entity.enums.StatutProcessusSterilisation;

public class ProcessusStateFactory {

    private ProcessusStateFactory() {
    }

    public static ProcessusState from(StatutProcessusSterilisation statut) {
        return switch (statut) {
            case EN_ATTENTE -> new EnAttenteState();
            case LAVAGE -> new LavageState();
            case CONDITIONNEMENT -> new ConditionnementState();
            case AUTOCLAVE -> new AutoclaveState();
            case VALIDATION -> new ValidationState();
            case TERMINE -> new TermineState();
            case ECHEC -> new EchecState();
        };
    }
}