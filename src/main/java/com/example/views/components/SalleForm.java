package com.example.views.components;

import com.example.entity.Salle;
import com.example.entity.enums.StatutSalle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Consumer;

public class SalleForm extends VerticalLayout {

    private final TextField numeroSalle = new TextField("Numéro de salle");
    private final TextField typeSalle = new TextField("Type de salle");
    private final ComboBox<StatutSalle> statutSalle = new ComboBox<>("Statut");

    public SalleForm(Salle salle,
                     String submitLabel,
                     Consumer<SalleFormData> onSubmit) {

        addClassName("intervention-form");
        setWidthFull();

        configureFields(salle);

        Button submitBtn = new Button(submitLabel, event -> onSubmit.accept(getData()));
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.addClassName("primary-action");

        Div identitySection = createSection(
                row(numeroSalle, typeSalle),
                row(statutSalle)
        );

        HorizontalLayout actions = new HorizontalLayout(submitBtn);
        actions.addClassName("form-actions");
        actions.setWidthFull();

        add(identitySection, actions);
    }

    private void configureFields(Salle salle) {
        numeroSalle.setPlaceholder("Ex : SALLE-01");
        typeSalle.setPlaceholder("Ex : Chirurgie générale");

        statutSalle.setItems(StatutSalle.values());

        if (salle != null) {
            numeroSalle.setValue(salle.getNumeroSalle() == null ? "" : salle.getNumeroSalle());
            typeSalle.setValue(salle.getTypeSalle() == null ? "" : salle.getTypeSalle());
            statutSalle.setValue(salle.getStatutSalle());
        }

        numeroSalle.setWidthFull();
        typeSalle.setWidthFull();
        statutSalle.setWidthFull();
    }

    private Div createSection(HorizontalLayout... rows) {
        Div section = new Div();
        section.addClassName("form-section");

        Span titleSpan = new Span("Identification de la salle");
        titleSpan.addClassName("form-section-title");

        Span descriptionSpan = new Span("Renseigner les informations principales de la salle opératoire.");
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

    private SalleFormData getData() {
        return new SalleFormData(
                numeroSalle.getValue(),
                typeSalle.getValue(),
                statutSalle.getValue()
        );
    }

    public record SalleFormData(
            String numeroSalle,
            String typeSalle,
            StatutSalle statutSalle
    ) {}
}