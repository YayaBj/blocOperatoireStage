package com.example.views.components;

import com.example.entity.Machine;
import com.example.entity.enums.StatutMachine;
import com.example.entity.enums.TypeMachine;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;

import java.util.function.Consumer;

public class MachineForm extends VerticalLayout {

    private final TextField nom = new TextField("Nom de la machine");
    private final ComboBox<TypeMachine> typeMachine = new ComboBox<>("Type de machine");
    private final IntegerField tempsProcessus = new IntegerField("Temps de processus");
    private final TextField cycleEnCours = new TextField("Cycle en cours");
    private final ComboBox<StatutMachine> statut = new ComboBox<>("Statut");

    public MachineForm(Machine machine,
                       String submitLabel,
                       Consumer<MachineFormData> onSubmit) {

        addClassName("intervention-form");
        setWidthFull();

        configureFields(machine);

        Button submitBtn = new Button(submitLabel, event -> onSubmit.accept(getData()));
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.addClassName("primary-action");

        Div identitySection = createSection(
                "Identification de la machine",
                "Définir le nom et le type de machine utilisée dans le circuit de stérilisation.",
                row(nom, typeMachine)
        );

        Div processSection = createSection(
                "Paramètres du cycle",
                "Renseigner la durée moyenne du cycle et le cycle actuellement en cours si nécessaire.",
                row(tempsProcessus),
                row(cycleEnCours)
        );

        Div statusSection = createSection(
                "État opérationnel",
                "Indiquer si la machine est disponible, active, en maintenance ou en erreur.",
                row(statut)
        );

        HorizontalLayout actions = new HorizontalLayout(submitBtn);
        actions.addClassName("form-actions");
        actions.setWidthFull();

        add(identitySection, processSection, statusSection, actions);
    }

    private void configureFields(Machine machine) {
        nom.setPlaceholder("Ex : Autoclave vapeur 01");

        typeMachine.setItems(TypeMachine.values());

        tempsProcessus.setPlaceholder("Minutes");
        tempsProcessus.setSuffixComponent(new Span("min"));
        tempsProcessus.setMin(0);

        cycleEnCours.setPlaceholder("Ex : Processus demande DS-001");

        statut.setItems(StatutMachine.values());

        if (machine != null) {
            nom.setValue(machine.getNom() == null ? "" : machine.getNom());
            typeMachine.setValue(machine.getTypeMachine());
            tempsProcessus.setValue(machine.getTempsProcessusMinutes());
            cycleEnCours.setValue(machine.getCycleEnCours() == null ? "" : machine.getCycleEnCours());
            statut.setValue(machine.getStatut());
        }

        nom.setWidthFull();
        typeMachine.setWidthFull();
        tempsProcessus.setWidthFull();
        cycleEnCours.setWidthFull();
        statut.setWidthFull();
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

    private MachineFormData getData() {
        return new MachineFormData(
                nom.getValue(),
                typeMachine.getValue(),
                tempsProcessus.getValue(),
                cycleEnCours.getValue(),
                statut.getValue()
        );
    }

    public record MachineFormData(
            String nom,
            TypeMachine typeMachine,
            Integer tempsProcessusMinutes,
            String cycleEnCours,
            StatutMachine statut
    ) {}
}