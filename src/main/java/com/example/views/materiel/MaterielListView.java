package com.example.views.materiel;

import com.example.base.ui.ViewTitle;
import com.example.entity.Materiel;
import com.example.entity.UniteMateriel;
import com.example.service.MaterielService;
import com.example.views.components.GridDialog;
import com.example.views.components.MaterielForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "materiels")
@PageTitle("Gestion du matériel")
@Menu(order = 5, icon = "icons/package.svg", title = "Données de base/Matériel")
public class MaterielListView extends VerticalLayout {

    private final MaterielService materielService;

    final TextField searchField;
    final Grid<Materiel> materielGrid;

    public MaterielListView(MaterielService materielService) {
        this.materielService = materielService;

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par nom ou type");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("30em");
        searchField.addValueChangeListener(_ -> refreshGrid());

        Button createBtn = new Button("Ajouter un matériel", _ -> openCreateDialog());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.addClassName("primary-action");

        var titleLine = new HorizontalLayout(new ViewTitle("Gestion des matériaux"));
        titleLine.setWidthFull();

        var searchLine = new HorizontalLayout(searchField, createBtn);
        searchLine.setWidthFull();
        searchLine.setWrap(true);
        searchLine.setFlexGrow(1, searchField);

        var toolbar = new VerticalLayout(titleLine, searchLine);
        toolbar.addClassName("page-toolbar");
        toolbar.setWidthFull();

        materielGrid = new Grid<>();
        refreshGrid();

        searchField.addValueChangeListener(_ -> refreshGrid());

        materielGrid.addColumn(Materiel::getNomMateriel)
                .setHeader("Nom")
                .setSortable(true);

        materielGrid.addColumn(Materiel::getTypeMateriel)
                .setHeader("Type")
                .setSortable(true);

        materielGrid.addColumn(materiel -> materiel.getStock() != null ? materiel.getStock().getQuantiteTotale() : 0)
                .setHeader("Quantité totale")
                .setSortable(true);

        materielGrid.addColumn(materiel -> materiel.getStock() != null ? materiel.getStock().getQuantiteDisponible() : 0)
                .setHeader("Quantité disponible")
                .setSortable(true);

        materielGrid.addColumn(materiel -> materiel.getStock() != null ? materiel.getStock().getSeuilAlerte() : 0)
                .setHeader("Seuil alerte")
                .setSortable(true);

        materielGrid.addComponentColumn(materiel -> {
            MenuBar menuBar = new MenuBar();
            menuBar.addClassName("grid-action-menu");

            var actions = menuBar.addItem("Actions");

            actions.getSubMenu().addItem("Voir unités", _ -> openUnitesDialog(materiel));
            actions.getSubMenu().addItem("Ajouter stock", _ -> openAjouterStockDialog(materiel));
            actions.getSubMenu().addItem("Modifier", _ -> openEditDialog(materiel));
            actions.getSubMenu().addItem("Supprimer", _ -> openDeleteDialog(materiel));

            return menuBar;
        }).setHeader("Actions").setWidth("130px").setFlexGrow(0);

        materielGrid.setEmptyStateText("Aucun matériel enregistré");
        materielGrid.setSizeFull();
        materielGrid.getStyle().set("overflow", "auto");
        materielGrid.setAllRowsVisible(false);
        materielGrid.addClassName("professional-grid");

        setSizeFull();
        addClassName("page-container");
        add(toolbar, materielGrid);
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Ajouter un matériel");
        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");

        MaterielForm form = new MaterielForm(
                null,
                "Ajouter le matériel",
                data -> {
                    try {
                        materielService.createMateriel(
                                data.nomMateriel(),
                                data.typeMateriel(),
                                data.quantiteTotale() == null ? 0 : data.quantiteTotale(),
                                data.quantiteDisponible() == null ? -1 : data.quantiteDisponible(),
                                data.seuilAlerte() == null ? -1 : data.seuilAlerte()
                        );

                        Notification.show("Matériel ajouté", 3000, Notification.Position.BOTTOM_END)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                        refreshGrid();
                        dialog.close();

                    } catch (IllegalArgumentException e) {
                        Notification.show(e.getMessage(), 3000, Notification.Position.BOTTOM_END)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
        );

        dialog.add(form);
        dialog.open();
    }

    private void openEditDialog(Materiel materiel) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Modifier le matériel");
        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");

        MaterielForm form = new MaterielForm(
                materiel,
                "Modifier le matériel",
                data -> {
                    try {
                        materielService.updateMateriel(
                                materiel,
                                data.nomMateriel(),
                                data.typeMateriel(),
                                data.seuilAlerte() == null ? -1 : data.seuilAlerte()
                        );

                        Notification.show("Matériel modifié", 3000, Notification.Position.BOTTOM_END)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                        refreshGrid();
                        dialog.close();

                    } catch (IllegalArgumentException e) {
                        Notification.show(e.getMessage(), 3000, Notification.Position.BOTTOM_END)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
        );

        dialog.add(form);
        dialog.open();
    }

    private void refreshGrid() {
        String search = searchField.getValue() == null ? "" : searchField.getValue().trim().toLowerCase();

        materielGrid.setItems(
                materielService.findAll().stream()
                        .filter(materiel ->
                                search.isBlank()
                                        || materiel.getNomMateriel().toLowerCase().contains(search)
                                        || materiel.getTypeMateriel().toLowerCase().contains(search)
                        )
                        .toList()
        );
    }

    private void openUnitesDialog(Materiel materiel) {
        Grid<UniteMateriel> uniteGrid = new Grid<>();

        uniteGrid.addColumn(UniteMateriel::getCodeInventaire)
                .setHeader("Code inventaire")
                .setSortable(true);

        uniteGrid.addComponentColumn(unite -> {
            Span badge = new Span(unite.getEtat() == null ? "" : unite.getEtat().name());

            if (unite.getEtat() == null) {
                badge.addClassName("status-neutral");
            } else {
                switch (unite.getEtat()) {
                    case STERILE -> badge.addClassName("status-success");
                    case EN_STERILISATION, RESERVE, EN_UTILISATION -> badge.addClassName("status-info");
                    case HS, INDISPONIBLE -> badge.addClassName("status-danger");
                    default -> badge.addClassName("status-neutral");
                }
            }

            return badge;
        }).setHeader("État");

        uniteGrid.setItems(materielService.findUnitesByMateriel(materiel));
        uniteGrid.setEmptyStateText("Aucune unité enregistrée");

        new GridDialog<>(
                "Unités du matériel : " + materiel.getNomMateriel(),
                uniteGrid
        ).open();
    }

    private void openAjouterStockDialog(Materiel materiel) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Ajouter du stock : " + materiel.getNomMateriel());

        IntegerField quantiteAjoutee = new IntegerField();
        quantiteAjoutee.setLabel("Quantité à ajouter");
        quantiteAjoutee.setMin(1);
        quantiteAjoutee.setValue(1);

        Button confirmBtn = new Button("Ajouter", _ -> {
            Integer quantite = quantiteAjoutee.getValue();

            try {
                materielService.ajouterStock(materiel, quantite);
                refreshGrid();
                dialog.close();

                Notification.show("Stock ajouté", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (IllegalArgumentException e) {
                Notification.show(e.getMessage(), 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Annuler", event -> dialog.close());

        HorizontalLayout actions = new HorizontalLayout(confirmBtn, cancelBtn);

        VerticalLayout layout = new VerticalLayout(quantiteAjoutee, actions);
        layout.setWidth("400px");

        dialog.add(layout);
        dialog.open();
    }

    private void openDeleteDialog(Materiel materiel) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmation de suppression");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add("Voulez-vous vraiment supprimer le matériel : " + materiel.getNomMateriel() + " ?");

        Button confirmBtn = new Button("Oui, supprimer", _ -> {
            materielService.deleteMateriel(materiel);
            refreshGrid();
            confirmDialog.close();

            Notification.show("Matériel supprimé", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button closeBtn = new Button("Annuler", _ -> confirmDialog.close());

        HorizontalLayout actions = new HorizontalLayout(confirmBtn, closeBtn);
        dialogLayout.add(actions);

        confirmDialog.add(dialogLayout);
        confirmDialog.open();
    }
}