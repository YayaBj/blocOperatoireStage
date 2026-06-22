package com.example.views.patient;

import com.example.base.ui.ViewTitle;
import com.example.entity.Patient;
import com.example.service.PatientService;
import com.example.views.components.ConfirmDeleteDialog;
import com.example.views.components.PatientForm;
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
import java.util.Optional;

@Route(value = "patients")
@PageTitle("Gestion des patients")
@Menu(order = 2, icon = "icons/user.svg", title = "Données de base/Patients")
public class PatientListView extends VerticalLayout {

    private final PatientService patientService;

    final TextField searchField;
    final Grid<Patient> patientGrid;

    public PatientListView(PatientService patientService) {
        this.patientService = patientService;

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par CN, nom ou prénom");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("30em");
        searchField.addValueChangeListener(_ -> refreshGrid());

        Button createBtn = new Button("Ajouter un patient", _ -> openCreateDialog());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.addClassName("primary-action");

        var titleLine = new HorizontalLayout(new ViewTitle("Gestion des patients"));
        titleLine.setWidthFull();

        var searchLine = new HorizontalLayout(searchField, createBtn);
        searchLine.setWidthFull();
        searchLine.setWrap(true);
        searchLine.setFlexGrow(1, searchField);

        var toolbar = new VerticalLayout(titleLine, searchLine);
        toolbar.addClassName("page-toolbar");
        toolbar.setWidthFull();

        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());

        patientGrid = new Grid<>();
        refreshGrid();
        searchField.addValueChangeListener(event -> refreshGrid());

        patientGrid.addColumn(Patient::getCnPatient).setHeader("CN").setSortable(true);
        patientGrid.addColumn(Patient::getNomPatient).setHeader("Nom").setSortable(true);
        patientGrid.addColumn(Patient::getPrenomPatient).setHeader("Prénom").setSortable(true);
        patientGrid.addColumn(patient -> Optional.ofNullable(patient.getDateNaissance())
                .map(dateFormatter::format)
                .orElse("")).setHeader("Date de naissance")
                .setSortable(true);

        patientGrid.addComponentColumn(patient -> {
            Button modifierBtn = new Button("Modifier");
            modifierBtn.addClickListener(_ -> openEditDialog(patient));

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

            deleteBtn.addClickListener(_ -> {
                ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(
                        "Voulez-vous vraiment supprimer le patient : "
                                + patient.getNomPatient() + " " + patient.getPrenomPatient() + " ?",
                        () -> {
                            patientService.deletePatient(patient);
                            refreshGrid();

                            Notification.show("Patient supprimé", 3000, Notification.Position.BOTTOM_END)
                                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        }
                );

                dialog.open();
            });

            return new HorizontalLayout(modifierBtn, deleteBtn);
        }).setHeader("Actions");

        patientGrid.setEmptyStateText("Aucun patient enregistré");
        patientGrid.setSizeFull();
        patientGrid.addClassName("professional-grid");

        setSizeFull();
        addClassName("page-container");
        add(toolbar, patientGrid);
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Ajouter un patient");
        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");

        PatientForm form = new PatientForm(
                null,
                "Ajouter le patient",
                data -> {
                    try {
                        patientService.createPatient(
                                data.cnPatient(),
                                data.nomPatient(),
                                data.prenomPatient(),
                                data.dateNaissance()
                        );

                        Notification.show("Patient ajouté", 3000, Notification.Position.BOTTOM_END)
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

    private void openEditDialog(Patient patient) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Modifier le patient");
        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");

        PatientForm form = new PatientForm(
                patient,
                "Modifier le patient",
                data -> {
                    try {
                        patientService.updatePatient(
                                patient,
                                data.cnPatient(),
                                data.nomPatient(),
                                data.prenomPatient(),
                                data.dateNaissance()
                        );

                        Notification.show("Patient modifié", 3000, Notification.Position.BOTTOM_END)
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

    private void refreshGrid() {
        String search = searchField.getValue() == null ? "" : searchField.getValue().trim().toLowerCase();

        patientGrid.setItems(
                patientService.findAll().stream()
                        .filter(patient ->
                                search.isBlank()
                                        || patient.getCnPatient().toLowerCase().contains(search)
                                        || patient.getNomPatient().toLowerCase().contains(search)
                                        || patient.getPrenomPatient().toLowerCase().contains(search)
                        )
                        .toList()
        );
    }
}