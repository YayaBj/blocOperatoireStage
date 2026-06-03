package com.example.views.sterilisation;

import com.example.entity.DemandeSterilisation;
import com.example.entity.Machine;
import com.example.entity.ProcessusSterilisation;
import com.example.entity.enums.StatutDemandeSterilisation;
import com.example.entity.enums.StatutProcessusSterilisation;
import com.example.entity.enums.TypeMachine;
import com.example.service.DemandeSterilisationService;
import com.example.service.MachineService;
import com.example.service.ProcessusSterilisationService;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("processus-sterilisation")
@PageTitle("Processus de stérilisation")
@Menu(order = 11, icon = "icons/refresh.svg", title = "Processus stérilisation")
public class ProcessusSterilisationListView extends VerticalLayout {

    private final ProcessusSterilisationService processusService;
    private final DemandeSterilisationService demandeService;
    private final MachineService machineService;

    private final TextField searchField;
    private final Grid<ProcessusSterilisation> processusGrid;

    public ProcessusSterilisationListView(ProcessusSterilisationService processusService,
                                          DemandeSterilisationService demandeService,
                                          MachineService machineService) {
        this.processusService = processusService;
        this.demandeService = demandeService;
        this.machineService = machineService;

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par demande, boîte, statut ou machine");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("30em");
        searchField.addValueChangeListener(event -> refreshGrid());

        Button createBtn = new Button("Lancer processus", event -> openCreateDialog());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(searchField, createBtn);
        toolbar.setWidthFull();
        toolbar.setWrap(true);

        processusGrid = new Grid<>();

        processusGrid.addColumn(p -> p.getDemandeSterilisation().getCodeDemande())
                .setHeader("Demande")
                .setSortable(true);

        processusGrid.addColumn(p -> p.getDemandeSterilisation().getBoiteChirurgicale().getCodeBoite())
                .setHeader("Boîte")
                .setSortable(true);

        processusGrid.addColumn(ProcessusSterilisation::getStatut)
                .setHeader("Étape actuelle")
                .setSortable(true);

        processusGrid.addColumn(p -> p.getMachineLavage() == null ? "" : p.getMachineLavage().getNom())
                .setHeader("Machine lavage")
                .setSortable(true);

        processusGrid.addColumn(p -> p.getMachineAutoclave() == null ? "" : p.getMachineAutoclave().getNom())
                .setHeader("Autoclave")
                .setSortable(true);

        processusGrid.addColumn(ProcessusSterilisation::getDateCreation)
                .setHeader("Date création")
                .setSortable(true);

        processusGrid.addColumn(ProcessusSterilisation::getDateFin)
                .setHeader("Date fin")
                .setSortable(true);

        processusGrid.addComponentColumn(processus -> {
            if (processus.getStatut() == StatutProcessusSterilisation.TERMINE) {
                return new Span("Terminé");
            }

            if (processus.getStatut() == StatutProcessusSterilisation.ECHEC) {
                return new Span("Échec");
            }

            Button nextBtn = new Button("Passer à : " + getNextStatutLabel(processus.getStatut()));
            nextBtn.addClickListener(event -> executeAction(
                    () -> processusService.avancerProcessus(processus.getId())
            ));

            Button echecBtn = new Button("Échec", event -> openEchecDialog(processus));
            echecBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

            HorizontalLayout actions = new HorizontalLayout(nextBtn, echecBtn);
            actions.setWrap(false);

            return actions;
        }).setHeader("Actions").setWidth("360px").setFlexGrow(0);

        processusGrid.setEmptyStateText("Aucun processus de stérilisation");
        processusGrid.setSizeFull();

        setSizeFull();
        getStyle().set("overflow", "auto");

        add(toolbar, processusGrid);
        refreshGrid();
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Lancer un processus de stérilisation");

        ComboBox<DemandeSterilisation> demande = new ComboBox<>("Demande acceptée");
        demande.setItems(
                demandeService.findAll().stream()
                        .filter(d -> d.getStatut() == StatutDemandeSterilisation.ACCEPTEE)
                        .toList()
        );
        demande.setItemLabelGenerator(d ->
                d.getCodeDemande() + " - " + d.getBoiteChirurgicale().getCodeBoite()
        );

        ComboBox<Machine> machineLavage = new ComboBox<>("Machine de lavage");
        machineLavage.setItems(machineService.findMachinesDisponiblesParType(TypeMachine.LAVAGE));
        machineLavage.setItemLabelGenerator(Machine::getNom);

        ComboBox<Machine> machineAutoclave = new ComboBox<>("Machine autoclave");
        machineAutoclave.setItems(machineService.findMachinesDisponiblesParType(TypeMachine.STERILISATION));
        machineAutoclave.setItemLabelGenerator(Machine::getNom);

        TextArea commentaire = new TextArea("Commentaire");
        commentaire.setWidthFull();

        Button saveBtn = new Button("Lancer", event -> {
            try {
                processusService.creerProcessus(
                        demande.getValue() == null ? null : demande.getValue().getId(),
                        machineLavage.getValue() == null ? null : machineLavage.getValue().getId(),
                        machineAutoclave.getValue() == null ? null : machineAutoclave.getValue().getId(),
                        commentaire.getValue()
                );

                Notification.show("Processus lancé", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

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
                demande,
                machineLavage,
                machineAutoclave,
                commentaire,
                new HorizontalLayout(saveBtn, cancelBtn)
        );

        layout.setWidth("650px");

        dialog.add(layout);
        dialog.open();
    }

    private void openEchecDialog(ProcessusSterilisation processus) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Déclarer un échec");

        TextArea commentaire = new TextArea("Motif de l’échec");
        commentaire.setWidthFull();

        Button confirmBtn = new Button("Confirmer échec", event -> {
            try {
                processusService.mettreEnEchec(processus.getId(), commentaire.getValue());

                Notification.show("Processus marqué en échec", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                refreshGrid();
                dialog.close();

            } catch (IllegalArgumentException e) {
                Notification.show(e.getMessage(), 4000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Annuler", event -> dialog.close());

        VerticalLayout layout = new VerticalLayout(
                commentaire,
                new HorizontalLayout(confirmBtn, cancelBtn)
        );

        layout.setWidth("500px");

        dialog.add(layout);
        dialog.open();
    }

    private void executeAction(Runnable action) {
        try {
            action.run();
            refreshGrid();

            Notification.show("Processus mis à jour", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (IllegalArgumentException e) {
            Notification.show(e.getMessage(), 4000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void refreshGrid() {
        String search = searchField.getValue() == null
                ? ""
                : searchField.getValue().trim().toLowerCase();

        processusGrid.setItems(
                processusService.findAll().stream()
                        .filter(p -> {
                            String demande = p.getDemandeSterilisation().getCodeDemande().toLowerCase();
                            String boite = p.getDemandeSterilisation().getBoiteChirurgicale().getCodeBoite().toLowerCase();
                            String statut = p.getStatut() == null ? "" : p.getStatut().name().toLowerCase();
                            String lavage = p.getMachineLavage() == null ? "" : p.getMachineLavage().getNom().toLowerCase();
                            String autoclave = p.getMachineAutoclave() == null ? "" : p.getMachineAutoclave().getNom().toLowerCase();

                            return search.isBlank()
                                    || demande.contains(search)
                                    || boite.contains(search)
                                    || statut.contains(search)
                                    || lavage.contains(search)
                                    || autoclave.contains(search);
                        })
                        .toList()
        );
    }

    private String getNextStatutLabel(StatutProcessusSterilisation statut) {
        return switch (statut) {
            case EN_ATTENTE -> "lavage";
            case LAVAGE -> "conditionnement";
            case CONDITIONNEMENT -> "autoclave";
            case AUTOCLAVE -> "validation";
            case VALIDATION -> "terminé";
            default -> "aucune étape";
        };
    }
}