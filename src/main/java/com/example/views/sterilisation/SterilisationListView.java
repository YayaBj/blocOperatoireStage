package com.example.views.sterilisation;

import com.example.entity.Sterilisation;
import com.example.service.SterilisationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("sterilisations")
@PageTitle("Stérilisation")
@Menu(order = 6, icon = "icons/refresh.svg", title = "Stérilisation")
public class SterilisationListView extends VerticalLayout {

    private final SterilisationService sterilisationService;

    private final Grid<Sterilisation> sterilisationGrid;

    public SterilisationListView(SterilisationService sterilisationService) {
        this.sterilisationService = sterilisationService;

        sterilisationGrid = new Grid<>();

        sterilisationGrid.addColumn(s ->
                s.getUniteMateriel().getCodeInventaire()
        ).setHeader("Code inventaire");

        sterilisationGrid.addColumn(s ->
                s.getUniteMateriel().getMateriel().getNomMateriel()
        ).setHeader("Matériel");

        sterilisationGrid.addColumn(Sterilisation::getDateDebut)
                .setHeader("Date début");

        sterilisationGrid.addColumn(Sterilisation::getStatut)
                .setHeader("Statut");

        sterilisationGrid.addComponentColumn(sterilisation -> {
            Button validerBtn = new Button("Valider stérilisation");

            validerBtn.addClickListener(event -> {
                try {
                    sterilisationService.validerSterilisation(sterilisation);
                    refreshGrid();

                    Notification.show("Matériel stérilisé", 3000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                } catch (IllegalArgumentException e) {
                    Notification.show(e.getMessage(), 3000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });

            return validerBtn;
        }).setHeader("Actions");

        sterilisationGrid.setEmptyStateText("Aucun matériel en stérilisation");
        sterilisationGrid.setSizeFull();

        setSizeFull();
        add(sterilisationGrid);
        refreshGrid();
    }

    private void refreshGrid() {
        sterilisationGrid.setItems(sterilisationService.findSterilisationsEnCours());
    }
}