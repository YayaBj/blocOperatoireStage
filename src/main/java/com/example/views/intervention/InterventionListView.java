package com.example.views.intervention;

import com.example.base.ui.ViewTitle;
import com.example.entity.*;
import com.example.entity.enums.PrioriteIntervention;
import com.example.entity.enums.RoleIntervention;
import com.example.entity.enums.StatutSalle;
import com.example.service.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;

@Route(value = "interventions")
@PageTitle("Gestion des interventions")
@Menu(order = 5, icon = "", title = "Interventions")
public class InterventionListView extends VerticalLayout {

    private final InterventionService interventionService;

    final TextField typeIntervention;
    final ComboBox<PrioriteIntervention> priorite;
    final DateTimePicker dateHeureDebut;
    final IntegerField dureePrevue;
    final ComboBox<Patient> patient;
    final ComboBox<Salle> salle;
    final MultiSelectComboBox<Personnel> personnels;
    final MultiSelectComboBox<UniteMateriel> unitesMateriel;
    final Button createBtn;
    final Grid<Intervention> interventionGrid;

    public InterventionListView(
            InterventionService interventionService,
            PatientService patientService,
            SalleService salleService,
            PersonnelService personnelService
    ) {
        this.interventionService = interventionService;

        typeIntervention = new TextField();
        typeIntervention.setPlaceholder("Type intervention");

        priorite = new ComboBox<>();
        priorite.setPlaceholder("Priorité");
        priorite.setItems(PrioriteIntervention.values());

        dateHeureDebut = new DateTimePicker();
        dateHeureDebut.setDatePlaceholder("Date et heure");

        dureePrevue = new IntegerField();
        dureePrevue.setPlaceholder("Durée prévue (min)");
        dureePrevue.setMin(1);

        patient = new ComboBox<>();
        patient.setPlaceholder("Patient");
        patient.setItems(patientService.findAll());
        patient.setItemLabelGenerator(p -> p.getCnPatient() + " - " + p.getNomPatient() + " " + p.getPrenomPatient());

        salle = new ComboBox<>();
        salle.setPlaceholder("Salle");
        salle.setItems(salleService.findByetat(StatutSalle.DISPONIBLE));
        salle.setItemLabelGenerator(s -> s.getNumeroSalle() + " - " + s.getTypeSalle());

        personnels = new MultiSelectComboBox<>();
        personnels.setPlaceholder("Personnel");
        personnels.setItems(personnelService.findAll());
        personnels.setItemLabelGenerator(p -> p.getMatricule() + " - " + p.getNomPersonnel() + " " + p.getPrenomPersonnel());

        unitesMateriel = new MultiSelectComboBox<>();
        unitesMateriel.setPlaceholder("Unités matériel stériles");
        unitesMateriel.setItems(interventionService.findUnitesSteriles());
        unitesMateriel.setItemLabelGenerator(u -> u.getCodeInventaire() + " - " + u.getMateriel().getNomMateriel());

        createBtn = new Button("Créer intervention", _ -> openRoleDialog());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var toolbar = getVerticalLayout();

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

        setSizeFull();
        add(toolbar, interventionGrid);
    }

    @NotNull
    private VerticalLayout getVerticalLayout() {
        var toolbar = new VerticalLayout();

        var line1 = new HorizontalLayout(
                new ViewTitle("Gestion des interventions"),
                typeIntervention,
                priorite,
                dateHeureDebut,
                dureePrevue
        );
        line1.setWidthFull();
        line1.setWrap(true);

        var line2 = new HorizontalLayout(
                patient,
                salle,
                personnels,
                unitesMateriel,
                createBtn
        );
        line2.setWidthFull();
        line2.setWrap(true);

        toolbar.add(line1, line2);
        return toolbar;
    }

    private void createIntervention(Map<Long, RoleIntervention> personnelsAvecRoles) {
        try {
            interventionService.createIntervention(
                    typeIntervention.getValue().trim(),
                    priorite.getValue(),
                    dateHeureDebut.getValue(),
                    dureePrevue.getValue(),
                    patient.getValue().getId(),
                    salle.getValue().getId(),
                    personnelsAvecRoles,
                    unitesMateriel.getValue().stream()
                            .map(UniteMateriel::getId)
                            .toList()
            );

            Notification.show("Intervention créée", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            clearForm();
            refreshGrid();
            refreshUnitesDisponibles();

        } catch (IllegalArgumentException e) {
            Notification.show(e.getMessage(), 4000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void refreshGrid() {
        interventionGrid.setItems(interventionService.findAll());
    }

    private void refreshUnitesDisponibles() {
        unitesMateriel.setItems(interventionService.findUnitesSteriles());
    }

    private void clearForm() {
        typeIntervention.clear();
        priorite.clear();
        dateHeureDebut.clear();
        dureePrevue.clear();
        patient.clear();
        salle.clear();
        personnels.clear();
        unitesMateriel.clear();
    }

    private void openRoleDialog() {
        if (!validateFormBeforeRoles()) {
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Rôles du personnel");

        VerticalLayout layout = new VerticalLayout();

        Map<Personnel, ComboBox<RoleIntervention>> roleMap = new HashMap<>();

        for (Personnel personnelSelectionne : personnels.getValue()) {
            ComboBox<RoleIntervention> roleComboBox = new ComboBox<>();
            roleComboBox.setLabel(
                    personnelSelectionne.getNomPersonnel() + " " + personnelSelectionne.getPrenomPersonnel()
            );
            roleComboBox.setItems(RoleIntervention.values());
            roleComboBox.setPlaceholder("Choisir un rôle");

            roleMap.put(personnelSelectionne, roleComboBox);
            layout.add(roleComboBox);
        }

        Button confirmBtn = new Button("Confirmer", event -> {
            Map<Long, RoleIntervention> personnelsAvecRoles = new HashMap<>();

            for (Map.Entry<Personnel, ComboBox<RoleIntervention>> entry : roleMap.entrySet()) {
                Personnel personnelSelectionne = entry.getKey();
                ComboBox<RoleIntervention> roleComboBox = entry.getValue();

                RoleIntervention role = roleComboBox.getValue();

                if (role == null) {
                    roleComboBox.setInvalid(true);
                    roleComboBox.setErrorMessage("Le rôle est obligatoire");
                    return;
                }

                personnelsAvecRoles.put(personnelSelectionne.getId(), role);
            }

            createIntervention(personnelsAvecRoles);
            dialog.close();
        });

        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Annuler", event -> dialog.close());

        HorizontalLayout actions = new HorizontalLayout(confirmBtn, cancelBtn);

        layout.add(actions);
        layout.setWidth("500px");

        dialog.add(layout);
        dialog.open();
    }

    private boolean validateFormBeforeRoles() {
        if (typeIntervention.getValue().trim().isBlank()) {
            typeIntervention.setInvalid(true);
            typeIntervention.setErrorMessage("Le type d’intervention est obligatoire");
            return false;
        }
        typeIntervention.setInvalid(false);

        if (priorite.getValue() == null) {
            priorite.setInvalid(true);
            priorite.setErrorMessage("La priorité est obligatoire");
            return false;
        }
        priorite.setInvalid(false);

        if (dateHeureDebut.getValue() == null) {
            dateHeureDebut.setInvalid(true);
            dateHeureDebut.setErrorMessage("La date et l’heure sont obligatoires");
            return false;
        }
        dateHeureDebut.setInvalid(false);

        if (dureePrevue.getValue() == null || dureePrevue.getValue() <= 0) {
            dureePrevue.setInvalid(true);
            dureePrevue.setErrorMessage("La durée doit être supérieure à 0");
            return false;
        }
        dureePrevue.setInvalid(false);

        if (patient.getValue() == null) {
            patient.setInvalid(true);
            patient.setErrorMessage("Le patient est obligatoire");
            return false;
        }
        patient.setInvalid(false);

        if (salle.getValue() == null) {
            salle.setInvalid(true);
            salle.setErrorMessage("La salle est obligatoire");
            return false;
        }
        salle.setInvalid(false);

        if (personnels.getValue().isEmpty()) {
            personnels.setInvalid(true);
            personnels.setErrorMessage("Au moins un membre du personnel est obligatoire");
            return false;
        }
        personnels.setInvalid(false);

        if (unitesMateriel.getValue().isEmpty()) {
            unitesMateriel.setInvalid(true);
            unitesMateriel.setErrorMessage("Au moins une unité de matériel est obligatoire");
            return false;
        }
        unitesMateriel.setInvalid(false);

        return true;
    }
}