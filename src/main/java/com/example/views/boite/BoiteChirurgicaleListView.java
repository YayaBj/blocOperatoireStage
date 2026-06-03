package com.example.views.boite;

import com.example.entity.BoiteChirurgicale;
import com.example.entity.BoiteMateriel;
import com.example.entity.UniteMateriel;
import com.example.entity.enums.PrioriteIntervention;
import com.example.service.BoiteChirurgicaleService;
import com.example.service.UniteMaterielService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
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

@Route("boites")
@PageTitle("Boîtes chirurgicales")
@Menu(order = 8, icon = "icons/box.svg", title = "Boîtes")
public class BoiteChirurgicaleListView extends VerticalLayout {

    private final BoiteChirurgicaleService boiteService;
    private final UniteMaterielService uniteMaterielService;

    private final TextField searchField;
    private final Grid<BoiteChirurgicale> boiteGrid;

    public BoiteChirurgicaleListView(
            BoiteChirurgicaleService boiteService,
            UniteMaterielService uniteMaterielService
    ) {
        this.boiteService = boiteService;
        this.uniteMaterielService = uniteMaterielService;

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par code, nom, priorité, département ou spécialité");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("30em");
        searchField.addValueChangeListener(event -> refreshGrid());

        Button createBtn = new Button("Créer boîte", event -> openCreateDialog());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(searchField, createBtn);
        toolbar.setWidthFull();
        toolbar.setWrap(true);

        boiteGrid = new Grid<>();

        boiteGrid.addColumn(BoiteChirurgicale::getCodeBoite)
                .setHeader("Code")
                .setSortable(true);

        boiteGrid.addColumn(BoiteChirurgicale::getNom)
                .setHeader("Nom")
                .setSortable(true);

        boiteGrid.addColumn(BoiteChirurgicale::getPriorite)
                .setHeader("Priorité")
                .setSortable(true);

        boiteGrid.addColumn(BoiteChirurgicale::getStatut)
                .setHeader("Statut")
                .setSortable(true);

        boiteGrid.addColumn(BoiteChirurgicale::getDepartement)
                .setHeader("Département")
                .setSortable(true);

        boiteGrid.addColumn(BoiteChirurgicale::getSpecialite)
                .setHeader("Spécialité")
                .setSortable(true);

        boiteGrid.addColumn(boite -> boiteService.findMaterielsByBoite(boite).size())
                .setHeader("Articles")
                .setSortable(true);

        boiteGrid.addComponentColumn(boite -> {
            Button voirBtn = new Button("Voir contenu");
            voirBtn.addClickListener(event -> openContenuDialog(boite));

            Button modifierBtn = new Button("Modifier");
            modifierBtn.addClickListener(event -> openEditDialog(boite));

            Button supprimerBtn = new Button("Supprimer");
            supprimerBtn.addClickListener(event -> openDeleteDialog(boite));

            HorizontalLayout actions = new HorizontalLayout(voirBtn, modifierBtn, supprimerBtn);
            actions.setWrap(false);

            return actions;
        }).setHeader("Actions").setWidth("330px").setFlexGrow(0);

        boiteGrid.setEmptyStateText("Aucune boîte chirurgicale enregistrée");
        boiteGrid.setSizeFull();
        boiteGrid.setWidthFull();
        boiteGrid.getStyle().set("overflow-x", "auto");

        setSizeFull();
        add(toolbar, boiteGrid);

        refreshGrid();
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Créer une boîte chirurgicale");

        TextField codeBoite = new TextField("Code boîte");
        TextField nom = new TextField("Nom");
        TextField departement = new TextField("Département");
        TextField specialite = new TextField("Spécialité");

        ComboBox<PrioriteIntervention> priorite = new ComboBox<>("Priorité");
        priorite.setItems(PrioriteIntervention.values());

        MultiSelectComboBox<UniteMateriel> materiels = new MultiSelectComboBox<>("Matériels");
        materiels.setItems(uniteMaterielService.findAll());
        materiels.setItemLabelGenerator(unite ->
                unite.getCodeInventaire() + " - " + unite.getMateriel().getNomMateriel()
        );

        Button saveBtn = new Button("Créer", event -> {
            try {
                boiteService.createBoite(
                        codeBoite.getValue(),
                        nom.getValue(),
                        priorite.getValue(),
                        departement.getValue(),
                        specialite.getValue(),
                        materiels.getValue().stream()
                                .map(UniteMateriel::getId)
                                .toList()
                );

                Notification.show("Boîte créée", 3000, Notification.Position.BOTTOM_END)
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
                codeBoite,
                nom,
                departement,
                specialite,
                priorite,
                materiels,
                new HorizontalLayout(saveBtn, cancelBtn)
        );

        layout.setWidth("600px");

        dialog.add(layout);
        dialog.open();
    }

    private void openContenuDialog(BoiteChirurgicale boite) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Contenu de la boîte : " + boite.getNom());

        Grid<BoiteMateriel> contenuGrid = new Grid<>();

        contenuGrid.addColumn(bm -> bm.getUniteMateriel().getCodeInventaire())
                .setHeader("Code inventaire")
                .setSortable(true);

        contenuGrid.addColumn(bm -> bm.getUniteMateriel().getMateriel().getNomMateriel())
                .setHeader("Matériel")
                .setSortable(true);

        contenuGrid.addColumn(bm -> bm.getUniteMateriel().getEtat())
                .setHeader("État")
                .setSortable(true);

        contenuGrid.setItems(boiteService.findMaterielsByBoite(boite));
        contenuGrid.setEmptyStateText("Aucun matériel dans cette boîte");
        contenuGrid.setSizeFull();

        Button closeBtn = new Button("Fermer", event -> dialog.close());

        VerticalLayout layout = new VerticalLayout(contenuGrid, closeBtn);
        layout.setWidth("700px");
        layout.setHeight("500px");

        dialog.add(layout);
        dialog.open();
    }

    private void refreshGrid() {
        String search = searchField.getValue() == null
                ? ""
                : searchField.getValue().trim().toLowerCase();

        boiteGrid.setItems(
                boiteService.findAll().stream()
                        .filter(boite -> {
                            String code = boite.getCodeBoite() == null ? "" : boite.getCodeBoite().toLowerCase();
                            String nom = boite.getNom() == null ? "" : boite.getNom().toLowerCase();
                            String priorite = boite.getPriorite() == null ? "" : boite.getPriorite().name().toLowerCase();
                            String statut = boite.getStatut() == null ? "" : boite.getStatut().name().toLowerCase();
                            String departement = boite.getDepartement() == null ? "" : boite.getDepartement().toLowerCase();
                            String specialite = boite.getSpecialite() == null ? "" : boite.getSpecialite().toLowerCase();

                            return search.isBlank()
                                    || code.contains(search)
                                    || nom.contains(search)
                                    || priorite.contains(search)
                                    || statut.contains(search)
                                    || departement.contains(search)
                                    || specialite.contains(search);
                        })
                        .toList()
        );
    }

    private void openEditDialog(BoiteChirurgicale boite) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Modifier la boîte");

        TextField codeBoite = new TextField("Code boîte");
        codeBoite.setValue(boite.getCodeBoite());

        TextField nom = new TextField("Nom");
        nom.setValue(boite.getNom());

        TextField departement = new TextField("Département");
        departement.setValue(boite.getDepartement() == null ? "" : boite.getDepartement());

        TextField specialite = new TextField("Spécialité");
        specialite.setValue(boite.getSpecialite() == null ? "" : boite.getSpecialite());

        ComboBox<PrioriteIntervention> priorite = new ComboBox<>("Priorité");
        priorite.setItems(PrioriteIntervention.values());
        priorite.setValue(boite.getPriorite());

        MultiSelectComboBox<UniteMateriel> materiels = new MultiSelectComboBox<>("Matériels");
        materiels.setItems(uniteMaterielService.findAll());
        materiels.setItemLabelGenerator(unite ->
                unite.getCodeInventaire() + " - " + unite.getMateriel().getNomMateriel()
        );

        var allUnites = uniteMaterielService.findAll();
        materiels.setItems(allUnites);

        var selectedIds = boiteService.findMaterielsByBoite(boite).stream()
                .map(bm -> bm.getUniteMateriel().getId())
                .toList();

        materiels.select(
                allUnites.stream()
                        .filter(unite -> selectedIds.contains(unite.getId()))
                        .toList()
        );

        Button saveBtn = new Button("Modifier", event -> {
            try {
                boiteService.updateBoite(
                        boite,
                        codeBoite.getValue(),
                        nom.getValue(),
                        priorite.getValue(),
                        departement.getValue(),
                        specialite.getValue(),
                        materiels.getValue().stream()
                                .map(UniteMateriel::getId)
                                .toList()
                );

                Notification.show("Boîte modifiée", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                refreshGrid();
                dialog.close();

            } catch (IllegalArgumentException e) {
                Notification.show(e.getMessage(), 4000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelBtn = new Button("Annuler", event -> dialog.close());

        VerticalLayout layout = new VerticalLayout(
                codeBoite,
                nom,
                departement,
                specialite,
                priorite,
                materiels,
                new HorizontalLayout(saveBtn, cancelBtn)
        );

        layout.setWidth("600px");

        dialog.add(layout);
        dialog.open();
    }

    private void openDeleteDialog(BoiteChirurgicale boite) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirmation de suppression");

        Button confirmBtn = new Button("Oui, supprimer", event -> {
            try {
                boiteService.deleteBoite(boite);
                refreshGrid();
                dialog.close();

                Notification.show("Boîte supprimée", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (IllegalArgumentException e) {
                Notification.show(e.getMessage(), 4000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelBtn = new Button("Annuler", event -> dialog.close());

        VerticalLayout layout = new VerticalLayout(
                new Span("Voulez-vous vraiment supprimer la boîte : " + boite.getCodeBoite() + " ?"),
                new HorizontalLayout(confirmBtn, cancelBtn)
        );

        dialog.add(layout);
        dialog.open();
    }
}