package com.example.views.components;

import com.example.entity.Machine;
import com.example.entity.UniteMateriel;
import com.example.entity.enums.GraviteIncident;
import com.example.entity.enums.TypeIncidentSterilisation;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.List;
import java.util.function.Consumer;

public class IncidentProcessusForm extends VerticalLayout {

    private final ComboBox<TypeIncidentSterilisation> typeIncident = new ComboBox<>("Type d’incident");
    private final ComboBox<GraviteIncident> gravite = new ComboBox<>("Gravité");
    private final ComboBox<Machine> machine = new ComboBox<>("Machine concernée");
    private final ComboBox<UniteMateriel> uniteMateriel = new ComboBox<>("Unité de matériel concernée");
    private final TextArea description = new TextArea("Description");
    private final Checkbox arreterProcessus = new Checkbox("Arrêter le processus");

    public IncidentProcessusForm(
            List<Machine> machines,
            List<UniteMateriel> unitesMateriel,
            Consumer<IncidentProcessusFormData> onSubmit
    ) {

        addClassName("intervention-form");
        setWidthFull();

        configureFields(machines, unitesMateriel);

        Button submitBtn = new Button("Enregistrer l'incident", event -> onSubmit.accept(getData()));
        submitBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        submitBtn.addClassName("primary-action");

        Div incidentSection = createSection(
                "Nature de l’incident",
                "Préciser le type d’incident et son niveau de gravité.",
                row(typeIncident, gravite)
        );

        Div equipmentSection = createSection(
                "Équipement concerné",
                "Associer l’incident à une machine ou une unité de matériel si nécessaire.",
                row(machine, uniteMateriel, arreterProcessus)
        );

        Div descriptionSection = createSection(
                "Description de l’incident",
                "Décrire clairement ce qui s’est produit pendant le processus.",
                row(description)
        );

        HorizontalLayout actions = new HorizontalLayout(submitBtn);
        actions.addClassName("form-actions");
        actions.setWidthFull();

        add(incidentSection, equipmentSection, descriptionSection, actions);
    }

    private void configureFields(
            List<Machine> machines,
            List<UniteMateriel> unitesMateriel
    ) {
        typeIncident.setItems(TypeIncidentSterilisation.values());
        gravite.setItems(GraviteIncident.values());

        machine.setItems(machines);
        machine.setItemLabelGenerator(m -> m == null ? "" : m.getNom());
        machine.setClearButtonVisible(true);

        description.setPlaceholder("Ex : Température insuffisante pendant le cycle autoclave");
        description.setWidthFull();
        description.setMinHeight("140px");

        uniteMateriel.setItems(unitesMateriel);

        uniteMateriel.setItemLabelGenerator(unite ->
                unite.getCodeInventaire()
                        + " - "
                        + unite.getMateriel().getNomMateriel()
        );

        uniteMateriel.setClearButtonVisible(true);
        uniteMateriel.setWidthFull();

        typeIncident.setWidthFull();
        gravite.setWidthFull();
        machine.setWidthFull();
        machine.setVisible(false);
        uniteMateriel.setVisible(false);
        arreterProcessus.setVisible(false);
        arreterProcessus.setValue(false);

        typeIncident.addValueChangeListener(event -> {
            TypeIncidentSterilisation type = event.getValue();

            boolean incidentMachine =
                    type == TypeIncidentSterilisation.PANNE_MACHINE;

            boolean incidentMateriel =
                    type == TypeIncidentSterilisation.MATERIEL_MANQUANT
                            || type == TypeIncidentSterilisation.MATERIEL_CASSE
                            || type == TypeIncidentSterilisation.MATERIEL_ENDOMMAGE
                            || type == TypeIncidentSterilisation.MATERIEL_PERDU;

            machine.setVisible(incidentMachine);
            uniteMateriel.setVisible(incidentMateriel);

            arreterProcessus.setVisible(incidentMateriel);

            if (!incidentMachine) {
                machine.clear();
            }

            if (!incidentMateriel) {
                arreterProcessus.setValue(false);
                uniteMateriel.clear();
            }
        });

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

    private HorizontalLayout row(Component... components) {
        HorizontalLayout row = new HorizontalLayout(components);
        row.addClassName("form-row");
        row.setWidthFull();
        row.setWrap(true);

        for (var component : components) {
            row.setFlexGrow(1, component);
        }

        return row;
    }

    private IncidentProcessusFormData getData() {
        return new IncidentProcessusFormData(
                machine.getValue() == null ? null : machine.getValue().getId(),
                uniteMateriel.getValue() == null ? null : uniteMateriel.getValue().getId(),
                typeIncident.getValue(),
                gravite.getValue(),
                description.getValue(),
                arreterProcessus.getValue()
        );
    }

    public record IncidentProcessusFormData(
            Long machineId,
            Long uniteMaterielId,
            TypeIncidentSterilisation typeIncident,
            GraviteIncident gravite,
            String description,
            boolean arreterProcessus
    ) {}
}