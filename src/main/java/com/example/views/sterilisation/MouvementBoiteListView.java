package com.example.views.sterilisation;

import com.example.entity.MouvementBoite;
import com.example.service.MouvementBoiteService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("mouvements-boites")
@PageTitle("Mouvements de boîtes")
@Menu(order = 12, icon = "icons/arrows-right-left.svg", title = "Mouvements boîtes")
public class MouvementBoiteListView extends VerticalLayout {

    private final MouvementBoiteService mouvementBoiteService;
    private final TextField searchField;
    private final Grid<MouvementBoite> mouvementGrid;

    public MouvementBoiteListView(MouvementBoiteService mouvementBoiteService) {
        this.mouvementBoiteService = mouvementBoiteService;

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par boîte, zone, étape ou commentaire");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("30em");
        searchField.addValueChangeListener(event -> refreshGrid());

        mouvementGrid = new Grid<>();

        mouvementGrid.addColumn(m -> m.getBoiteChirurgicale().getCodeBoite())
                .setHeader("Boîte")
                .setSortable(true);

        mouvementGrid.addColumn(MouvementBoite::getAncienneZone)
                .setHeader("Ancienne zone")
                .setSortable(true);

        mouvementGrid.addColumn(MouvementBoite::getNouvelleZone)
                .setHeader("Nouvelle zone")
                .setSortable(true);

        mouvementGrid.addColumn(MouvementBoite::getTypeMouvement)
                .setHeader("Type mouvement")
                .setSortable(true);

        mouvementGrid.addColumn(MouvementBoite::getDateMouvement)
                .setHeader("Date")
                .setSortable(true);

        mouvementGrid.addColumn(MouvementBoite::getCommentaire)
                .setHeader("Commentaire");

        mouvementGrid.setEmptyStateText("Aucun mouvement enregistré");
        mouvementGrid.setSizeFull();

        setSizeFull();
        add(searchField, mouvementGrid);

        refreshGrid();
    }

    private void refreshGrid() {
        String search = searchField.getValue() == null
                ? ""
                : searchField.getValue().trim().toLowerCase();

        mouvementGrid.setItems(
                mouvementBoiteService.findAll().stream()
                        .filter(m -> {
                            String boite = m.getBoiteChirurgicale() == null ? "" : m.getBoiteChirurgicale().getCodeBoite().toLowerCase();
                            String ancienneZone = m.getAncienneZone() == null ? "" : m.getAncienneZone().name().toLowerCase();
                            String nouvelleZone = m.getNouvelleZone() == null ? "" : m.getNouvelleZone().name().toLowerCase();
                            String type = m.getTypeMouvement() == null ? "" : m.getTypeMouvement().name().toLowerCase();
                            String commentaire = m.getCommentaire() == null ? "" : m.getCommentaire().toLowerCase();

                            return search.isBlank()
                                    || boite.contains(search)
                                    || ancienneZone.contains(search)
                                    || nouvelleZone.contains(search)
                                    || type.contains(search)
                                    || commentaire.contains(search);
                        })
                        .toList()
        );
    }
}