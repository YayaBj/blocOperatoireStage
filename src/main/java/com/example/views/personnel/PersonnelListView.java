package com.example.views.personnel;

import com.example.base.ui.ViewTitle;
import com.example.entity.Personnel;
import com.example.service.PersonnelService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

@Route(value = "personnels")
@PageTitle("Gestion du personnel")
@Menu(order = 2, icon = "icons/users.svg", title = "Personnel")
public class PersonnelListView extends VerticalLayout {

    private final PersonnelService personnelService;

    final TextField matricule;
    final TextField nomPersonnel;
    final TextField prenomPersonnel;
    final TextField specialite;
    final TextField searchField;
    final Button createBtn;
    final Button cancelBtn;
    final Grid<Personnel> personnelGrid;

    private Personnel selectedPersonnel = null;

    public PersonnelListView(PersonnelService personnelService) {
        this.personnelService = personnelService;

        matricule = new TextField();
        matricule.setPlaceholder("Matricule");

        nomPersonnel = new TextField();
        nomPersonnel.setPlaceholder("Nom");

        prenomPersonnel = new TextField();
        prenomPersonnel.setPlaceholder("Prénom");

        specialite = new TextField();
        specialite.setPlaceholder("Spécialité");

        createBtn = new Button("Ajouter", _ -> saveOrUpdatePersonnel());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancelBtn = new Button("Annuler", _ -> clearForm());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.setVisible(false);

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par matricule, nom, prénom ou spécialité");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("18em");

        var toolbar = new VerticalLayout();
        var gestionPersonnel = new HorizontalLayout();
        gestionPersonnel.add(new ViewTitle("Gestion du personnel"), matricule, nomPersonnel, prenomPersonnel, specialite, createBtn, cancelBtn);
        gestionPersonnel.setFlexGrow(1, matricule, nomPersonnel, prenomPersonnel, specialite);
        gestionPersonnel.setWrap(true);
        gestionPersonnel.setWidthFull();
        var searchLine = new HorizontalLayout();
        searchLine.add(searchField);
        searchLine.setFlexGrow(1, searchField);
        searchLine.setWrap(true);
        searchLine.setWidthFull();
        toolbar.add(gestionPersonnel, searchLine);

        personnelGrid = new Grid<>();
        refreshGrid();
        searchField.addValueChangeListener(_ -> refreshGrid());

        personnelGrid.addColumn(Personnel::getMatricule).setHeader("Matricule").setSortable(true);
        personnelGrid.addColumn(Personnel::getNomPersonnel).setHeader("Nom").setSortable(true);
        personnelGrid.addColumn(Personnel::getPrenomPersonnel).setHeader("Prénom").setSortable(true);
        personnelGrid.addColumn(Personnel::getSpecialite).setHeader("Spécialité").setSortable(true);

        personnelGrid.addComponentColumn(personnel -> {
            Button deleteBtn = new Button("Supprimer");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

            deleteBtn.addClickListener(_ -> {
                Dialog confirmDialog = new Dialog();
                confirmDialog.setHeaderTitle("Confirmation de suppression");

                VerticalLayout dialogLayout = new VerticalLayout();
                dialogLayout.add("Voulez-vous vraiment supprimer : "
                        + personnel.getNomPersonnel() + " " + personnel.getPrenomPersonnel() + " ?");

                Button confirmBtn = new Button("Oui, supprimer", event -> {
                    personnelService.deletePersonnel(personnel);
                    refreshGrid();
                    confirmDialog.close();

                    Notification.show("Personnel supprimé", 3000, Notification.Position.BOTTOM_END)
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

        personnelGrid.asSingleSelect().addValueChangeListener(event -> {
            selectedPersonnel = event.getValue();

            if (selectedPersonnel != null) {
                matricule.setValue(selectedPersonnel.getMatricule());
                nomPersonnel.setValue(selectedPersonnel.getNomPersonnel());
                prenomPersonnel.setValue(selectedPersonnel.getPrenomPersonnel());
                specialite.setValue(selectedPersonnel.getSpecialite());
                createBtn.setText("Modifier");
                cancelBtn.setVisible(true);
            }
        });

        personnelGrid.setEmptyStateText("Aucun personnel enregistré");
        personnelGrid.setSizeFull();

        setSizeFull();
        add(toolbar, personnelGrid);
    }

    private void saveOrUpdatePersonnel() {
        try {
            if (selectedPersonnel == null) {
                personnelService.createPersonnel(
                        matricule.getValue(),
                        nomPersonnel.getValue(),
                        prenomPersonnel.getValue(),
                        specialite.getValue()
                );

                Notification.show("Personnel ajouté", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                personnelService.updatePersonnel(
                        selectedPersonnel,
                        matricule.getValue(),
                        nomPersonnel.getValue(),
                        prenomPersonnel.getValue(),
                        specialite.getValue()
                );

                Notification.show("Personnel modifié", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }

            refreshGrid();
            clearForm();

        } catch (IllegalArgumentException e) {
            Notification.show(e.getMessage(), 4000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearForm() {
        matricule.clear();
        nomPersonnel.clear();
        prenomPersonnel.clear();
        specialite.clear();
        selectedPersonnel = null;
        createBtn.setText("Ajouter");
        cancelBtn.setVisible(false);
        personnelGrid.asSingleSelect().clear();
    }

    private void refreshGrid() {
        String search = searchField.getValue() == null ? "" : searchField.getValue().trim().toLowerCase();

        personnelGrid.setItems(
                personnelService.findAll().stream()
                        .filter(personnel ->
                                search.isBlank()
                                        || personnel.getMatricule().toLowerCase().contains(search)
                                        || personnel.getNomPersonnel().toLowerCase().contains(search)
                                        || personnel.getPrenomPersonnel().toLowerCase().contains(search)
                                        || personnel.getSpecialite().toLowerCase().contains(search)
                        )
                        .toList()
        );
    }
}