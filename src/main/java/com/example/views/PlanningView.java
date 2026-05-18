package com.example.views;

import com.example.entity.*;
import com.example.entity.enums.PrioriteIntervention;
import com.example.entity.enums.RoleIntervention;
import com.example.entity.enums.StatutIntervention;
import com.example.service.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
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
import org.jetbrains.annotations.NotNull;
import org.vaadin.stefan.fullcalendar.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Route("")
@PageTitle("Planning")
@Menu(order = 1, icon = "icons/calendar.svg", title = "Planning")
public class PlanningView extends VerticalLayout {

    private final InterventionService interventionService;
    private final PatientService patientService;
    private final SalleService salleService;
    private final PersonnelService personnelService;

    public PlanningView(
            InterventionService interventionService,
            PatientService patientService,
            SalleService salleService,
            PersonnelService personnelService
    ) {
        this.patientService = patientService;
        this.salleService = salleService;
        this.personnelService = personnelService;
        this.interventionService = interventionService;

        FullCalendar calendar = FullCalendarBuilder.create().build();
        calendar.setOption(FullCalendar.Option.LOCALE, Locale.FRANCE);
        calendar.setSizeFull();

        calendar.setOption(FullCalendar.Option.SELECTABLE, true);

        calendar.addTimeslotsSelectedListener(event -> {
            var start = event.getStart();
            var end = event.getEnd();

            openCreateInterventionDialog(calendar, start, end);
        });

        calendar.addEntryClickedListener(event -> {

            Entry entry = event.getEntry();

            Long interventionId = Long.valueOf(
                    entry.getCustomProperty("interventionId").toString()
            );

            openInterventionDetailsDialog(calendar, interventionId);
        });

        getListenerDragAndDrop(interventionService, calendar);

        DatePicker datePicker = new DatePicker("Choisir une date");
        datePicker.setValue(LocalDate.now());

        HorizontalLayout toolbar = getHorizontalLayout(calendar, datePicker);

        loadInterventions(calendar);

        calendar.addAttachListener(_ -> {
            calendar.changeView(CalendarViewImpl.TIME_GRID_WEEK);
            calendar.gotoDate(LocalDate.now());
        });

        setSizeFull();
        add(toolbar, calendar);
        expand(calendar);
    }

    private void getListenerDragAndDrop(InterventionService interventionService, FullCalendar calendar) {
        calendar.addEntryDroppedListener(event -> {

            Entry updatedEntry = event.createCopyBasedOnChanges();

            Long interventionId = Long.valueOf(
                    updatedEntry.getCustomProperty("interventionId").toString()
            );

            for (var method : event.getClass().getMethods()) {
                System.out.println(method.getName());
            }
            try {

                interventionService.deplacerIntervention(
                        interventionId,
                        updatedEntry.getStart(),
                        updatedEntry.getEnd()
                );

                Notification.show(
                        "Intervention déplacée",
                        3000,
                        Notification.Position.BOTTOM_END
                ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                loadInterventions(calendar);

            } catch (IllegalArgumentException e) {

                Notification.show(
                        e.getMessage(),
                        4000,
                        Notification.Position.BOTTOM_END
                ).addThemeVariants(NotificationVariant.LUMO_ERROR);

                loadInterventions(calendar);
            }
        });

        calendar.addEntryResizedListener(event -> {

            Entry updatedEntry = event.createCopyBasedOnChanges();

            Long interventionId = Long.valueOf(
                    updatedEntry.getCustomProperty("interventionId").toString()
            );

            try {

                interventionService.redimensionnerIntervention(
                        interventionId,
                        updatedEntry.getStart(),
                        updatedEntry.getEnd()
                );

                Notification.show(
                        "Durée de l’intervention modifiée",
                        3000,
                        Notification.Position.BOTTOM_END
                ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                loadInterventions(calendar);

            } catch (IllegalArgumentException e) {

                Notification.show(
                        e.getMessage(),
                        4000,
                        Notification.Position.BOTTOM_END
                ).addThemeVariants(NotificationVariant.LUMO_ERROR);

                loadInterventions(calendar);
            }
        });
    }

    @NotNull
    private static HorizontalLayout getHorizontalLayout(FullCalendar calendar, DatePicker datePicker) {
        Button moisBtn = new Button("Mois", _ -> {
            calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH);
            calendar.gotoDate(datePicker.getValue());
        });

        Button semaineBtn = new Button("Semaine", _ -> {
            calendar.changeView(CalendarViewImpl.TIME_GRID_WEEK);
            calendar.gotoDate(datePicker.getValue());
        });

        Button jourBtn = new Button("Jour", _ -> {
            calendar.changeView(CalendarViewImpl.TIME_GRID_DAY);
            calendar.gotoDate(datePicker.getValue());
        });

        HorizontalLayout toolbar = new HorizontalLayout(datePicker, moisBtn, semaineBtn, jourBtn);
        toolbar.setWidthFull();
        return toolbar;
    }

    private void loadInterventions(FullCalendar calendar) {
        var entryProvider = calendar.getEntryProvider().asInMemory();

        entryProvider.removeAllEntries();

        for (Intervention intervention : interventionService.findAll()) {
            Entry entry = new Entry();

            entry.setCustomProperty("interventionId", intervention.getId());

            entry.setTitle(intervention.getTypeIntervention());

            entry.setStart(intervention.getDateHeureDebut());
            entry.setEnd(
                    intervention.getDateHeureDebut()
                            .plusMinutes(intervention.getDureePrevue())
            );

            entry.setDescription(
                    intervention.getPatient().getNomPatient() + " - Salle " +
                            intervention.getSalle().getNumeroSalle()
            );

            entryProvider.addEntries(entry);
        }

        entryProvider.refreshAll();
    }

    private void openCreateInterventionDialog(FullCalendar calendar, LocalDateTime start, LocalDateTime end) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Créer une intervention");

        TextField typeIntervention = new TextField("Type intervention");

        ComboBox<PrioriteIntervention> priorite = new ComboBox<>("Priorité");
        priorite.setItems(PrioriteIntervention.values());

        IntegerField dureePrevue = new IntegerField("Durée prévue (minutes)");
        dureePrevue.setValue((int) Duration.between(start, end).toMinutes());
        dureePrevue.setMin(1);

        ComboBox<Patient> patient = new ComboBox<>("Patient");
        patient.setItems(patientService.findAll());
        patient.setItemLabelGenerator(p ->
                p.getCnPatient() + " - " + p.getNomPatient() + " " + p.getPrenomPatient()
        );

        ComboBox<Salle> salle = new ComboBox<>("Salle");
        salle.setItems(salleService.findAll());
        salle.setItemLabelGenerator(s ->
                s.getNumeroSalle() + " - " + s.getTypeSalle()
        );

        MultiSelectComboBox<Personnel> personnels = new MultiSelectComboBox<>("Personnel");
        personnels.setItems(personnelService.findAll());
        personnels.setItemLabelGenerator(p ->
                p.getMatricule() + " - " + p.getNomPersonnel() + " " + p.getPrenomPersonnel()
        );

        MultiSelectComboBox<UniteMateriel> unitesMateriel = new MultiSelectComboBox<>("Unités matériel");
        unitesMateriel.setItems(interventionService.findUnitesSteriles());
        unitesMateriel.setItemLabelGenerator(u ->
                u.getCodeInventaire() + " - " + u.getMateriel().getNomMateriel()
        );

        Button createBtn = new Button("Créer", event -> {
            if (typeIntervention.getValue().trim().isBlank()
                    || priorite.getValue() == null
                    || dureePrevue.getValue() == null
                    || dureePrevue.getValue() <= 0
                    || patient.getValue() == null
                    || salle.getValue() == null
                    || personnels.getValue().isEmpty()
                    || unitesMateriel.getValue().isEmpty()) {

                Notification.show("Veuillez remplir tous les champs", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            openRoleDialog(
                    dialog,
                    calendar,
                    typeIntervention.getValue().trim(),
                    priorite.getValue(),
                    start,
                    dureePrevue.getValue(),
                    patient.getValue(),
                    salle.getValue(),
                    personnels,
                    unitesMateriel
            );
        });

        Button cancelBtn = new Button("Annuler", event -> dialog.close());

        HorizontalLayout actions = new HorizontalLayout(createBtn, cancelBtn);

        VerticalLayout layout = new VerticalLayout(
                typeIntervention,
                priorite,
                dureePrevue,
                patient,
                salle,
                personnels,
                unitesMateriel,
                actions
        );

        layout.setWidth("600px");

        dialog.add(layout);
        dialog.open();
    }

    private void openRoleDialog(
            Dialog parentDialog,
            FullCalendar calendar,
            String typeIntervention,
            PrioriteIntervention priorite,
            LocalDateTime dateHeureDebut,
            int dureePrevue,
            Patient patient,
            Salle salle,
            MultiSelectComboBox<Personnel> personnels,
            MultiSelectComboBox<UniteMateriel> unitesMateriel
    ) {
        Dialog roleDialog = new Dialog();
        roleDialog.setHeaderTitle("Rôles du personnel");

        VerticalLayout layout = new VerticalLayout();

        Map<Personnel, ComboBox<RoleIntervention>> roleMap = new HashMap<>();

        for (Personnel personnel : personnels.getValue()) {
            ComboBox<RoleIntervention> roleComboBox = new ComboBox<>();
            roleComboBox.setLabel(personnel.getNomPersonnel() + " " + personnel.getPrenomPersonnel());
            roleComboBox.setItems(RoleIntervention.values());

            roleMap.put(personnel, roleComboBox);
            layout.add(roleComboBox);
        }

        Button confirmBtn = new Button("Confirmer", event -> {
            Map<Long, RoleIntervention> personnelsAvecRoles = new HashMap<>();

            for (Map.Entry<Personnel, ComboBox<RoleIntervention>> entry : roleMap.entrySet()) {
                RoleIntervention role = entry.getValue().getValue();

                if (role == null) {
                    entry.getValue().setInvalid(true);
                    entry.getValue().setErrorMessage("Le rôle est obligatoire");
                    return;
                }

                personnelsAvecRoles.put(entry.getKey().getId(), role);
            }

            try {
                interventionService.createIntervention(
                        typeIntervention,
                        priorite,
                        dateHeureDebut,
                        dureePrevue,
                        patient.getId(),
                        salle.getId(),
                        personnelsAvecRoles,
                        unitesMateriel.getValue().stream()
                                .map(UniteMateriel::getId)
                                .toList()
                );

                Notification.show("Intervention créée", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                roleDialog.close();
                parentDialog.close();
                loadInterventions(calendar);

            } catch (IllegalArgumentException e) {
                Notification.show(e.getMessage(), 4000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelBtn = new Button("Annuler", event -> roleDialog.close());

        layout.add(new HorizontalLayout(confirmBtn, cancelBtn));
        layout.setWidth("500px");

        roleDialog.add(layout);
        roleDialog.open();
    }

    private void openInterventionDetailsDialog(FullCalendar calendar, Long interventionId) {

        Intervention intervention = interventionService.findById(interventionId);

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Détails intervention");

        VerticalLayout layout = new VerticalLayout();

        layout.add(
                new Span("Type : " + intervention.getTypeIntervention()),
                new Span(
                        "Patient : " +
                                intervention.getPatient().getNomPatient() + " " +
                                intervention.getPatient().getPrenomPatient()
                ),
                new Span("Salle : " + intervention.getSalle().getNumeroSalle()),
                new Span("Début : " + intervention.getDateHeureDebut()),
                new Span("Durée : " + intervention.getDureePrevue() + " minutes"),
                new Span("Priorité : " + intervention.getPriorite()),
                new Span("Statut : " + intervention.getStatutIntervention())
        );

        HorizontalLayout actions = getHorizontalLayout(calendar, intervention, dialog);

        layout.add(actions);
        dialog.add(layout);
        dialog.open();
    }

    @NotNull
    private HorizontalLayout getHorizontalLayout(FullCalendar calendar, Intervention intervention, Dialog dialog) {
        Button annulerBtn = new Button("Annuler");

        annulerBtn.addClickListener(event -> {

            interventionService.annulerIntervention(intervention.getId());

            loadInterventions(calendar);

            dialog.close();

            Notification.show(
                    "Intervention annulée",
                    3000,
                    Notification.Position.BOTTOM_END
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button supprimerBtn = new Button("Supprimer");

        supprimerBtn.addClickListener(  _ -> {

            interventionService.deleteIntervention(intervention.getId());

            loadInterventions(calendar);

            dialog.close();

            Notification.show(
                    "Intervention supprimée",
                    3000,
                    Notification.Position.BOTTOM_END
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        Button fermerBtn = new Button("Fermer", event -> dialog.close());

        Button terminerBtn = new Button("Terminer intervention", event -> {
            try {
                interventionService.terminerIntervention(intervention.getId());

                loadInterventions(calendar);

                dialog.close();

                Notification.show(
                        "Intervention terminée et matériel envoyé en stérilisation",
                        4000,
                        Notification.Position.BOTTOM_END
                ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (IllegalArgumentException e) {
                Notification.show(
                        e.getMessage(),
                        4000,
                        Notification.Position.BOTTOM_END
                ).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        HorizontalLayout actions = new HorizontalLayout();

        Button demarrerBtn = getButton(calendar, intervention, dialog);

        if (intervention.getStatutIntervention() == StatutIntervention.PLANIFIEE) {
            actions.add(demarrerBtn);
        }

        if (intervention.getStatutIntervention() == StatutIntervention.EN_COURS) {
            actions.add(terminerBtn);
        }

        actions.add(annulerBtn, supprimerBtn, fermerBtn);

        return actions;
    }

    @NotNull
    private Button getButton(FullCalendar calendar, Intervention intervention, Dialog dialog) {
        Button demarrerBtn = new Button("Démarrer intervention");

        demarrerBtn.addClickListener(event -> {

            try {

                interventionService.demarrerIntervention(intervention.getId());

                loadInterventions(calendar);

                dialog.close();

                Notification.show(
                        "Intervention démarrée",
                        3000,
                        Notification.Position.BOTTOM_END
                ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (IllegalArgumentException e) {

                Notification.show(
                        e.getMessage(),
                        4000,
                        Notification.Position.BOTTOM_END
                ).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        return demarrerBtn;
    }
}