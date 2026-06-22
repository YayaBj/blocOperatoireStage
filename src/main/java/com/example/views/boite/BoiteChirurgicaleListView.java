package com.example.views.boite;

import com.example.base.ui.ViewTitle;
import com.example.entity.BoiteChirurgicale;
import com.example.entity.BoiteMateriel;
import com.example.entity.MouvementBoite;
import com.example.service.BoiteChirurgicaleService;
import com.example.service.MouvementBoiteService;
import com.example.service.UniteMaterielService;
import com.example.views.components.BoiteForm;
import com.example.views.components.GridDialog;
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

import java.util.List;

@Route("boites")
@PageTitle("Boîtes chirurgicales")
@Menu(order = 7, icon = "icons/box.svg", title = "Bloc opératoire/Boîtes chirurgicales")
public class BoiteChirurgicaleListView extends VerticalLayout {

    private final BoiteChirurgicaleService boiteService;
    private final UniteMaterielService uniteMaterielService;
    private final MouvementBoiteService mouvementBoiteService;

    private final TextField searchField;
    private final Grid<BoiteChirurgicale> boiteGrid;

    public BoiteChirurgicaleListView(
            BoiteChirurgicaleService boiteService,
            UniteMaterielService uniteMaterielService,
            MouvementBoiteService mouvementBoiteService
    ) {
        this.boiteService = boiteService;
        this.uniteMaterielService = uniteMaterielService;
        this.mouvementBoiteService = mouvementBoiteService;

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par code, nom, priorité, département ou spécialité");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("30em");
        searchField.addValueChangeListener(_ -> refreshGrid());

        Button createBtn = new Button("Créer boîte", _ -> openCreateDialog());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.addClassName("primary-action");

        var titleLine = new HorizontalLayout(new ViewTitle("Gestion des boîtes chirurgicales"));
        titleLine.setWidthFull();

        var searchLine = new HorizontalLayout(searchField, createBtn);
        searchLine.setWidthFull();
        searchLine.setWrap(true);
        searchLine.setFlexGrow(1, searchField);

        var toolbar = new VerticalLayout(titleLine, searchLine);
        toolbar.addClassName("page-toolbar");
        toolbar.setWidthFull();

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

        boiteGrid.addComponentColumn(boite -> {
            Span badge = new Span(boite.getStatut().name());

            switch (boite.getStatut()) {
                case ACTIVE -> badge.addClassName("status-success");
                case EN_STERILISATION -> badge.addClassName("status-info");
                case INCIDENT -> badge.addClassName("status-danger");
                default -> badge.addClassName("status-neutral");
            }

            return badge;
        }).setHeader("Statut");

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
            Button voirBtn = new Button("Voir");
            voirBtn.addClickListener(_ -> openContenuDialog(boite));

            Button modifierBtn = new Button("Modifier");
            modifierBtn.addClickListener(_ -> openEditDialog(boite));

            Button supprimerBtn = new Button("Supprimer");
            supprimerBtn.addClickListener(_ -> openDeleteDialog(boite));

            Button mouvementsBtn = new Button("Mouvements");
            mouvementsBtn.addClickListener(_ -> openMouvementsDialog(boite));

            HorizontalLayout actions = new HorizontalLayout(voirBtn, mouvementsBtn, modifierBtn, supprimerBtn);
            actions.setWrap(false);

            return actions;
        }).setHeader("Actions").setWidth("450px").setFlexGrow(0);

        boiteGrid.setEmptyStateText("Aucune boîte chirurgicale enregistrée");
        boiteGrid.setSizeFull();
        boiteGrid.setWidthFull();
        boiteGrid.getStyle().set("overflow-x", "auto");

        toolbar.addClassName("page-toolbar");
        createBtn.addClassName("primary-action");

        boiteGrid.addClassName("professional-grid");

        setSizeFull();
        addClassName("page-container");
        add(toolbar, boiteGrid);

        refreshGrid();
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Créer une boîte chirurgicale");
        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");

        BoiteForm form = new BoiteForm(
                uniteMaterielService.findAll(),
                null,
                List.of(),
                "Créer la boîte",
                data -> {
                    try {
                        boiteService.createBoite(
                                data.codeBoite(),
                                data.nom(),
                                data.priorite(),
                                data.departement(),
                                data.specialite(),
                                data.uniteMaterielIds()
                        );

                        Notification.show("Boîte créée", 3000, Notification.Position.BOTTOM_END)
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

    private void openContenuDialog(BoiteChirurgicale boite) {
        Grid<BoiteMateriel> contenuGrid = new Grid<>();

        contenuGrid.addColumn(bm -> bm.getUniteMateriel().getCodeInventaire())
                .setHeader("Code inventaire")
                .setAutoWidth(true);

        contenuGrid.addColumn(bm -> bm.getUniteMateriel().getMateriel().getNomMateriel())
                .setHeader("Matériel")
                .setFlexGrow(1);

        contenuGrid.addColumn(bm -> bm.getUniteMateriel().getEtat())
                .setHeader("État")
                .setAutoWidth(true);

        contenuGrid.setItems(boiteService.findMaterielsByBoite(boite));
        contenuGrid.setEmptyStateText("Aucun matériel dans cette boîte");

        new GridDialog<>(
                "Contenu de la boîte : " + boite.getNom(),
                contenuGrid
        ).open();
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
        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");

        var allUnites = uniteMaterielService.findAll();

        var selectedIds = boiteService.findMaterielsByBoite(boite).stream()
                .map(bm -> bm.getUniteMateriel().getId())
                .toList();

        BoiteForm form = new BoiteForm(
                allUnites,
                boite,
                selectedIds,
                "Modifier la boîte",
                data -> {
                    try {
                        boiteService.updateBoite(
                                boite,
                                data.codeBoite(),
                                data.nom(),
                                data.priorite(),
                                data.departement(),
                                data.specialite(),
                                data.uniteMaterielIds()
                        );

                        Notification.show("Boîte modifiée", 3000, Notification.Position.BOTTOM_END)
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

    private void openMouvementsDialog(BoiteChirurgicale boite) {
        Grid<MouvementBoite> mouvementGrid = new Grid<>();

        mouvementGrid.addColumn(MouvementBoite::getDateMouvement)
                .setHeader("Date")
                .setAutoWidth(true);

        mouvementGrid.addColumn(MouvementBoite::getAncienneZone)
                .setHeader("Ancienne zone")
                .setAutoWidth(true);

        mouvementGrid.addColumn(MouvementBoite::getNouvelleZone)
                .setHeader("Nouvelle zone")
                .setAutoWidth(true);

        mouvementGrid.addColumn(MouvementBoite::getTypeMouvement)
                .setHeader("Type")
                .setAutoWidth(true);

        mouvementGrid.addComponentColumn(m -> {
                    Span span = new Span(m.getCommentaire());

                    span.getElement().setProperty(
                            "title",
                            m.getCommentaire() == null ? "" : m.getCommentaire()
                    );

                    return span;
                }).setHeader("Commentaire")
                .setFlexGrow(3);

        mouvementGrid.setItems(mouvementBoiteService.findByBoite(boite.getId()));
        mouvementGrid.setEmptyStateText("Aucun mouvement pour cette boîte");

        new GridDialog<>(
                "Mouvements de la boîte : " + boite.getCodeBoite(),
                mouvementGrid
        ).open();
    }
}