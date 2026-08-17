package com.example.views;

import com.example.base.ui.ViewTitle;
import com.example.entity.*;
import com.example.entity.enums.*;
import com.example.service.*;
import com.example.views.components.InterventionForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import org.vaadin.stefan.fullcalendar.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

@Route("")
@PageTitle("Planning")
@Menu(order = 1, icon = "icons/calendar.svg", title = "Planning")
public class PlanningView extends VerticalLayout {

    private final InterventionService interventionService;
    private final PatientService patientService;
    private final SalleService salleService;
    private final PersonnelService personnelService;
    private final DemandeSterilisationService demandeService;
    private final MachineService machineService;

    private final HorizontalLayout dashboardCards;

    public PlanningView(
            InterventionService interventionService,
            PatientService patientService,
            SalleService salleService,
            PersonnelService personnelService,
            DemandeSterilisationService demandeService,
            MachineService machineService

    ) {
        this.patientService = patientService;
        this.salleService = salleService;
        this.personnelService = personnelService;
        this.interventionService = interventionService;
        this.demandeService = demandeService;
        this.machineService = machineService;

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

        VerticalLayout toolbar = createPlanningToolbar(calendar, datePicker);

        dashboardCards = new HorizontalLayout();
        dashboardCards.addClassName("dashboard-cards");
        dashboardCards.setWidthFull();
        dashboardCards.setWrap(true);

        refreshPlanning(calendar);

        calendar.addAttachListener(_ -> {
            calendar.changeView(CalendarViewImpl.TIME_GRID_WEEK);
            calendar.gotoDate(LocalDate.now());
        });

        calendar.addClassName("planning-calendar");

        Div calendarCard = new Div(calendar);
        calendarCard.addClassName("planning-calendar-card");

        setSizeFull();
        addClassName("page-container");
        add(toolbar, dashboardCards, calendarCard);
        expand(calendarCard);
    }

    private void getListenerDragAndDrop(InterventionService interventionService, FullCalendar calendar) {
        calendar.addEntryDroppedListener(event -> {

            Entry updatedEntry = event.createCopyBasedOnChanges();

            Long interventionId = Long.valueOf(
                    updatedEntry.getCustomProperty("interventionId").toString()
            );

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

                refreshPlanning(calendar);

            } catch (IllegalArgumentException e) {

                Notification.show(
                        e.getMessage(),
                        4000,
                        Notification.Position.BOTTOM_END
                ).addThemeVariants(NotificationVariant.LUMO_ERROR);

                refreshPlanning(calendar);
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

               refreshPlanning(calendar);

            } catch (IllegalArgumentException e) {

                Notification.show(
                        e.getMessage(),
                        4000,
                        Notification.Position.BOTTOM_END
                ).addThemeVariants(NotificationVariant.LUMO_ERROR);

               refreshPlanning(calendar);
            }
        });
    }

    private VerticalLayout createPlanningToolbar(FullCalendar calendar, DatePicker datePicker) {
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

        moisBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        semaineBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        jourBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout titleLine = new HorizontalLayout(new ViewTitle("Planning opératoire"));
        titleLine.setWidthFull();

        HorizontalLayout actionsLine = new HorizontalLayout(datePicker, moisBtn, semaineBtn, jourBtn);
        actionsLine.setWidthFull();
        actionsLine.setWrap(true);
        actionsLine.setAlignItems(Alignment.END);

        VerticalLayout toolbar = new VerticalLayout(titleLine, actionsLine);
        toolbar.addClassName("page-toolbar");
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

        InterventionForm form = new InterventionForm(
                patientService.findAll(),
                salleService.findByStatut(StatutSalle.DISPONIBLE),
                personnelService.findByEtat(EtatPersonnel.DISPONIBLE),
                interventionService.findBoitesDisponibles(),
                start,
                (int) Duration.between(start, end).toMinutes(),
                (data, personnelsAvecRoles) -> {
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
                       refreshPlanning(calendar);

                    } catch (IllegalArgumentException e) {
                        Notification.show(e.getMessage(), 4000, Notification.Position.BOTTOM_END)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
        );

        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");
        form.setWidthFull();

        dialog.add(form);
        dialog.open();
    }

    private void openInterventionDetailsDialog(FullCalendar calendar, Long interventionId) {
        Intervention intervention = interventionService.findById(interventionId);

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Détails de l’intervention");
        dialog.setWidth("700px");
        dialog.setMaxWidth("95vw");

        Span statutBadge = new Span(intervention.getStatutIntervention().name());

        switch (intervention.getStatutIntervention()) {
            case PLANIFIEE -> statutBadge.addClassName("status-neutral");
            case EN_COURS -> statutBadge.addClassName("status-info");
            case TERMINEE -> statutBadge.addClassName("status-success");
            case ANNULEE -> statutBadge.addClassName("status-danger");
        }

        Div detailsSection = new Div();
        detailsSection.addClassName("form-section");

        Span title = new Span("Informations de l’intervention");
        title.addClassName("form-section-title");

        detailsSection.add(
                title,
                createDetailLine("Type", intervention.getTypeIntervention()),
                createDetailLine("Patient", intervention.getPatient().getNomPatient() + " " + intervention.getPatient().getPrenomPatient()),
                createDetailLine("Salle", intervention.getSalle().getNumeroSalle()),
                createDetailLine("Début", String.valueOf(intervention.getDateHeureDebut())),
                createDetailLine("Durée", intervention.getDureePrevue() + " minutes"),
                createDetailLine("Priorité", String.valueOf(intervention.getPriorite())),
                statutBadge
        );

        HorizontalLayout actions = createActionButtons(calendar, intervention, dialog);

        VerticalLayout layout = new VerticalLayout(detailsSection, actions);
        layout.setWidthFull();

        dialog.add(layout);
        dialog.open();
    }

    private HorizontalLayout createDetailLine(String label, String value) {
        Span labelSpan = new Span(label + " :");
        labelSpan.getStyle()
                .set("font-weight", "600")
                .set("color", "#334155");

        Span valueSpan = new Span(value == null ? "" : value);

        HorizontalLayout line = new HorizontalLayout(labelSpan, valueSpan);
        line.setWidthFull();
        line.setSpacing(true);

        return line;
    }

    @NotNull
    private HorizontalLayout createActionButtons(FullCalendar calendar, Intervention intervention, Dialog dialog) {
        Button annulerBtn = new Button("Annuler");

        annulerBtn.addClickListener(event -> {

            interventionService.annulerIntervention(intervention.getId());

           refreshPlanning(calendar);

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

           refreshPlanning(calendar);

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

               refreshPlanning(calendar);

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

        Button demarrerBtn = createDemarrerButton(calendar, intervention, dialog);

        if (intervention.getStatutIntervention() == StatutIntervention.PLANIFIEE) {
            actions.add(demarrerBtn);
        }

        if (intervention.getStatutIntervention() == StatutIntervention.EN_COURS) {
            actions.add(terminerBtn);
        }

        if (intervention.getStatutIntervention() != StatutIntervention.ANNULEE
                && intervention.getStatutIntervention() != StatutIntervention.TERMINEE) {
            actions.add(annulerBtn);
        }

        actions.add(supprimerBtn, fermerBtn);

        return actions;
    }

    @NotNull
    private Button createDemarrerButton(FullCalendar calendar, Intervention intervention, Dialog dialog) {
        Button demarrerBtn = new Button("Démarrer intervention");

        demarrerBtn.addClickListener(event -> {

            try {

                interventionService.demarrerIntervention(intervention.getId());

               refreshPlanning(calendar);

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

    private void refreshDashboardCards() {
        dashboardCards.removeAll();

        long interventionsAujourdhui = interventionService.findAll().stream()
                .filter(intervention -> intervention.getDateHeureDebut() != null)
                .filter(intervention -> intervention.getDateHeureDebut().toLocalDate().equals(LocalDate.now()))
                .count();

        long demandesEnAttente = demandeService.findAll().stream()
                .filter(demande -> demande.getStatut() == StatutDemandeSterilisation.ENVOYEE)
                .count();

        long boitesDisponibles = interventionService.findBoitesDisponibles().size();

        long machinesEnPanne = machineService.findAll().stream()
                .filter(machine -> machine.getStatut() == StatutMachine.ERROR)
                .count();

        dashboardCards.add(
                createDashboardCard("Interventions aujourd’hui", interventionsAujourdhui, "Opérations prévues ce jour", "dashboard-card-info"),
                createDashboardCard("Demandes en attente", demandesEnAttente, "Demandes envoyées à traiter", "dashboard-card-warning"),
                createDashboardCard("Boîtes disponibles", boitesDisponibles, "Boîtes utilisables au bloc", "dashboard-card-success"),
                createDashboardCard("Machines en panne", machinesEnPanne, "Machines en erreur", "dashboard-card-danger")
        );
    }

    private Div createDashboardCard(String title, long value, String subtitle, String styleClass) {
        Div card = new Div();
        card.addClassName("dashboard-card");
        card.addClassName(styleClass);

        Span valueSpan = new Span(String.valueOf(value));
        valueSpan.addClassName("dashboard-card-value");

        Span titleSpan = new Span(title);
        titleSpan.addClassName("dashboard-card-title");

        Span subtitleSpan = new Span(subtitle);
        subtitleSpan.addClassName("dashboard-card-subtitle");

        card.add(valueSpan, titleSpan, subtitleSpan);

        return card;
    }

    private void refreshPlanning(FullCalendar calendar) {
        loadInterventions(calendar);
        refreshDashboardCards();
    }
}