package com.example.views.components;

import com.example.entity.BoiteChirurgicale;
import com.example.entity.UniteMateriel;
import com.example.entity.enums.PrioriteIntervention;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;
import java.util.function.Consumer;

public class BoiteForm extends VerticalLayout {

    private final TextField codeBoite = new TextField("Code boîte");
    private final TextField nom = new TextField("Nom de la boîte");
    private final TextField departement = new TextField("Département");
    private final TextField specialite = new TextField("Spécialité");
    private final ComboBox<PrioriteIntervention> priorite = new ComboBox<>("Priorité");
    private final MultiSelectComboBox<UniteMateriel> materiels = new MultiSelectComboBox<>("Matériels");

    public BoiteForm(List<UniteMateriel> unitesDisponibles,
                     BoiteChirurgicale boite,
                     List<Long> selectedUniteIds,
                     String submitLabel,
                     Consumer<BoiteFormData> onSubmit) {

        addClassName("intervention-form");
        setWidthFull();

        configureFields(unitesDisponibles, boite, selectedUniteIds);

        Button submitBtn = new Button(submitLabel, event -> onSubmit.accept(getData()));
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.addClassName("primary-action");

        Div identitySection = createSection(
                "Identification de la boîte",
                "Renseigner le code, le nom et le niveau de priorité de la boîte chirurgicale.",
                row(codeBoite, nom),
                row(priorite)
        );

        Div serviceSection = createSection(
                "Affectation médicale",
                "Associer la boîte à un département et à une spécialité médicale.",
                row(departement, specialite)
        );

        Div contentSection = createSection(
                "Composition de la boîte",
                "Sélectionner les unités de matériel qui composent cette boîte.",
                row(materiels)
        );

        HorizontalLayout actions = new HorizontalLayout(submitBtn);
        actions.addClassName("form-actions");
        actions.setWidthFull();

        add(identitySection, serviceSection, contentSection, actions);
    }

    private void configureFields(List<UniteMateriel> unitesDisponibles,
                                 BoiteChirurgicale boite,
                                 List<Long> selectedUniteIds) {

        codeBoite.setPlaceholder("Ex : BOX-ORTH-001");
        nom.setPlaceholder("Ex : Boîte orthopédie");
        departement.setPlaceholder("Ex : Bloc opératoire");
        specialite.setPlaceholder("Ex : Orthopédie");

        priorite.setItems(PrioriteIntervention.values());

        materiels.setItems(unitesDisponibles);
        materiels.setItemLabelGenerator(unite ->
                unite.getCodeInventaire() + " - " + unite.getMateriel().getNomMateriel()
        );

        if (boite != null) {
            codeBoite.setValue(boite.getCodeBoite() == null ? "" : boite.getCodeBoite());
            nom.setValue(boite.getNom() == null ? "" : boite.getNom());
            departement.setValue(boite.getDepartement() == null ? "" : boite.getDepartement());
            specialite.setValue(boite.getSpecialite() == null ? "" : boite.getSpecialite());
            priorite.setValue(boite.getPriorite());

            materiels.select(
                    unitesDisponibles.stream()
                            .filter(unite -> selectedUniteIds.contains(unite.getId()))
                            .toList()
            );
        }

        codeBoite.setWidthFull();
        nom.setWidthFull();
        departement.setWidthFull();
        specialite.setWidthFull();
        priorite.setWidthFull();
        materiels.setWidthFull();
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

    private BoiteFormData getData() {
        return new BoiteFormData(
                codeBoite.getValue(),
                nom.getValue(),
                priorite.getValue(),
                departement.getValue(),
                specialite.getValue(),
                materiels.getValue().stream()
                        .map(UniteMateriel::getId)
                        .toList()
        );
    }

    public record BoiteFormData(
            String codeBoite,
            String nom,
            PrioriteIntervention priorite,
            String departement,
            String specialite,
            List<Long> uniteMaterielIds
    ) {}
}