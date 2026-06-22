package com.example.views.sterilisation;

import com.example.base.ui.ViewTitle;
import com.example.entity.*;
import com.example.entity.enums.*;
import com.example.service.*;
import com.example.views.components.GridDialog;
import com.example.views.components.IncidentProcessusForm;
import com.example.views.components.ProcessusSterilisationForm;
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

@Route("processus-sterilisation")
@PageTitle("Processus de stérilisation")
@Menu(order = 10, icon = "icons/refresh.svg", title = "Stérilisation/Processus")
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
        createBtn.addClassName("primary-action");

        var titleLine = new HorizontalLayout(new ViewTitle("Processus de stérilisation"));
        titleLine.setWidthFull();

        var searchLine = new HorizontalLayout(searchField, createBtn);
        searchLine.setWidthFull();
        searchLine.setWrap(true);
        searchLine.setFlexGrow(1, searchField);

        VerticalLayout toolbar = new VerticalLayout(titleLine, searchLine);
        toolbar.addClassName("page-toolbar");
        toolbar.setWidthFull();

        processusGrid = new Grid<>();

        processusGrid.addColumn(p -> p.getDemandeSterilisation().getCodeDemande())
                .setHeader("Demande")
                .setSortable(true);

        processusGrid.addColumn(p -> p.getDemandeSterilisation().getBoiteChirurgicale().getCodeBoite())
                .setHeader("Boîte")
                .setSortable(true);

        processusGrid.addComponentColumn(processus -> {
            Span badge = new Span(processus.getStatut() == null ? "" : processus.getStatut().name());

            if (processus.getStatut() == null) {
                badge.addClassName("status-neutral");
            } else {
                switch (processus.getStatut()) {
                    case EN_ATTENTE -> badge.addClassName("status-neutral");
                    case LAVAGE, CONDITIONNEMENT, AUTOCLAVE, VALIDATION -> badge.addClassName("status-info");
                    case TERMINE -> badge.addClassName("status-success");
                    case ECHEC -> badge.addClassName("status-danger");
                }
            }

            return badge;
        }).setHeader("Étape actuelle");

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
        processusGrid.addClassName("professional-grid");

        setSizeFull();
        addClassName("page-container");
        getStyle().set("overflow", "auto");

        add(toolbar, processusGrid);
        refreshGrid();
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Lancer un processus de stérilisation");
        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");

        ProcessusSterilisationForm form = new ProcessusSterilisationForm(
                demandeService.findAll().stream()
                        .filter(d -> d.getStatut() == StatutDemandeSterilisation.ACCEPTEE)
                        .toList(),
                machineService.findMachinesDisponiblesParType(TypeMachine.LAVAGE),
                machineService.findMachinesDisponiblesParType(TypeMachine.STERILISATION),
                data -> {
                    try {
                        processusService.creerProcessus(
                                data.demandeId(),
                                data.machineLavageId(),
                                data.machineAutoclaveId(),
                                data.commentaire()
                        );

                        Notification.show("Processus lancé", 3000, Notification.Position.BOTTOM_END)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

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

    private void openEchecDialog(ProcessusSterilisation processus) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Déclarer un incident / échec");
        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");

        IncidentProcessusForm form = new IncidentProcessusForm(
                java.util.stream.Stream.of(
                        processus.getMachineLavage(),
                        processus.getMachineAutoclave()
                ).filter(java.util.Objects::nonNull).toList(),
                data -> {
                    try {
                        incidentService.createIncident(
                                processus.getId(),
                                data.machineId(),
                                data.typeIncident(),
                                data.gravite(),
                                data.description()
                        );

                        processusService.mettreEnEchec(
                                processus.getId(),
                                data.description()
                        );

                        Notification.show("Incident enregistré et processus marqué en échec", 3000, Notification.Position.BOTTOM_END)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

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
                        .filter(p -> matchesSearch(p, search))
                        .toList()
        );
    }

    private void openHistoriqueDialog(ProcessusSterilisation processus) {
        Grid<HistoriqueProcessus> historiqueGrid = new Grid<>();

        historiqueGrid.addColumn(HistoriqueProcessus::getDateAction)
                .setHeader("Date");

        historiqueGrid.addColumn(HistoriqueProcessus::getEtape)
                .setHeader("Étape");

        historiqueGrid.addColumn(HistoriqueProcessus::getCommentaire)
                .setHeader("Commentaire");

        historiqueGrid.setItems(historiqueService.findByProcessus(processus.getId()));
        historiqueGrid.setEmptyStateText("Aucun historique pour ce processus");

        new GridDialog<>(
                "Historique du processus #" + processus.getId(),
                historiqueGrid
        ).open();
    }

    private void openIncidentsDialog(ProcessusSterilisation processus) {
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

        incidentGrid.setItems(incidentService.findByProcessus(processus.getId()));
        incidentGrid.setEmptyStateText("Aucun incident pour ce processus");
        incidentGrid.setAllRowsVisible(true);

        new GridDialog<>(
                "Incidents du processus #" + processus.getId(),
                incidentGrid
        ).open();
    }

    private boolean matchesSearch(ProcessusSterilisation p, String search) {
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