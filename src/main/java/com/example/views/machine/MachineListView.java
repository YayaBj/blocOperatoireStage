package com.example.views.machine;

import com.example.base.ui.ViewTitle;
import com.example.entity.Machine;
import com.example.service.MachineService;
import com.example.views.components.MachineForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("machines")
@PageTitle("Gestion des Machines")
@Menu(order = 9, icon = "icons/settings.svg", title = "Stérilisation/Machines")
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
        createBtn.addClassName("primary-action");

        var titleLine = new HorizontalLayout(new ViewTitle("Gestion des machines"));
        titleLine.setWidthFull();

        var searchLine = new HorizontalLayout(searchField, createBtn);
        searchLine.setWidthFull();
        searchLine.setWrap(true);
        searchLine.setFlexGrow(1, searchField);

        var toolbar = new VerticalLayout(titleLine, searchLine);
        toolbar.addClassName("page-toolbar");
        toolbar.setWidthFull();

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

        machineGrid.addComponentColumn(machine -> {
            Span badge = new Span(machine.getStatut() == null ? "" : machine.getStatut().name());

            if (machine.getStatut() == null) {
                badge.addClassName("status-neutral");
            } else {
                switch (machine.getStatut()) {
                    case IDLE -> badge.addClassName("status-success");
                    case ACTIVE -> badge.addClassName("status-info");
                    case MAINTENANCE -> badge.addClassName("status-neutral");
                    case ERROR -> badge.addClassName("status-danger");
                }
            }

            return badge;
        }).setHeader("Statut");

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
        machineGrid.addClassName("professional-grid");

        toolbar.addClassName("page-toolbar");
        createBtn.addClassName("primary-action");

        setSizeFull();
        addClassName("page-container");
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
        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");

        MachineForm form = new MachineForm(
                selectedMachine,
                selectedMachine == null ? "Ajouter la machine" : "Modifier la machine",
                data -> {
                    try {
                        if (selectedMachine == null) {
                            machineService.createMachine(
                                    data.nom(),
                                    data.typeMachine(),
                                    data.tempsProcessusMinutes() == null ? 0 : data.tempsProcessusMinutes(),
                                    data.cycleEnCours(),
                                    data.statut()
                            );

                            Notification.show("Machine ajoutée", 3000, Notification.Position.BOTTOM_END)
                                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        } else {
                            machineService.updateMachine(
                                    selectedMachine,
                                    data.nom(),
                                    data.typeMachine(),
                                    data.tempsProcessusMinutes() == null ? 0 : data.tempsProcessusMinutes(),
                                    data.cycleEnCours(),
                                    data.statut()
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
                }
        );

        dialog.add(form);
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