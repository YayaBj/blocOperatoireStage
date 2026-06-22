package com.example.views.components;

import com.example.entity.Personnel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Consumer;

public class PersonnelForm extends VerticalLayout {

    private final TextField matricule = new TextField("Matricule");
    private final TextField nomPersonnel = new TextField("Nom");
    private final TextField prenomPersonnel = new TextField("Prénom");
    private final TextField specialite = new TextField("Spécialité");

    public PersonnelForm(Personnel personnel,
                         String submitLabel,
                         Consumer<PersonnelFormData> onSubmit) {

        addClassName("intervention-form");
        setWidthFull();

        configureFields(personnel);

        Button submitBtn = new Button(submitLabel, event -> onSubmit.accept(getData()));
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.addClassName("primary-action");

        Div identitySection = createSection(
                row(matricule),
                row(nomPersonnel, prenomPersonnel),
                row(specialite)
        );

        HorizontalLayout actions = new HorizontalLayout(submitBtn);
        actions.addClassName("form-actions");
        actions.setWidthFull();

        add(identitySection, actions);
    }

    private void configureFields(Personnel personnel) {
        matricule.setPlaceholder("Ex : MED001");
        nomPersonnel.setPlaceholder("Ex : Karimi");
        prenomPersonnel.setPlaceholder("Ex : Omar");
        specialite.setPlaceholder("Ex : Chirurgie");

        if (personnel != null) {
            matricule.setValue(personnel.getMatricule() == null ? "" : personnel.getMatricule());
            nomPersonnel.setValue(personnel.getNomPersonnel() == null ? "" : personnel.getNomPersonnel());
            prenomPersonnel.setValue(personnel.getPrenomPersonnel() == null ? "" : personnel.getPrenomPersonnel());
            specialite.setValue(personnel.getSpecialite() == null ? "" : personnel.getSpecialite());
        }

        matricule.setWidthFull();
        nomPersonnel.setWidthFull();
        prenomPersonnel.setWidthFull();
        specialite.setWidthFull();
    }

    private Div createSection(HorizontalLayout... rows) {
        Div section = new Div();
        section.addClassName("form-section");

        Span titleSpan = new Span("Identité du personnel");
        titleSpan.addClassName("form-section-title");

        Span descriptionSpan = new Span("Renseigner les informations principales du membre du personnel.");
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

    private PersonnelFormData getData() {
        return new PersonnelFormData(
                matricule.getValue(),
                nomPersonnel.getValue(),
                prenomPersonnel.getValue(),
                specialite.getValue()
        );
    }

    public record PersonnelFormData(
            String matricule,
            String nomPersonnel,
            String prenomPersonnel,
            String specialite
    ) {}
}