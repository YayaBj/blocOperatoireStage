package com.example.views.sterilisation;

import com.example.entity.*;
import com.example.entity.enums.*;
import com.example.service.*;
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
    private final HistoriqueProcessusService historiqueService;
    private final IncidentSterilisationService incidentService;

    private final TextField searchField;
    private final Grid<ProcessusSterilisation> processusGrid;

    public ProcessusSterilisationListView(ProcessusSterilisationService processusService,
                                          DemandeSterilisationService demandeService,
                                          MachineService machineService,
                                          HistoriqueProcessusService historiqueService,
                                          IncidentSterilisationService incidentService) {
        this.processusService = processusService;
        this.demandeService = demandeService;
        this.machineService = machineService;
        this.historiqueService = historiqueService;
        this.incidentService = incidentService;

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
                Button historiqueBtn = new Button("Historique");
                historiqueBtn.addClickListener(_ -> openHistoriqueDialog(processus));

                return new HorizontalLayout(
                        new Span("Terminé"),
                        historiqueBtn
                );
            }

            if (processus.getStatut() == StatutProcessusSterilisation.ECHEC) {
                Button historiqueBtn = new Button("Historique");
                historiqueBtn.addClickListener(event -> openHistoriqueDialog(processus));

                Button incidentsBtn = new Button("Voir incident");
                incidentsBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
                incidentsBtn.addClickListener(event -> openIncidentsDialog(processus));

                HorizontalLayout actions = new HorizontalLayout(
                        new Span("Échec"),
                        historiqueBtn,
                        incidentsBtn
                );
                actions.setWrap(false);

                return actions;
            }

            Button nextBtn = new Button("Passer à : " + getNextStatutLabel(processus.getStatut()));
            nextBtn.addClickListener(_ -> executeAction(
                    () -> processusService.avancerProcessus(processus.getId())
            ));

            Button echecBtn = new Button("Échec", _ -> openEchecDialog(processus));
            echecBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

            Button historiqueBtn = new Button("Historique");
            historiqueBtn.addClickListener(_ ->
                    openHistoriqueDialog(processus)
            );

            Button incidentsBtn = new Button("Incidents");
            incidentsBtn.addClickListener(_ -> openIncidentsDialog(processus));

            HorizontalLayout actions = new HorizontalLayout(nextBtn, echecBtn, historiqueBtn, incidentsBtn);
            actions.setWrap(false);

            return actions;
        }).setHeader("Actions").setWidth("520px").setFlexGrow(0);

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
        dialog.setHeaderTitle("Déclarer un incident / échec");

        ComboBox<TypeIncidentSterilisation> typeIncident = new ComboBox<>("Type incident");
        typeIncident.setItems(TypeIncidentSterilisation.values());

        ComboBox<GraviteIncident> gravite = new ComboBox<>("Gravité");
        gravite.setItems(GraviteIncident.values());

        ComboBox<Machine> machine = new ComboBox<>("Machine concernée");
        machine.setItems(
                processus.getMachineLavage(),
                processus.getMachineAutoclave()
        );
        machine.setItemLabelGenerator(m -> m == null ? "" : m.getNom());
        machine.setClearButtonVisible(true);

        TextArea description = new TextArea("Description");
        description.setWidthFull();

        Button confirmBtn = new Button("Déclarer l’échec", event -> {
            try {
                Long machineId = machine.getValue() == null
                        ? null
                        : machine.getValue().getId();

                incidentService.createIncident(
                        processus.getId(),
                        machineId,
                        typeIncident.getValue(),
                        gravite.getValue(),
                        description.getValue()
                );

                processusService.mettreEnEchec(
                        processus.getId(),
                        description.getValue()
                );

                Notification.show("Incident enregistré et processus marqué en échec", 3000, Notification.Position.BOTTOM_END)
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
                typeIncident,
                gravite,
                machine,
                description,
                new HorizontalLayout(confirmBtn, cancelBtn)
        );

        layout.setWidth("550px");

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

    private void openHistoriqueDialog(ProcessusSterilisation processus) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(
                "Historique du processus #" + processus.getId()
        );

        Grid<HistoriqueProcessus> historiqueGrid = new Grid<>();

        historiqueGrid.addColumn(HistoriqueProcessus::getDateAction)
                .setHeader("Date");

        historiqueGrid.addColumn(HistoriqueProcessus::getEtape)
                .setHeader("Étape");

        historiqueGrid.addColumn(HistoriqueProcessus::getCommentaire)
                .setHeader("Commentaire");

        historiqueGrid.setItems(
                historiqueService.findByProcessus(processus.getId())
        );

        historiqueGrid.setSizeFull();

        dialog.add(historiqueGrid);

        dialog.setWidth("900px");
        dialog.setHeight("600px");

        dialog.open();
    }

    private void openIncidentsDialog(ProcessusSterilisation processus) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Incidents du processus #" + processus.getId());

        Grid<IncidentSterilisation> incidentGrid = new Grid<>();

        incidentGrid.addColumn(IncidentSterilisation::getDateIncident)
                .setHeader("Date");

        incidentGrid.addColumn(IncidentSterilisation::getTypeIncident)
                .setHeader("Type");

        incidentGrid.addColumn(IncidentSterilisation::getGravite)
                .setHeader("Gravité");

        incidentGrid.addColumn(i -> i.getMachine() == null ? "" : i.getMachine().getNom())
                .setHeader("Machine");

        incidentGrid.addComponentColumn(incident -> {
            Span description = new Span(incident.getDescription());

            description.getStyle()
                    .set("white-space", "normal")
                    .set("word-break", "break-word")
                    .set("line-height", "1.4");

            return description;
        }).setHeader("Description").setWidth("350px").setFlexGrow(1);

        incidentGrid.setAllRowsVisible(true);

        incidentGrid.setItems(incidentService.findByProcessus(processus.getId()));
        incidentGrid.setSizeFull();

        Button closeBtn = new Button("Fermer", event -> dialog.close());

        VerticalLayout layout = new VerticalLayout(incidentGrid, closeBtn);
        layout.setSizeFull();

        dialog.setWidth("900px");
        dialog.setHeight("600px");
        dialog.add(layout);
        dialog.open();
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