package com.example.views.sterilisation;

import com.example.entity.Sterilisation;
import com.example.entity.enums.StatutSterilisation;
import com.example.service.SterilisationService;
import com.vaadin.flow.component.button.Button;
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

@Route("sterilisations")
@PageTitle("Stérilisation")
@Menu(order = 7, icon = "icons/refresh.svg", title = "Stérilisation")
public class SterilisationListView extends VerticalLayout {

    private final SterilisationService sterilisationService;
    private final Grid<Sterilisation> sterilisationGrid;
    private final TextField searchField;

    public SterilisationListView(SterilisationService sterilisationService) {
        this.sterilisationService = sterilisationService;

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par code, matériel, état, étape ou date");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("30em");
        searchField.addValueChangeListener(event -> refreshGrid());

        HorizontalLayout toolbar = new HorizontalLayout(searchField);
        toolbar.setWidthFull();

        sterilisationGrid = new Grid<>();

        sterilisationGrid.addColumn(s -> s.getUniteMateriel().getCodeInventaire())
                .setHeader("Code inventaire")
                .setSortable(true);

        sterilisationGrid.addColumn(s -> s.getUniteMateriel().getMateriel().getNomMateriel())
                .setHeader("Matériel")
                .setSortable(true);

        sterilisationGrid.addColumn(s -> s.getUniteMateriel().getEtat())
                .setHeader("État matériel")
                .setSortable(true);

        sterilisationGrid.addColumn(Sterilisation::getDateDebut)
                .setHeader("Date début")
                .setSortable(true);

        sterilisationGrid.addColumn(Sterilisation::getDateFin)
                .setHeader("Date fin")
                .setSortable(true);

        sterilisationGrid.addColumn(Sterilisation::getStatut)
                .setHeader("Étape stérilisation")
                .setSortable(true);

        sterilisationGrid.addComponentColumn(sterilisation -> {
            if (sterilisation.getStatut() == StatutSterilisation.TERMINEE) {
                return new Span("Terminé");
            }

            if (sterilisation.getStatut() == StatutSterilisation.ECHEC) {
                return new Span("Échec");
            }

            Button avancerBtn = new Button("Passer à : " + getNextStatutLabel(sterilisation.getStatut()));

            avancerBtn.addClickListener(event -> {
                try {
                    sterilisationService.avancerSterilisation(sterilisation.getId());
                    refreshGrid();

                    Notification.show("Étape de stérilisation mise à jour", 3000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                } catch (IllegalArgumentException e) {
                    Notification.show(e.getMessage(), 3000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });

            return avancerBtn;
        }).setHeader("Actions");

        sterilisationGrid.setEmptyStateText("Aucun matériel en stérilisation");
        sterilisationGrid.setSizeFull();

        setSizeFull();
        add(toolbar, sterilisationGrid);
        refreshGrid();
    }

    private String getNextStatutLabel(StatutSterilisation statut) {
        StatutSterilisation next = sterilisationService.getNextStatut(statut);

        if (next == null) {
            return "Aucune étape";
        }

        return formatStatut(next);
    }


    private String formatStatut(StatutSterilisation statut) {
        return switch (statut) {
            case EN_ATTENTE_COLLECTE -> "attente collecte";
            case EN_TRANSPORT -> "transport";
            case EN_LAVAGE -> "lavage";
            case CONTROLE_QUALITE -> "contrôle qualité";
            case EN_EMBALLAGE -> "emballage";
            case EN_AUTOCLAVE -> "autoclave";
            case VALIDATION_CYCLE -> "validation cycle";
            case TERMINEE -> "terminée";
            case ECHEC -> "échec";
        };
    }

    private void refreshGrid() {
        String search = searchField.getValue() == null
                ? ""
                : searchField.getValue().trim().toLowerCase();

        sterilisationGrid.setItems(
                sterilisationService.findAll().stream()
                        .filter(sterilisation -> {
                            String codeInventaire = sterilisation.getUniteMateriel().getCodeInventaire().toLowerCase();
                            String nomMateriel = sterilisation.getUniteMateriel().getMateriel().getNomMateriel().toLowerCase();
                            String etatMateriel = sterilisation.getUniteMateriel().getEtat().name().toLowerCase();
                            String statut = sterilisation.getStatut().name().toLowerCase();

                            String dateDebut = sterilisation.getDateDebut() == null
                                    ? ""
                                    : sterilisation.getDateDebut().toString();

                            String dateFin = sterilisation.getDateFin() == null
                                    ? ""
                                    : sterilisation.getDateFin().toString();

                            return search.isBlank()
                                    || codeInventaire.contains(search)
                                    || nomMateriel.contains(search)
                                    || etatMateriel.contains(search)
                                    || statut.contains(search)
                                    || dateDebut.contains(search)
                                    || dateFin.contains(search);
                        })
                        .toList()
        );
    }
}