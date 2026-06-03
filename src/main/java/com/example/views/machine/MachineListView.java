package com.example.views.machine;

import com.example.entity.Machine;
import com.example.entity.enums.StatutMachine;
import com.example.entity.enums.TypeMachine;
import com.example.service.MachineService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("machines")
@PageTitle("Gestion des Machines")
@Menu(order = 10, icon = "icons/settings.svg", title = "Machines")
public class MachineListView extends VerticalLayout {

    private final MachineService machineService;

    private final TextField searchField;
    private final Grid<Machine> machineGrid;

    public MachineListView(MachineService machineService) {
        this.machineService = machineService;

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par nom, type, statut ou cycle");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("30em");
        searchField.addValueChangeListener(event -> refreshGrid());

        Button createBtn = new Button("Ajouter une machine", event -> openCreateDialog());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(searchField, createBtn);
        toolbar.setWidthFull();
        toolbar.setWrap(true);

        machineGrid = new Grid<>();

        machineGrid.addColumn(Machine::getNom)
                .setHeader("Nom")
                .setSortable(true);

        machineGrid.addColumn(Machine::getTypeMachine)
                .setHeader("Type")
                .setSortable(true);

        machineGrid.addColumn(Machine::getCycleEnCours)
                .setHeader("Cycle en cours")
                .setSortable(true);

        machineGrid.addColumn(Machine::getStatut)
                .setHeader("Statut")
                .setSortable(true);

        machineGrid.addColumn(machine -> machine.getTempsProcessusMinutes() + " min")
                .setHeader("Temps de processus")
                .setSortable(true);

        machineGrid.addColumn(Machine::getDerniereUtilisation)
                .setHeader("Dernière utilisation")
                .setSortable(true);

        machineGrid.addComponentColumn(machine -> {
            Button modifierBtn = new Button("Modifier", event -> openEditDialog(machine));
            Button supprimerBtn = new Button("Supprimer", event -> openDeleteDialog(machine));

            HorizontalLayout actions = new HorizontalLayout(modifierBtn, supprimerBtn);
            actions.setWrap(false);

            return actions;
        }).setHeader("Actions").setWidth("250px").setFlexGrow(0);

        machineGrid.setEmptyStateText("Aucune machine enregistrée");
        machineGrid.setSizeFull();

        setSizeFull();
        getStyle().set("overflow", "auto");

        add(toolbar, machineGrid);
        refreshGrid();
    }

    private void openCreateDialog() {
        openMachineDialog(null);
    }

    private void openEditDialog(Machine machine) {
        openMachineDialog(machine);
    }

    private void openMachineDialog(Machine selectedMachine) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(selectedMachine == null ? "Ajouter une machine" : "Modifier une machine");

        TextField nom = new TextField("Nom");

        ComboBox<TypeMachine> typeMachine = new ComboBox<>("Type de machine");
        typeMachine.setItems(TypeMachine.values());

        IntegerField tempsProcessus = new IntegerField("Temps de processus (minutes)");
        tempsProcessus.setMin(0);

        TextField cycleEnCours = new TextField("Cycle en cours");

        ComboBox<StatutMachine> statut = new ComboBox<>("Statut");
        statut.setItems(StatutMachine.values());

        if (selectedMachine != null) {
            nom.setValue(selectedMachine.getNom() == null ? "" : selectedMachine.getNom());
            typeMachine.setValue(selectedMachine.getTypeMachine());
            tempsProcessus.setValue(selectedMachine.getTempsProcessusMinutes());
            cycleEnCours.setValue(selectedMachine.getCycleEnCours() == null ? "" : selectedMachine.getCycleEnCours());
            statut.setValue(selectedMachine.getStatut());
        }

        Button saveBtn = new Button(selectedMachine == null ? "Enregistrer" : "Modifier", event -> {
            try {
                if (selectedMachine == null) {
                    machineService.createMachine(
                            nom.getValue(),
                            typeMachine.getValue(),
                            tempsProcessus.getValue() == null ? 0 : tempsProcessus.getValue(),
                            cycleEnCours.getValue(),
                            statut.getValue()
                    );

                    Notification.show("Machine ajoutée", 3000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    machineService.updateMachine(
                            selectedMachine,
                            nom.getValue(),
                            typeMachine.getValue(),
                            tempsProcessus.getValue() == null ? 0 : tempsProcessus.getValue(),
                            cycleEnCours.getValue(),
                            statut.getValue()
                    );

                    Notification.show("Machine modifiée", 3000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }

                refreshGrid();
                dialog.close();

            } catch (IllegalArgumentException e) {
                Notification.show(e.getMessage(), 4000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Annuler", event -> dialog.close());

        VerticalLayout layout = new VerticalLayout(
                nom,
                typeMachine,
                tempsProcessus,
                cycleEnCours,
                statut,
                new HorizontalLayout(saveBtn, cancelBtn)
        );

        layout.setWidth("550px");

        dialog.add(layout);
        dialog.open();
    }

    private void openDeleteDialog(Machine machine) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirmation de suppression");

        Button confirmBtn = new Button("Oui, supprimer", event -> {
            try {
                machineService.deleteMachine(machine);
                refreshGrid();
                dialog.close();

                Notification.show("Machine supprimée", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (IllegalArgumentException e) {
                Notification.show(e.getMessage(), 4000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        confirmBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Annuler", event -> dialog.close());

        VerticalLayout layout = new VerticalLayout(
                new Span("Voulez-vous vraiment supprimer la machine : " + machine.getNom() + " ?"),
                new HorizontalLayout(confirmBtn, cancelBtn)
        );

        dialog.add(layout);
        dialog.open();
    }

    private void refreshGrid() {
        String search = searchField.getValue() == null
                ? ""
                : searchField.getValue().trim().toLowerCase();

        machineGrid.setItems(
                machineService.findAll().stream()
                        .filter(machine -> {
                            String nom = machine.getNom() == null ? "" : machine.getNom().toLowerCase();
                            String type = machine.getTypeMachine() == null ? "" : machine.getTypeMachine().name().toLowerCase();
                            String statut = machine.getStatut() == null ? "" : machine.getStatut().name().toLowerCase();
                            String cycle = machine.getCycleEnCours() == null ? "" : machine.getCycleEnCours().toLowerCase();
                            String derniereUtilisation = machine.getDerniereUtilisation() == null ? "" : machine.getDerniereUtilisation().toString();

                            return search.isBlank()
                                    || nom.contains(search)
                                    || type.contains(search)
                                    || statut.contains(search)
                                    || cycle.contains(search)
                                    || derniereUtilisation.contains(search);
                        })
                        .toList()
        );
    }
}