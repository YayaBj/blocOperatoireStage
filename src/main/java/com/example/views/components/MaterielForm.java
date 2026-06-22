package com.example.views.components;

import com.example.entity.Materiel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Consumer;

public class MaterielForm extends VerticalLayout {

    private final TextField nomMateriel = new TextField("Nom du matériel");
    private final TextField typeMateriel = new TextField("Type de matériel");
    private final IntegerField quantiteTotale = new IntegerField("Quantité totale");
    private final IntegerField quantiteDisponible = new IntegerField("Quantité disponible");
    private final IntegerField seuilAlerte = new IntegerField("Seuil d’alerte");

    public MaterielForm(Materiel materiel,
                        String submitLabel,
                        Consumer<MaterielFormData> onSubmit) {

        addClassName("intervention-form");
        setWidthFull();

        configureFields(materiel);

        Button submitBtn = new Button(submitLabel, event -> onSubmit.accept(getData()));
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.addClassName("primary-action");

        Div identitySection = createSection(
                "Identification du matériel",
                "Définir le nom et le type du matériel utilisé au bloc opératoire.",
                row(nomMateriel, typeMateriel)
        );

        Div stockSection = createSection(
                "Gestion du stock",
                "Renseigner les quantités disponibles et le seuil d’alerte.",
                row(quantiteTotale, quantiteDisponible, seuilAlerte)
        );

        HorizontalLayout actions = new HorizontalLayout(submitBtn);
        actions.addClassName("form-actions");
        actions.setWidthFull();

        add(identitySection, stockSection, actions);
    }

    private void configureFields(Materiel materiel) {
        nomMateriel.setPlaceholder("Ex : Scalpel");
        typeMateriel.setPlaceholder("Ex : Instrument chirurgical");

        quantiteTotale.setMin(1);
        quantiteDisponible.setMin(0);
        seuilAlerte.setMin(0);

        if (materiel != null) {
            nomMateriel.setValue(materiel.getNomMateriel() == null ? "" : materiel.getNomMateriel());
            typeMateriel.setValue(materiel.getTypeMateriel() == null ? "" : materiel.getTypeMateriel());

            if (materiel.getStock() != null) {
                quantiteTotale.setValue(materiel.getStock().getQuantiteTotale());
                quantiteDisponible.setValue(materiel.getStock().getQuantiteDisponible());
                seuilAlerte.setValue(materiel.getStock().getSeuilAlerte());
            }

            quantiteTotale.setReadOnly(true);
            quantiteDisponible.setReadOnly(true);
        }

        nomMateriel.setWidthFull();
        typeMateriel.setWidthFull();
        quantiteTotale.setWidthFull();
        quantiteDisponible.setWidthFull();
        seuilAlerte.setWidthFull();
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

    private MaterielFormData getData() {
        return new MaterielFormData(
                nomMateriel.getValue(),
                typeMateriel.getValue(),
                quantiteTotale.getValue(),
                quantiteDisponible.getValue(),
                seuilAlerte.getValue()
        );
    }

    public record MaterielFormData(
            String nomMateriel,
            String typeMateriel,
            Integer quantiteTotale,
            Integer quantiteDisponible,
            Integer seuilAlerte
    ) {}
}