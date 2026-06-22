package com.example.views.components;

import com.example.entity.Patient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;
import java.util.function.Consumer;

public class PatientForm extends VerticalLayout {

    private final TextField cnPatient = new TextField("Carte nationale");
    private final TextField nomPatient = new TextField("Nom");
    private final TextField prenomPatient = new TextField("Prénom");
    private final DatePicker dateNaissance = new DatePicker("Date de naissance");

    public PatientForm(Patient patient,
                       String submitLabel,
                       Consumer<PatientFormData> onSubmit) {

        addClassName("intervention-form");
        setWidthFull();

        configureFields(patient);

        Button submitBtn = new Button(submitLabel, event -> onSubmit.accept(getData()));
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.addClassName("primary-action");

        Div identitySection = createSection(
                row(cnPatient),
                row(nomPatient, prenomPatient),
                row(dateNaissance)
        );

        HorizontalLayout actions = new HorizontalLayout(submitBtn);
        actions.addClassName("form-actions");
        actions.setWidthFull();

        add(identitySection, actions);
    }

    private void configureFields(Patient patient) {
        cnPatient.setPlaceholder("Ex : BK123456");
        nomPatient.setPlaceholder("Ex : Belhaj");
        prenomPatient.setPlaceholder("Ex : Yaniss");

        if (patient != null) {
            cnPatient.setValue(patient.getCnPatient() == null ? "" : patient.getCnPatient());
            nomPatient.setValue(patient.getNomPatient() == null ? "" : patient.getNomPatient());
            prenomPatient.setValue(patient.getPrenomPatient() == null ? "" : patient.getPrenomPatient());
            dateNaissance.setValue(patient.getDateNaissance());
        }

        cnPatient.setWidthFull();
        nomPatient.setWidthFull();
        prenomPatient.setWidthFull();
        dateNaissance.setWidthFull();
    }

    private Div createSection(HorizontalLayout... rows) {
        Div section = new Div();
        section.addClassName("form-section");

        Span titleSpan = new Span("Identité du patient");
        titleSpan.addClassName("form-section-title");

        Span descriptionSpan = new Span("Renseigner les informations principales du patient.");
        descriptionSpan.addClassName("form-section-description");

        section.add(titleSpan, descriptionSpan);

        for (HorizontalLayout row : rows) {
            section.add(row);
        }

        return section;
    }

    private HorizontalLayout row(com.vaadin.flow.component.Component... components) {
        HorizontalLayout row = new HorizontalLayout(components);
        row.addClassName("form-row");
        row.setWidthFull();
        row.setWrap(true);

        for (var component : components) {
            row.setFlexGrow(1, component);
        }

        return row;
    }

    private PatientFormData getData() {
        return new PatientFormData(
                cnPatient.getValue(),
                nomPatient.getValue(),
                prenomPatient.getValue(),
                dateNaissance.getValue()
        );
    }

    public record PatientFormData(
            String cnPatient,
            String nomPatient,
            String prenomPatient,
            LocalDate dateNaissance
    ) {}
}