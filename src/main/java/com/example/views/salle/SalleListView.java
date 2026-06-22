package com.example.views.salle;

import com.example.base.ui.ViewTitle;
import com.example.entity.Salle;
import com.example.service.SalleService;
import com.example.views.components.ConfirmDeleteDialog;
import com.example.views.components.SalleForm;
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

@Route(value = "salles")
@PageTitle("Gestion des salles")
@Menu(order = 4, icon = "icons/building.svg", title = "Données de base/Salles")
public class SalleListView extends VerticalLayout {

    private final SalleService salleService;

    final TextField searchField;
    final Grid<Salle> salleGrid;

    public SalleListView(SalleService salleService) {
        this.salleService = salleService;

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par numéro, type ou statut");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("30em");
        searchField.addValueChangeListener(_ -> refreshGrid());

        Button createBtn = new Button("Ajouter une salle", _ -> openCreateDialog());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.addClassName("primary-action");

        var titleLine = new HorizontalLayout(new ViewTitle("Gestion des salles"));
        titleLine.setWidthFull();

        var searchLine = new HorizontalLayout(searchField, createBtn);
        searchLine.setWidthFull();
        searchLine.setWrap(true);
        searchLine.setFlexGrow(1, searchField);

        var toolbar = new VerticalLayout(titleLine, searchLine);
        toolbar.addClassName("page-toolbar");
        toolbar.setWidthFull();

        salleGrid = new Grid<>();
        refreshGrid();
        searchField.addValueChangeListener(_ -> refreshGrid());

        salleGrid.addColumn(Salle::getNumeroSalle).setHeader("Numéro").setSortable(true);
        salleGrid.addColumn(Salle::getTypeSalle).setHeader("Type").setSortable(true);
        salleGrid.addComponentColumn(salle -> {
            Span badge = new Span(salle.getStatutSalle() == null ? "" : salle.getStatutSalle().name());

            if (salle.getStatutSalle() == null) {
                badge.addClassName("status-neutral");
            } else {
                switch (salle.getStatutSalle()) {
                    case DISPONIBLE -> badge.addClassName("status-success");
                    case EN_NETTOYAGE -> badge.addClassName("status-info");
                    case MAINTENANCE -> badge.addClassName("status-neutral");
                }
            }

            return badge;
        }).setHeader("Statut");

        salleGrid.addComponentColumn(salle -> {
            Button modifierBtn = new Button("Modifier");
            modifierBtn.addClickListener(_ -> openEditDialog(salle));

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

            deleteBtn.addClickListener(_ -> {
                ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(
                        "Voulez-vous vraiment supprimer la salle : " + salle.getNumeroSalle() + " ?",
                        () -> {
                            salleService.deleteSalle(salle);
                            refreshGrid();

                            Notification.show("Salle supprimée", 3000, Notification.Position.BOTTOM_END)
                                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        }
                );

                dialog.open();
            });

            return new HorizontalLayout(modifierBtn, deleteBtn);
        }).setHeader("Actions");

        salleGrid.setEmptyStateText("Aucune salle enregistrée");
        salleGrid.setSizeFull();
        salleGrid.addClassName("professional-grid");

        setSizeFull();
        addClassName("page-container");
        add(toolbar, salleGrid);
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Ajouter une salle");
        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");

        SalleForm form = new SalleForm(
                null,
                "Ajouter la salle",
                data -> {
                    try {
                        salleService.createSalle(
                                data.numeroSalle(),
                                data.typeSalle(),
                                data.statutSalle()
                        );

                        Notification.show("Salle ajoutée", 3000, Notification.Position.BOTTOM_END)
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

    private void openEditDialog(Salle salle) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Modifier la salle");
        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");

        SalleForm form = new SalleForm(
                salle,
                "Modifier la salle",
                data -> {
                    try {
                        salleService.updateSalle(
                                salle,
                                data.numeroSalle(),
                                data.typeSalle(),
                                data.statutSalle()
                        );

                        Notification.show("Salle modifiée", 3000, Notification.Position.BOTTOM_END)
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

        salleGrid.setItems(
                salleService.findAll().stream()
                        .filter(salle ->
                                search.isBlank()
                                        || salle.getNumeroSalle().toLowerCase().contains(search)
                                        || salle.getTypeSalle().toLowerCase().contains(search)
                                        || salle.getStatutSalle().name().toLowerCase().contains(search)
                        )
                        .toList()
        );
    }
}