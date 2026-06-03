package com.example.views.sterilisation;

import com.example.entity.BoiteChirurgicale;
import com.example.entity.DemandeSterilisation;
import com.example.entity.Intervention;
import com.example.entity.enums.PrioriteIntervention;
import com.example.entity.enums.StatutDemandeSterilisation;
import com.example.service.BoiteChirurgicaleService;
import com.example.service.DemandeSterilisationService;
import com.example.service.InterventionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
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

@Route("demandes-sterilisation")
@PageTitle("Demandes de stérilisation")
@Menu(order = 9, icon = "icons/clipboard-check.svg", title = "Demandes stérilisation")
public class DemandeSterilisationListView extends VerticalLayout {

    private final DemandeSterilisationService demandeService;
    private final BoiteChirurgicaleService boiteService;
    private final InterventionService interventionService;

    private final TextField searchField;
    private final Grid<DemandeSterilisation> demandeGrid;

    public DemandeSterilisationListView(DemandeSterilisationService demandeService,
                                        BoiteChirurgicaleService boiteService,
                                        InterventionService interventionService) {
        this.demandeService = demandeService;
        this.boiteService = boiteService;
        this.interventionService = interventionService;

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par code, boîte, priorité, statut ou date");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("30em");
        searchField.addValueChangeListener(event -> refreshGrid());

        Button createBtn = new Button("Créer demande", event -> openCreateDialog());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(searchField, createBtn);
        toolbar.setWidthFull();
        toolbar.setWrap(true);

        demandeGrid = new Grid<>();

        demandeGrid.addColumn(DemandeSterilisation::getCodeDemande)
                .setHeader("Code")
                .setSortable(true);

        demandeGrid.addColumn(d -> d.getBoiteChirurgicale().getCodeBoite())
                .setHeader("Boîte")
                .setSortable(true);

        demandeGrid.addColumn(DemandeSterilisation::getDateDemande)
                .setHeader("Date demande")
                .setSortable(true);

        demandeGrid.addColumn(DemandeSterilisation::getDateSouhaitee)
                .setHeader("Date souhaitée")
                .setSortable(true);

        demandeGrid.addColumn(DemandeSterilisation::getPriorite)
                .setHeader("Priorité")
                .setSortable(true);

        demandeGrid.addColumn(DemandeSterilisation::getStatut)
                .setHeader("Statut")
                .setSortable(true);

        demandeGrid.addComponentColumn(demande -> {
            Button detailsBtn = new Button("Détails", event -> openDetailsDialog(demande));

            Button envoyerBtn = new Button("Envoyer", event -> executeAction(() ->
                    demandeService.envoyerDemande(demande.getId()), "Demande envoyée"));

            Button accepterBtn = new Button("Accepter", event -> executeAction(() ->
                    demandeService.accepterDemande(demande.getId()), "Demande acceptée"));

            Button refuserBtn = new Button("Refuser", event -> executeAction(() ->
                    demandeService.refuserDemande(demande.getId()), "Demande refusée"));

            Button annulerBtn = new Button("Annuler", event -> executeAction(() ->
                    demandeService.annulerDemande(demande.getId()), "Demande annulée"));

            HorizontalLayout actions = new HorizontalLayout();
            actions.setWrap(false);
            actions.add(detailsBtn);

            if (demande.getStatut() == StatutDemandeSterilisation.BROUILLON) {
                actions.add(envoyerBtn, annulerBtn);
            } else if (demande.getStatut() == StatutDemandeSterilisation.ENVOYEE) {
                actions.add(accepterBtn, refuserBtn, annulerBtn);
            } else if (demande.getStatut() == StatutDemandeSterilisation.ACCEPTEE) {
                actions.add(annulerBtn);
            }

            return actions;
        }).setHeader("Actions").setWidth("420px").setFlexGrow(0);

        demandeGrid.setEmptyStateText("Aucune demande de stérilisation");
        demandeGrid.setSizeFull();

        setSizeFull();
        add(toolbar, demandeGrid);
        refreshGrid();
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Créer une demande de stérilisation");

        TextField codeDemande = new TextField("Code demande");

        DatePicker dateSouhaitee = new DatePicker("Date souhaitée");

        ComboBox<PrioriteIntervention> priorite = new ComboBox<>("Priorité");
        priorite.setItems(PrioriteIntervention.values());

        ComboBox<BoiteChirurgicale> boite = new ComboBox<>("Boîte chirurgicale");
        boite.setItems(boiteService.findAll());
        boite.setItemLabelGenerator(b -> b.getCodeBoite() + " - " + b.getNom());

        ComboBox<Intervention> intervention = new ComboBox<>("Intervention liée");
        intervention.setItems(interventionService.findAll());
        intervention.setItemLabelGenerator(i ->
                "#" + i.getId() + " - " + i.getTypeIntervention()
        );
        intervention.setClearButtonVisible(true);

        TextArea commentaire = new TextArea("Commentaire");
        commentaire.setWidthFull();

        Button saveBtn = new Button("Créer", event -> {
            try {
                Long interventionId = intervention.getValue() == null
                        ? null
                        : intervention.getValue().getId();

                demandeService.createDemande(
                        codeDemande.getValue(),
                        dateSouhaitee.getValue(),
                        priorite.getValue(),
                        boite.getValue() == null ? null : boite.getValue().getId(),
                        interventionId,
                        commentaire.getValue()
                );

                Notification.show("Demande créée", 3000, Notification.Position.BOTTOM_END)
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
                codeDemande,
                dateSouhaitee,
                priorite,
                boite,
                intervention,
                commentaire,
                new HorizontalLayout(saveBtn, cancelBtn)
        );

        layout.setWidth("650px");

        dialog.add(layout);
        dialog.open();
    }

    private void openDetailsDialog(DemandeSterilisation demande) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Détails demande : " + demande.getCodeDemande());

        VerticalLayout layout = new VerticalLayout(
                new Span("Code : " + demande.getCodeDemande()),
                new Span("Boîte : " + demande.getBoiteChirurgicale().getCodeBoite() + " - " + demande.getBoiteChirurgicale().getNom()),
                new Span("Date demande : " + demande.getDateDemande()),
                new Span("Date souhaitée : " + demande.getDateSouhaitee()),
                new Span("Priorité : " + demande.getPriorite()),
                new Span("Statut : " + demande.getStatut()),
                new Span("Commentaire : " + (demande.getCommentaire() == null ? "" : demande.getCommentaire()))
        );

        Button closeBtn = new Button("Fermer", event -> dialog.close());
        layout.add(closeBtn);
        layout.setWidth("600px");

        dialog.add(layout);
        dialog.open();
    }

    private void executeAction(Runnable action, String successMessage) {
        try {
            action.run();
            refreshGrid();

            Notification.show(successMessage, 3000, Notification.Position.BOTTOM_END)
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

        demandeGrid.setItems(
                demandeService.findAll().stream()
                        .filter(d -> {
                            String code = d.getCodeDemande() == null ? "" : d.getCodeDemande().toLowerCase();
                            String boite = d.getBoiteChirurgicale() == null ? "" : d.getBoiteChirurgicale().getCodeBoite().toLowerCase();
                            String priorite = d.getPriorite() == null ? "" : d.getPriorite().name().toLowerCase();
                            String statut = d.getStatut() == null ? "" : d.getStatut().name().toLowerCase();
                            String dateDemande = d.getDateDemande() == null ? "" : d.getDateDemande().toString();
                            String dateSouhaitee = d.getDateSouhaitee() == null ? "" : d.getDateSouhaitee().toString();

                            return search.isBlank()
                                    || code.contains(search)
                                    || boite.contains(search)
                                    || priorite.contains(search)
                                    || statut.contains(search)
                                    || dateDemande.contains(search)
                                    || dateSouhaitee.contains(search);
                        })
                        .toList()
        );
    }
}