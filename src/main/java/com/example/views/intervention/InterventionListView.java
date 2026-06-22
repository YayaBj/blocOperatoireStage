package com.example.views.intervention;

import com.example.base.ui.ViewTitle;
import com.example.entity.*;
import com.example.entity.enums.RoleIntervention;
import com.example.entity.enums.StatutSalle;
import com.example.service.*;
import com.example.views.components.InterventionForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;

@Route(value = "interventions")
@PageTitle("Gestion des interventions")
@Menu(order = 6, title = "Bloc opératoire/Interventions")
public class InterventionListView extends VerticalLayout {

    private final TextField searchField;
    private final InterventionService interventionService;
    private final Grid<Intervention> interventionGrid;

    private final PatientService patientService;
    private final SalleService salleService;
    private final PersonnelService personnelService;

    public InterventionListView(
            InterventionService interventionService,
            PatientService patientService,
            SalleService salleService,
            PersonnelService personnelService
    ) {
        this.interventionService = interventionService;
        this.patientService = patientService;
        this.salleService = salleService;
        this.personnelService = personnelService;

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par type, priorité, patient, salle ou statut");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("30em");
        searchField.addValueChangeListener(_ -> refreshGrid());

        Button createBtn = new Button("Nouvelle intervention", _ -> openCreateDialog());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.addClassName("primary-action");

        var titleLine = new HorizontalLayout(new ViewTitle("Gestion des interventions"));
        titleLine.setWidthFull();

        var searchLine = new HorizontalLayout(searchField, createBtn);
        searchLine.setWidthFull();
        searchLine.setWrap(true);
        searchLine.setFlexGrow(1, searchField);

        var toolbar = new VerticalLayout(titleLine, searchLine);
        toolbar.addClassName("page-toolbar");
        toolbar.setWidthFull();

        var dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(getLocale());

        interventionGrid = new Grid<>();
        refreshGrid();

        interventionGrid.addColumn(Intervention::getTypeIntervention)
                .setHeader("Type")
                .setSortable(true);

        interventionGrid.addColumn(Intervention::getPriorite)
                .setHeader("Priorité")
                .setSortable(true);

        interventionGrid.addColumn(intervention -> dateFormatter.format(intervention.getDateHeureDebut()))
                .setHeader("Date début")
                .setSortable(true);

        interventionGrid.addColumn(Intervention::getDureePrevue)
                .setHeader("Durée prévue")
                .setSortable(true);

        interventionGrid.addColumn(intervention ->
                intervention.getPatient().getNomPatient() + " " + intervention.getPatient().getPrenomPatient()
        ).setHeader("Patient");

        interventionGrid.addColumn(intervention ->
                intervention.getSalle().getNumeroSalle()
        ).setHeader("Salle");

        interventionGrid.addColumn(Intervention::getStatutIntervention)
                .setHeader("Statut")
                .setSortable(true);

        interventionGrid.setEmptyStateText("Aucune intervention enregistrée");
        interventionGrid.setSizeFull();
        interventionGrid.addClassName("professional-grid");

        toolbar.addClassName("page-toolbar");

        setSizeFull();
        addClassName("page-container");
        add(toolbar, interventionGrid);
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Créer une intervention");

        InterventionForm form = new InterventionForm(
                patientService.findAll(),
                salleService.findByStatut(StatutSalle.DISPONIBLE),
                personnelService.findAll(),
                interventionService.findBoitesDisponibles(),
                null,
                null,
                (data, roles) -> createIntervention(dialog, data, roles)
        );

        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");

        dialog.add(form);
        dialog.open();
    }

    private void createIntervention(Dialog dialog,
                                    InterventionForm.InterventionFormData data,
                                    Map<Long, RoleIntervention> personnelsAvecRoles) {
        try {
            interventionService.createIntervention(
                    data.typeIntervention(),
                    data.priorite(),
                    data.dateHeureDebut(),
                    data.dureePrevue() == null ? 0 : data.dureePrevue(),
                    data.patientId(),
                    data.salleId(),
                    personnelsAvecRoles,
                    data.boiteIds()
            );

            Notification.show("Intervention créée", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            dialog.close();
            refreshGrid();

        } catch (IllegalArgumentException e) {
            Notification.show(e.getMessage(), 4000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void refreshGrid() {
        String search = searchField.getValue() == null
                ? ""
                : searchField.getValue().trim().toLowerCase();

        interventionGrid.setItems(
                interventionService.findAll().stream()
                        .filter(intervention -> {
                            String type = intervention.getTypeIntervention() == null ? "" : intervention.getTypeIntervention().toLowerCase();
                            String priorite = intervention.getPriorite() == null ? "" : intervention.getPriorite().name().toLowerCase();
                            String patient = intervention.getPatient() == null ? "" :
                                    (intervention.getPatient().getNomPatient() + " " + intervention.getPatient().getPrenomPatient()).toLowerCase();
                            String salle = intervention.getSalle() == null ? "" : intervention.getSalle().getNumeroSalle().toLowerCase();
                            String statut = intervention.getStatutIntervention() == null ? "" : intervention.getStatutIntervention().name().toLowerCase();

                            return search.isBlank()
                                    || type.contains(search)
                                    || priorite.contains(search)
                                    || patient.contains(search)
                                    || salle.contains(search)
                                    || statut.contains(search);
                        })
                        .toList()
        );
    }
}