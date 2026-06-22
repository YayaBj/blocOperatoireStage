package com.example.views.sterilisation;

import com.example.base.ui.ViewTitle;
import com.example.entity.DemandeSterilisation;
import com.example.entity.enums.StatutDemandeSterilisation;
import com.example.service.BoiteChirurgicaleService;
import com.example.service.DemandeSterilisationService;
import com.example.service.InterventionService;
import com.example.views.components.DemandeSterilisationForm;
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
import org.jetbrains.annotations.NotNull;

@Route("demandes-sterilisation")
@PageTitle("Demandes de stérilisation")
@Menu(order = 8, icon = "icons/clipboard-check.svg", title = "Stérilisation/Demandes")
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

        Button createBtn = new Button("Créer demande", _ -> openCreateDialog());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.addClassName("primary-action");

        var titleLine = new HorizontalLayout(new ViewTitle("Gestion des demandes"));
        titleLine.setWidthFull();

        var searchLine = new HorizontalLayout(searchField, createBtn);
        searchLine.setWidthFull();
        searchLine.setWrap(true);
        searchLine.setFlexGrow(1, searchField);

        var toolbar = new VerticalLayout(titleLine, searchLine);
        toolbar.addClassName("page-toolbar");
        toolbar.setWidthFull();

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

        demandeGrid.addComponentColumn(demande -> {
            Span badge = getStatutBadge(demande);

            return badge;
        }).setHeader("Statut");

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
        demandeGrid.addClassName("professional-grid");

        setSizeFull();
        addClassName("page-container");
        add(toolbar, demandeGrid);
        refreshGrid();
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Créer une demande de stérilisation");
        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");

        DemandeSterilisationForm form = new DemandeSterilisationForm(
                boiteService.findAll(),
                interventionService.findAll(),
                data -> {
                    try {
                        demandeService.createDemande(
                                data.codeDemande(),
                                data.dateSouhaitee(),
                                data.priorite(),
                                data.boiteId(),
                                data.interventionId(),
                                data.commentaire()
                        );

                        Notification.show("Demande créée", 3000, Notification.Position.BOTTOM_END)
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

    private void openDetailsDialog(DemandeSterilisation demande) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Détails demande : " + demande.getCodeDemande());
        dialog.setWidth("750px");
        dialog.setMaxWidth("95vw");

        Span statutBadge = getStatutBadge(demande);

        VerticalLayout content = new VerticalLayout();
        content.addClassName("intervention-form");
        content.setWidthFull();

        com.vaadin.flow.component.html.Div section = new com.vaadin.flow.component.html.Div();
        section.addClassName("form-section");

        Span title = new Span("Informations de la demande");
        title.addClassName("form-section-title");

        Span description = new Span("Résumé des informations liées à la demande de stérilisation.");
        description.addClassName("form-section-description");

        section.add(
                title,
                description,
                createDetailLine("Code", demande.getCodeDemande()),
                createDetailLine(
                        "Boîte",
                        demande.getBoiteChirurgicale().getCodeBoite()
                                + " - "
                                + demande.getBoiteChirurgicale().getNom()
                ),
                createDetailLine("Date demande", String.valueOf(demande.getDateDemande())),
                createDetailLine("Date souhaitée", String.valueOf(demande.getDateSouhaitee())),
                createDetailLine("Priorité", String.valueOf(demande.getPriorite())),
                createDetailLine("Statut", ""),
                statutBadge,
                createDetailLine("Commentaire", nullSafe(demande.getCommentaire()))
        );

        Button closeBtn = new Button("Fermer", event -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        closeBtn.addClassName("primary-action");

        HorizontalLayout actions = new HorizontalLayout(closeBtn);
        actions.addClassName("form-actions");
        actions.setWidthFull();

        content.add(section, actions);

        dialog.add(content);
        dialog.open();
    }

    @NotNull
    private static Span getStatutBadge(DemandeSterilisation demande) {
        Span statutBadge = new Span(demande.getStatut() == null ? "" : demande.getStatut().name());

        if (demande.getStatut() == null) {
            statutBadge.addClassName("status-neutral");
        } else {
            switch (demande.getStatut()) {
                case BROUILLON -> statutBadge.addClassName("status-neutral");
                case ENVOYEE, EN_COURS -> statutBadge.addClassName("status-info");
                case ACCEPTEE, TERMINEE -> statutBadge.addClassName("status-success");
                case REFUSEE, ANNULEE -> statutBadge.addClassName("status-danger");
            }
        }
        return statutBadge;
    }

    private HorizontalLayout createDetailLine(String label, String value) {
        Span labelSpan = new Span(label + " :");
        labelSpan.getStyle()
                .set("font-weight", "600")
                .set("color", "#334155")
                .set("min-width", "130px");

        Span valueSpan = new Span(value == null || value.isBlank() ? "-" : value);
        valueSpan.getStyle()
                .set("color", "#0f172a")
                .set("white-space", "normal")
                .set("word-break", "break-word");

        HorizontalLayout line = new HorizontalLayout(labelSpan, valueSpan);
        line.setWidthFull();
        line.setSpacing(true);

        return line;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
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

    private boolean matchesSearch(DemandeSterilisation d, String search) {
        String code = nullSafe(d.getCodeDemande()).toLowerCase();
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
    }

    private void refreshGrid() {
        String search = searchField.getValue() == null
                ? ""
                : searchField.getValue().trim().toLowerCase();

        demandeGrid.setItems(
                demandeService.findAll().stream()
                        .filter(d -> matchesSearch(d, search))
                        .toList()
        );
    }
}