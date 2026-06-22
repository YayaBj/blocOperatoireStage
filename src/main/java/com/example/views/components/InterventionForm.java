package com.example.views.components;

import com.example.entity.*;
import com.example.entity.enums.PrioriteIntervention;
import com.example.entity.enums.RoleIntervention;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class InterventionForm extends VerticalLayout {

    private final TextField typeIntervention = new TextField("Type d’intervention");
    private final ComboBox<PrioriteIntervention> priorite = new ComboBox<>("Priorité");
    private final DateTimePicker dateHeureDebut = new DateTimePicker("Date et heure");
    private final IntegerField dureePrevue = new IntegerField("Durée prévue");
    private final ComboBox<Patient> patient = new ComboBox<>("Patient");
    private final ComboBox<Salle> salle = new ComboBox<>("Salle");
    private final MultiSelectComboBox<Personnel> personnels = new MultiSelectComboBox<>("Personnel");
    private final MultiSelectComboBox<BoiteChirurgicale> boites = new MultiSelectComboBox<>("Boîtes chirurgicales");

    public InterventionForm(List<Patient> patients,
                            List<Salle> salles,
                            List<Personnel> personnelsList,
                            List<BoiteChirurgicale> boitesDisponibles,
                            LocalDateTime debutInitial,
                            Integer dureeInitiale,
                            BiConsumer<InterventionFormData, Map<Long, RoleIntervention>> onSubmit) {

        addClassName("intervention-form");
        setWidthFull();

        configureFields(patients, salles, personnelsList, boitesDisponibles, debutInitial, dureeInitiale);

        Button submitBtn = new Button("Créer l’intervention", event -> {
            RolePersonnelDialog dialog = new RolePersonnelDialog(
                    personnels.getValue(),
                    roles -> onSubmit.accept(getData(), roles)
            );
            dialog.open();
        });
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.addClassName("primary-action");

        Div generalSection = createSection(
                "Informations générales",
                "Renseigner le type, la priorité et la durée prévue de l’intervention.",
                row(typeIntervention),
                row(priorite, dateHeureDebut, dureePrevue)
        );

        Div patientSection = createSection(
                "Patient et salle",
                "Associer l’intervention à un patient et à une salle opératoire.",
                row(patient, salle)
        );

        Div resourcesSection = createSection(
                "Ressources opératoires",
                "Sélectionner l’équipe et les boîtes chirurgicales nécessaires.",
                row(personnels),
                row(boites)
        );

        generalSection.setWidthFull();
        patientSection.setWidthFull();
        resourcesSection.setWidthFull();

        HorizontalLayout actions = new HorizontalLayout(submitBtn);
        actions.addClassName("form-actions");
        actions.setWidthFull();

        add(generalSection, patientSection, resourcesSection, actions);
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

    private void configureFields(List<Patient> patients,
                                 List<Salle> salles,
                                 List<Personnel> personnelsList,
                                 List<BoiteChirurgicale> boitesDisponibles,
                                 LocalDateTime debutInitial,
                                 Integer dureeInitiale) {

        typeIntervention.setPlaceholder("Ex : Appendicectomie");
        priorite.setItems(PrioriteIntervention.values());

        if (debutInitial != null) {
            dateHeureDebut.setValue(debutInitial);
        }

        dureePrevue.setPlaceholder("Minutes");
        dureePrevue.setSuffixComponent(new Span("min"));
        dureePrevue.setMin(1);

        if (dureeInitiale != null) {
            dureePrevue.setValue(dureeInitiale);
        }

        patient.setItems(patients);
        patient.setItemLabelGenerator(p -> p.getCnPatient() + " - " + p.getNomPatient() + " " + p.getPrenomPatient());

        salle.setItems(salles);
        salle.setItemLabelGenerator(s -> s.getNumeroSalle() + " - " + s.getTypeSalle());

        personnels.setItems(personnelsList);
        personnels.setItemLabelGenerator(p -> p.getMatricule() + " - " + p.getNomPersonnel() + " " + p.getPrenomPersonnel());

        boites.setItems(boitesDisponibles);
        boites.setItemLabelGenerator(b -> b.getCodeBoite() + " - " + b.getNom());

        typeIntervention.setWidthFull();
        priorite.setWidthFull();
        dateHeureDebut.setWidthFull();
        dureePrevue.setWidthFull();
        patient.setWidthFull();
        salle.setWidthFull();
        personnels.setWidthFull();
        boites.setWidthFull();
    }

    public void refreshBoites(List<BoiteChirurgicale> boitesDisponibles) {
        boites.clear();
        boites.setItems(boitesDisponibles);
    }

    public void clearForm() {
        typeIntervention.clear();
        priorite.clear();
        dateHeureDebut.clear();
        dureePrevue.clear();
        patient.clear();
        salle.clear();
        personnels.clear();
        boites.clear();
    }

    private InterventionFormData getData() {
        return new InterventionFormData(
                typeIntervention.getValue(),
                priorite.getValue(),
                dateHeureDebut.getValue(),
                dureePrevue.getValue(),
                patient.getValue() == null ? null : patient.getValue().getId(),
                salle.getValue() == null ? null : salle.getValue().getId(),
                boites.getValue().stream()
                        .map(BoiteChirurgicale::getId)
                        .toList()
        );
    }

    public record InterventionFormData(
            String typeIntervention,
            PrioriteIntervention priorite,
            LocalDateTime dateHeureDebut,
            Integer dureePrevue,
            Long patientId,
            Long salleId,
            List<Long> boiteIds
    ) {}
}