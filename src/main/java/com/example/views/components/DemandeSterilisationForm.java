package com.example.views.components;

import com.example.entity.BoiteChirurgicale;
import com.example.entity.Intervention;
import com.example.entity.enums.PrioriteIntervention;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public class DemandeSterilisationForm extends VerticalLayout {

    private final TextField codeDemande = new TextField("Code demande");
    private final DatePicker dateSouhaitee = new DatePicker("Date souhaitée");
    private final ComboBox<PrioriteIntervention> priorite = new ComboBox<>("Priorité");
    private final ComboBox<BoiteChirurgicale> boite = new ComboBox<>("Boîte chirurgicale");
    private final ComboBox<Intervention> intervention = new ComboBox<>("Intervention liée");
    private final TextArea commentaire = new TextArea("Commentaire");

    public DemandeSterilisationForm(List<BoiteChirurgicale> boites,
                                    List<Intervention> interventions,
                                    Consumer<DemandeSterilisationFormData> onSubmit) {

        addClassName("intervention-form");
        setWidthFull();

        configureFields(boites, interventions);

        Button submitBtn = new Button("Créer la demande", event -> onSubmit.accept(getData()));
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.addClassName("primary-action");

        Div identitySection = createSection(
                "Informations de la demande",
                "Renseigner le code, la date souhaitée et la priorité de la demande.",
                row(codeDemande),
                row(dateSouhaitee, priorite)
        );

        Div sourceSection = createSection(
                "Origine de la demande",
                "Associer la demande à une boîte chirurgicale et éventuellement à une intervention.",
                row(boite),
                row(intervention)
        );

        Div commentSection = createSection(
                "Commentaire",
                "Ajouter une précision si nécessaire pour le service de stérilisation.",
                row(commentaire)
        );

        HorizontalLayout actions = new HorizontalLayout(submitBtn);
        actions.addClassName("form-actions");
        actions.setWidthFull();

        add(identitySection, sourceSection, commentSection, actions);
    }

    private void configureFields(List<BoiteChirurgicale> boites,
                                 List<Intervention> interventions) {
        codeDemande.setPlaceholder("Ex : DS-2026-001");

        priorite.setItems(PrioriteIntervention.values());

        boite.setItems(boites);
        boite.setItemLabelGenerator(b -> b.getCodeBoite() + " - " + b.getNom());

        intervention.setItems(interventions);
        intervention.setItemLabelGenerator(i ->
                "#" + i.getId() + " - " + i.getTypeIntervention()
        );
        intervention.setClearButtonVisible(true);

        commentaire.setPlaceholder("Ex : Demande urgente après intervention");
        commentaire.setWidthFull();
        commentaire.setMinHeight("120px");

        codeDemande.setWidthFull();
        dateSouhaitee.setWidthFull();
        priorite.setWidthFull();
        boite.setWidthFull();
        intervention.setWidthFull();
    }

    private Div createSection(String title, String description, HorizontalLayout... rows) {
        Div section = new Div();
        section.addClassName("form-section");

        Span titleSpan = new Span(title);
        titleSpan.addClassName("form-section-title");

        Span descriptionSpan = new Span(description);
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

    private DemandeSterilisationFormData getData() {
        return new DemandeSterilisationFormData(
                codeDemande.getValue(),
                dateSouhaitee.getValue(),
                priorite.getValue(),
                boite.getValue() == null ? null : boite.getValue().getId(),
                intervention.getValue() == null ? null : intervention.getValue().getId(),
                commentaire.getValue()
        );
    }

    public record DemandeSterilisationFormData(
            String codeDemande,
            LocalDate dateSouhaitee,
            PrioriteIntervention priorite,
            Long boiteId,
            Long interventionId,
            String commentaire
    ) {}
}