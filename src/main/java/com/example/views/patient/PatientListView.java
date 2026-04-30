package com.example.views.patient;

import com.example.base.ui.ViewTitle;
import com.example.entity.Patient;
import com.example.service.PatientService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
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

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route(value = "patients")
@PageTitle("Gestion des patients")
@Menu(order = 1, icon = "icons/user.svg", title = "Patients")
public class PatientListView extends VerticalLayout {

    private final PatientService patientService;

    final TextField cnPatient;
    final TextField nomPatient;
    final TextField prenomPatient;
    final DatePicker dateNaissance;
    final Button createBtn;
    final TextField searchField;
    final Grid<Patient> patientGrid;

    private Patient selectedPatient = null;

    public PatientListView(PatientService patientService) {
        this.patientService = patientService;

        cnPatient = new TextField();
        cnPatient.setPlaceholder("Numéro carte national");

        nomPatient = new TextField();
        nomPatient.setPlaceholder("Nom");

        prenomPatient = new TextField();
        prenomPatient.setPlaceholder("Prénom");

        dateNaissance = new DatePicker();
        dateNaissance.setPlaceholder("Date de naissance");

        createBtn = new Button("Ajouter", _ -> saveOrUpdatePatient());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par CN, nom ou prénom");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("18em");

        var toolbar = new VerticalLayout();
        var gestionPatient = new HorizontalLayout();
        gestionPatient.add(new ViewTitle("Gestion des patients"), searchField, cnPatient, nomPatient, prenomPatient, dateNaissance, createBtn);
        gestionPatient.setFlexGrow(1, searchField, cnPatient, nomPatient, prenomPatient, dateNaissance);
        gestionPatient.setWrap(true);
        gestionPatient.setWidthFull();
        var searchLine = new HorizontalLayout();
        searchLine.add(searchField);
        searchLine.setFlexGrow(1, searchField);
        searchLine.setWrap(true);
        searchLine.setWidthFull();
        toolbar.add(gestionPatient, searchLine);

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
            Button deleteBtn = new Button("Supprimer");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

            deleteBtn.addClickListener(_ -> {
                patientService.deletePatient(patient);
                refreshGrid();

                Notification.show("Patient supprimé", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });

            return deleteBtn;
        }).setHeader("Actions");

        patientGrid.asSingleSelect().addValueChangeListener(event -> {
            selectedPatient = event.getValue();

            if (selectedPatient != null) {
                cnPatient.setValue(selectedPatient.getCnPatient());
                nomPatient.setValue(selectedPatient.getNomPatient());
                prenomPatient.setValue(selectedPatient.getPrenomPatient());
                dateNaissance.setValue(selectedPatient.getDateNaissance());
                createBtn.setText("Modifier");
            }
        });

        patientGrid.setEmptyStateText("Aucun patient enregistré");
        patientGrid.setSizeFull();

        setSizeFull();
        add(toolbar, patientGrid);
    }

    private void saveOrUpdatePatient() {
        String cn = cnPatient.getValue().trim().toUpperCase().replace("-", "");
        String nom = nomPatient.getValue().trim();
        String prenom = prenomPatient.getValue().trim();

        if (cn.isBlank()) {
            cnPatient.setInvalid(true);
            cnPatient.setErrorMessage("Le CN est obligatoire");
            return;
        }

        if (!cn.matches("^[A-Z]{1,2}[0-9]{5,7}$")) {
            cnPatient.setInvalid(true);
            cnPatient.setErrorMessage("Format CN marocain invalide, exemple : BK123456");
            return;
        }

        if(patientService.getPatientByIdCN(cn) != null && selectedPatient == null) {
            cnPatient.setInvalid(true);
            cnPatient.setErrorMessage("Le patient est déjà dans la base de données");
            return;
        }

        cnPatient.setInvalid(false);

        if (nom.isBlank()) {
            nomPatient.setInvalid(true);
            nomPatient.setErrorMessage("Le nom est obligatoire");
            return;
        }

        nomPatient.setInvalid(false);

        if (prenom.isBlank()) {
            prenomPatient.setInvalid(true);
            prenomPatient.setErrorMessage("Le prénom est obligatoire");
            return;
        }

        prenomPatient.setInvalid(false);

        if (dateNaissance.getValue() == null) {
            dateNaissance.setInvalid(true);
            dateNaissance.setErrorMessage("La date de naissance est obligatoire");
            return;
        }

        dateNaissance.setInvalid(false);

        if (selectedPatient == null) {
            patientService.createPatient(
                    cn,
                    nom,
                    prenom,
                    dateNaissance.getValue()
            );

            Notification.show("Patient ajouté", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } else {
            patientService.updatePatient(
                    selectedPatient,
                    cn,
                    nom,
                    prenom,
                    dateNaissance.getValue()
            );

            Notification.show("Patient modifié", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }

        refreshGrid();
        clearForm();
    }

    private void clearForm() {
        cnPatient.clear();
        nomPatient.clear();
        prenomPatient.clear();
        dateNaissance.clear();
        selectedPatient = null;
        createBtn.setText("Ajouter");
        patientGrid.asSingleSelect().clear();
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