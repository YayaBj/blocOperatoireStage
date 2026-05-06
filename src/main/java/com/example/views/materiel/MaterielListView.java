package com.example.views.materiel;

import com.example.base.ui.ViewTitle;
import com.example.entity.Materiel;
import com.example.entity.UniteMateriel;
import com.example.service.MaterielService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

@Route(value = "materiels")
@PageTitle("Gestion du matériel")
@Menu(order = 4, icon = "icons/package.svg", title = "Matériel")
public class MaterielListView extends VerticalLayout {

    private final MaterielService materielService;

    final TextField nomMateriel;
    final TextField typeMateriel;
    final IntegerField quantiteTotale;
    final IntegerField quantiteDisponible;
    final IntegerField seuilAlerte;
    final TextField searchField;
    final Button createBtn;
    final Button cancelBtn;
    final Grid<Materiel> materielGrid;

    private Materiel selectedMateriel = null;

    public MaterielListView(MaterielService materielService) {
        this.materielService = materielService;

        nomMateriel = new TextField();
        nomMateriel.setPlaceholder("Nom matériel");

        typeMateriel = new TextField();
        typeMateriel.setPlaceholder("Type matériel");

        quantiteTotale = new IntegerField();
        quantiteTotale.setPlaceholder("Quantité totale");
        quantiteTotale.setMin(1);

        quantiteDisponible = new IntegerField();
        quantiteDisponible.setPlaceholder("Quantité disponible");
        quantiteDisponible.setMin(0);

        seuilAlerte = new IntegerField();
        seuilAlerte.setPlaceholder("Seuil alerte");
        seuilAlerte.setMin(0);

        createBtn = new Button("Ajouter", _ -> saveOrUpdateMateriel());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancelBtn = new Button("Annuler", _ -> clearForm());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.setVisible(false);

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par nom ou type");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("18em");

        var toolbar = new VerticalLayout();

        var gestionMateriel = new HorizontalLayout();
        gestionMateriel.add(
                new ViewTitle("Gestion du matériel"),
                nomMateriel,
                typeMateriel,
                quantiteTotale,
                quantiteDisponible,
                seuilAlerte,
                createBtn,
                cancelBtn
        );
        gestionMateriel.setFlexGrow(1, nomMateriel, typeMateriel, quantiteTotale, quantiteDisponible, seuilAlerte);
        gestionMateriel.setWrap(true);
        gestionMateriel.setWidthFull();

        var searchLine = new HorizontalLayout();
        searchLine.add(searchField);
        searchLine.setFlexGrow(1, searchField);
        searchLine.setWrap(true);
        searchLine.setWidthFull();

        toolbar.add(gestionMateriel, searchLine);

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
            Button voirUnitesBtn = new Button("Voir unités");
            voirUnitesBtn.addClickListener(_ -> openUnitesDialog(materiel));

            Button ajouterStockBtn = new Button("Ajouter stock");
            ajouterStockBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            ajouterStockBtn.addClickListener(_ -> openAjouterStockDialog(materiel));

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

            deleteBtn.addClickListener(_ -> {
                Dialog confirmDialog = new Dialog();
                confirmDialog.setHeaderTitle("Confirmation de suppression");

                VerticalLayout dialogLayout = new VerticalLayout();
                dialogLayout.add("Voulez-vous vraiment supprimer le matériel : " + materiel.getNomMateriel() + " ?");

                Button confirmBtn = new Button("Oui, supprimer", event -> {
                    materielService.deleteMateriel(materiel);
                    refreshGrid();
                    confirmDialog.close();

                    Notification.show("Matériel supprimé", 3000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                });
                confirmBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

                Button closeBtn = new Button("Annuler", event -> confirmDialog.close());

                HorizontalLayout actions = new HorizontalLayout(confirmBtn, closeBtn);
                dialogLayout.add(actions);
                confirmDialog.add(dialogLayout);
                confirmDialog.open();
            });

            return new HorizontalLayout(voirUnitesBtn, ajouterStockBtn, deleteBtn);
        }).setHeader("Actions");

        materielGrid.asSingleSelect().addValueChangeListener(event -> {
            selectedMateriel = event.getValue();

            if (selectedMateriel != null) {
                nomMateriel.setValue(selectedMateriel.getNomMateriel());
                typeMateriel.setValue(selectedMateriel.getTypeMateriel());

                if (selectedMateriel.getStock() != null) {
                    quantiteTotale.setValue(selectedMateriel.getStock().getQuantiteTotale());
                    quantiteDisponible.setValue(selectedMateriel.getStock().getQuantiteDisponible());
                    seuilAlerte.setValue(selectedMateriel.getStock().getSeuilAlerte());
                }

                quantiteTotale.setReadOnly(true);
                quantiteDisponible.setReadOnly(true);

                createBtn.setText("Modifier");
                cancelBtn.setVisible(true);
            }
        });

        materielGrid.setEmptyStateText("Aucun matériel enregistré");
        materielGrid.setSizeFull();

        setSizeFull();
        add(toolbar, materielGrid);
    }

    private void saveOrUpdateMateriel() {
        String nom = nomMateriel.getValue().trim();
        String type = typeMateriel.getValue().trim();
        Integer qteTotale = quantiteTotale.getValue();
        Integer qteDisponible = quantiteDisponible.getValue();
        Integer seuil = seuilAlerte.getValue();

        if (nom.isBlank()) {
            nomMateriel.setInvalid(true);
            nomMateriel.setErrorMessage("Le nom du matériel est obligatoire");
            return;
        }
        nomMateriel.setInvalid(false);

        if (type.isBlank()) {
            typeMateriel.setInvalid(true);
            typeMateriel.setErrorMessage("Le type du matériel est obligatoire");
            return;
        }
        typeMateriel.setInvalid(false);

        if (qteTotale == null || qteTotale <= 0) {
            quantiteTotale.setInvalid(true);
            quantiteTotale.setErrorMessage("La quantité totale doit être supérieure à 0");
            return;
        }
        quantiteTotale.setInvalid(false);

        if (qteDisponible == null || qteDisponible < 0) {
            quantiteDisponible.setInvalid(true);
            quantiteDisponible.setErrorMessage("La quantité disponible doit être positive ou égale à 0");
            return;
        }
        quantiteDisponible.setInvalid(false);

        if (qteDisponible > qteTotale) {
            quantiteDisponible.setInvalid(true);
            quantiteDisponible.setErrorMessage("La quantité disponible ne peut pas dépasser la quantité totale");
            return;
        }
        quantiteDisponible.setInvalid(false);

        if (seuil == null || seuil < 0 || seuil > qteTotale) {
            seuilAlerte.setInvalid(true);
            seuilAlerte.setErrorMessage("Le seuil d’alerte doit être entre 0 et la quantité totale");
            return;
        }
        seuilAlerte.setInvalid(false);

        try {
            if (selectedMateriel == null) {
                materielService.createMateriel(nom, type, qteTotale, qteDisponible, seuil);

                Notification.show("Matériel ajouté", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                materielService.updateMateriel(selectedMateriel, nom, type, seuil);

                Notification.show("Matériel modifié", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }

            refreshGrid();
            clearForm();

        } catch (IllegalArgumentException e) {
            Notification.show(e.getMessage(), 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearForm() {
        nomMateriel.clear();
        typeMateriel.clear();
        quantiteTotale.clear();
        quantiteDisponible.clear();
        seuilAlerte.clear();

        quantiteTotale.setReadOnly(false);
        quantiteDisponible.setReadOnly(false);

        selectedMateriel = null;
        createBtn.setText("Ajouter");
        cancelBtn.setVisible(false);
        materielGrid.asSingleSelect().clear();
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
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Unités du matériel : " + materiel.getNomMateriel());

        Grid<UniteMateriel> uniteGrid = new Grid<>();
        uniteGrid.addColumn(UniteMateriel::getCodeInventaire)
                .setHeader("Code inventaire")
                .setSortable(true);

        uniteGrid.addColumn(UniteMateriel::getEtat)
                .setHeader("État")
                .setSortable(true);

        uniteGrid.setItems(materielService.findUnitesByMateriel(materiel));
        uniteGrid.setEmptyStateText("Aucune unité enregistrée");
        uniteGrid.setWidthFull();

        Button closeBtn = new Button("Fermer", _ -> dialog.close());

        VerticalLayout layout = new VerticalLayout(uniteGrid, closeBtn);
        layout.setWidth("600px");

        dialog.add(layout);
        dialog.open();
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

            if (quantite == null || quantite <= 0) {
                quantiteAjoutee.setInvalid(true);
                quantiteAjoutee.setErrorMessage("La quantité doit être supérieure à 0");
                return;
            }

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
}