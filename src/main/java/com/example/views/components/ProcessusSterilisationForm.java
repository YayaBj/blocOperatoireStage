package com.example.views.components;

import com.example.entity.DemandeSterilisation;
import com.example.entity.Machine;
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

public class ProcessusSterilisationForm extends VerticalLayout {

    private final ComboBox<DemandeSterilisation> demande = new ComboBox<>("Demande acceptée");
    private final ComboBox<Machine> machineLavage = new ComboBox<>("Machine de lavage");
    private final ComboBox<Machine> machineAutoclave = new ComboBox<>("Machine autoclave");
    private final TextArea commentaire = new TextArea("Commentaire");

    public ProcessusSterilisationForm(List<DemandeSterilisation> demandes,
                                      List<Machine> machinesLavage,
                                      List<Machine> machinesAutoclave,
                                      Consumer<ProcessusSterilisationFormData> onSubmit) {

        addClassName("intervention-form");
        setWidthFull();

        configureFields(demandes, machinesLavage, machinesAutoclave);

        Button submitBtn = new Button("Lancer le processus", event -> onSubmit.accept(getData()));
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.addClassName("primary-action");

        Div demandeSection = createSection(
                "Demande à traiter",
                "Sélectionner une demande acceptée à lancer dans le circuit de stérilisation.",
                row(demande)
        );

        Div machineSection = createSection(
                "Machines utilisées",
                "Choisir les machines nécessaires au lavage et à l’autoclave.",
                row(machineLavage, machineAutoclave)
        );

        Div commentaireSection = createSection(
                "Commentaire",
                "Ajouter une précision si nécessaire avant le lancement du processus.",
                row(commentaire)
        );

        HorizontalLayout actions = new HorizontalLayout(submitBtn);
        actions.addClassName("form-actions");
        actions.setWidthFull();

        add(demandeSection, machineSection, commentaireSection, actions);
    }

    private void configureFields(List<DemandeSterilisation> demandes,
                                 List<Machine> machinesLavage,
                                 List<Machine> machinesAutoclave) {

        demande.setItems(demandes);
        demande.setItemLabelGenerator(d ->
                d.getCodeDemande() + " - " + d.getBoiteChirurgicale().getCodeBoite()
        );

        machineLavage.setItems(machinesLavage);
        machineLavage.setItemLabelGenerator(Machine::getNom);

        machineAutoclave.setItems(machinesAutoclave);
        machineAutoclave.setItemLabelGenerator(Machine::getNom);

        commentaire.setPlaceholder("Ex : Cycle lancé après validation de la demande");
        commentaire.setWidthFull();
        commentaire.setMinHeight("120px");

        demande.setWidthFull();
        machineLavage.setWidthFull();
        machineAutoclave.setWidthFull();
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

    private ProcessusSterilisationFormData getData() {
        return new ProcessusSterilisationFormData(
                demande.getValue() == null ? null : demande.getValue().getId(),
                machineLavage.getValue() == null ? null : machineLavage.getValue().getId(),
                machineAutoclave.getValue() == null ? null : machineAutoclave.getValue().getId(),
                commentaire.getValue()
        );
    }

    public record ProcessusSterilisationFormData(
            Long demandeId,
            Long machineLavageId,
            Long machineAutoclaveId,
            String commentaire
    ) {}
}