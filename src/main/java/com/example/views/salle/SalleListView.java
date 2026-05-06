package com.example.views.salle;

import com.example.base.ui.ViewTitle;
import com.example.entity.Salle;
import com.example.entity.enums.StatutSalle;
import com.example.service.SalleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
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
@Menu(order = 3, icon = "icons/building.svg", title = "Salles")
public class SalleListView extends VerticalLayout {

    private final SalleService salleService;

    final TextField numeroSalle;
    final TextField typeSalle;
    final ComboBox<StatutSalle> statutSalle;
    final TextField searchField;
    final Button createBtn;
    final Button cancelBtn;
    final Grid<Salle> salleGrid;

    private Salle selectedSalle = null;

    public SalleListView(SalleService salleService) {
        this.salleService = salleService;

        numeroSalle = new TextField();
        numeroSalle.setPlaceholder("Numéro salle");

        typeSalle = new TextField();
        typeSalle.setPlaceholder("Type salle");

        statutSalle = new ComboBox<>();
        statutSalle.setPlaceholder("Statut salle");
        statutSalle.setItems(StatutSalle.values());

        createBtn = new Button("Ajouter", _ -> saveOrUpdateSalle());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancelBtn = new Button("Annuler", _ -> clearForm());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.setVisible(false);

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par numéro, type ou statut");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("18em");

        var toolbar = new VerticalLayout();
        var gestionSalle = new HorizontalLayout();
        gestionSalle.add(new ViewTitle("Gestion des salles"), numeroSalle, typeSalle, statutSalle, createBtn, cancelBtn);
        gestionSalle.setFlexGrow(1, numeroSalle, typeSalle, statutSalle);
        gestionSalle.setWrap(true);
        gestionSalle.setWidthFull();
        var searchLine = new HorizontalLayout();
        searchLine.add(searchField);
        searchLine.setFlexGrow(1, searchField);
        searchLine.setWrap(true);
        searchLine.setWidthFull();
        toolbar.add(gestionSalle, searchLine);

        salleGrid = new Grid<>();
        refreshGrid();
        searchField.addValueChangeListener(_ -> refreshGrid());

        salleGrid.addColumn(Salle::getNumeroSalle).setHeader("Numéro").setSortable(true);
        salleGrid.addColumn(Salle::getTypeSalle).setHeader("Type").setSortable(true);
        salleGrid.addColumn(Salle::getStatutSalle).setHeader("Statut").setSortable(true);

        salleGrid.addComponentColumn(salle -> {
            Button deleteBtn = new Button("Supprimer");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

            deleteBtn.addClickListener(_ -> {
                Dialog confirmDialog = new Dialog();
                confirmDialog.setHeaderTitle("Confirmation de suppression");

                VerticalLayout dialogLayout = new VerticalLayout();
                dialogLayout.add("Voulez-vous vraiment supprimer la salle : " + salle.getNumeroSalle() + " ?");

                Button confirmBtn = new Button("Oui, supprimer", event -> {
                    salleService.deleteSalle(salle);
                    refreshGrid();
                    confirmDialog.close();

                    Notification.show("Salle supprimée", 3000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                });
                confirmBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

                Button closeBtn = new Button("Annuler", event -> confirmDialog.close());

                HorizontalLayout actions = new HorizontalLayout(confirmBtn, closeBtn);
                dialogLayout.add(actions);
                confirmDialog.add(dialogLayout);
                confirmDialog.open();
            });

            return deleteBtn;
        }).setHeader("Actions");

        salleGrid.asSingleSelect().addValueChangeListener(event -> {
            selectedSalle = event.getValue();

            if (selectedSalle != null) {
                numeroSalle.setValue(selectedSalle.getNumeroSalle());
                typeSalle.setValue(selectedSalle.getTypeSalle());
                statutSalle.setValue(selectedSalle.getStatutSalle());
                createBtn.setText("Modifier");
                cancelBtn.setVisible(true);
            }
        });

        salleGrid.setEmptyStateText("Aucune salle enregistrée");
        salleGrid.setSizeFull();

        setSizeFull();
        add(toolbar, salleGrid);
    }

    private void saveOrUpdateSalle() {
        String numero = numeroSalle.getValue().trim().toUpperCase();
        String type = typeSalle.getValue().trim();
        StatutSalle statut = statutSalle.getValue();

        if (numero.isBlank()) {
            numeroSalle.setInvalid(true);
            numeroSalle.setErrorMessage("Le numéro de salle est obligatoire");
            return;
        }

        if (salleService.getSalleByNumeroSalle(numero) != null && selectedSalle == null) {
            numeroSalle.setInvalid(true);
            numeroSalle.setErrorMessage("Cette salle existe déjà");
            return;
        }

        numeroSalle.setInvalid(false);

        if (type.isBlank()) {
            typeSalle.setInvalid(true);
            typeSalle.setErrorMessage("Le type de salle est obligatoire");
            return;
        }

        typeSalle.setInvalid(false);

        if (statut == null) {
            statutSalle.setInvalid(true);
            statutSalle.setErrorMessage("Le statut de salle est obligatoire");
            return;
        }
        statutSalle.setInvalid(false);

        if (selectedSalle == null) {
            salleService.createSalle(numero, type, statut);

            Notification.show("Salle ajoutée", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            salleService.updateSalle(selectedSalle, numero, type, statut);

            Notification.show("Salle modifiée", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }

        refreshGrid();
        clearForm();
    }

    private void clearForm() {
        numeroSalle.clear();
        typeSalle.clear();
        statutSalle.clear();
        selectedSalle = null;
        createBtn.setText("Ajouter");
        cancelBtn.setVisible(false);
        salleGrid.asSingleSelect().clear();
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