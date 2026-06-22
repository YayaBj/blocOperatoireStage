package com.example.views.components;

import com.example.entity.Machine;
import com.example.entity.enums.GraviteIncident;
import com.example.entity.enums.TypeIncidentSterilisation;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
    private final TextArea description = new TextArea("Description");

    public IncidentProcessusForm(List<Machine> machines,
                                 Consumer<IncidentProcessusFormData> onSubmit) {

        addClassName("intervention-form");
        setWidthFull();

        configureFields(machines);

        Button submitBtn = new Button("Déclarer l’échec", event -> onSubmit.accept(getData()));
        submitBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        submitBtn.addClassName("primary-action");

        Div incidentSection = createSection(
                "Nature de l’incident",
                "Préciser le type d’incident et son niveau de gravité.",
                row(typeIncident, gravite)
        );

        Div machineSection = createSection(
                "Équipement concerné",
                "Associer l’incident à une machine si nécessaire.",
                row(machine)
        );

        Div descriptionSection = createSection(
                "Description de l’incident",
                "Décrire clairement ce qui s’est produit pendant le processus.",
                row(description)
        );

        HorizontalLayout actions = new HorizontalLayout(submitBtn);
        actions.addClassName("form-actions");
        actions.setWidthFull();

        add(incidentSection, machineSection, descriptionSection, actions);
    }

    private void configureFields(List<Machine> machines) {
        typeIncident.setItems(TypeIncidentSterilisation.values());
        gravite.setItems(GraviteIncident.values());

        machine.setItems(machines);
        machine.setItemLabelGenerator(m -> m == null ? "" : m.getNom());
        machine.setClearButtonVisible(true);

        description.setPlaceholder("Ex : Température insuffisante pendant le cycle autoclave");
        description.setWidthFull();
        description.setMinHeight("140px");

        typeIncident.setWidthFull();
        gravite.setWidthFull();
        machine.setWidthFull();
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
                typeIncident.getValue(),
                gravite.getValue(),
                description.getValue()
        );
    }

    public record IncidentProcessusFormData(
            Long machineId,
            TypeIncidentSterilisation typeIncident,
            GraviteIncident gravite,
            String description
    ) {}
}