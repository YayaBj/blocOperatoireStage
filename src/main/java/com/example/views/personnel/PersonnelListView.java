package com.example.views.personnel;

import com.example.base.ui.ViewTitle;
import com.example.entity.Personnel;
import com.example.service.PersonnelService;
import com.example.views.components.ConfirmDeleteDialog;
import com.example.views.components.PersonnelForm;
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
@Menu(order = 3, icon = "icons/users.svg", title = "Données de base/Personnel")
public class PersonnelListView extends VerticalLayout {

    private final PersonnelService personnelService;

    final TextField searchField;
    final Grid<Personnel> personnelGrid;

    public PersonnelListView(PersonnelService personnelService) {
        this.personnelService = personnelService;

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par matricule, nom, prénom ou spécialité");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("30em");
        searchField.addValueChangeListener(_ -> refreshGrid());

        Button createBtn = new Button("Ajouter un personnel", _ -> openCreateDialog());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.addClassName("primary-action");

        var titleLine = new HorizontalLayout(new ViewTitle("Gestion du personnel"));
        titleLine.setWidthFull();

        var searchLine = new HorizontalLayout(searchField, createBtn);
        searchLine.setWidthFull();
        searchLine.setWrap(true);
        searchLine.setFlexGrow(1, searchField);

        var toolbar = new VerticalLayout(titleLine, searchLine);
        toolbar.addClassName("page-toolbar");
        toolbar.setWidthFull();

        personnelGrid = new Grid<>();
        refreshGrid();
        searchField.addValueChangeListener(_ -> refreshGrid());

        personnelGrid.addColumn(Personnel::getMatricule).setHeader("Matricule").setSortable(true);
        personnelGrid.addColumn(Personnel::getNomPersonnel).setHeader("Nom").setSortable(true);
        personnelGrid.addColumn(Personnel::getPrenomPersonnel).setHeader("Prénom").setSortable(true);
        personnelGrid.addColumn(Personnel::getSpecialite).setHeader("Spécialité").setSortable(true);

        personnelGrid.addComponentColumn(personnel -> {
            Button modifierBtn = new Button("Modifier");
            modifierBtn.addClickListener(_ -> openEditDialog(personnel));

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

            deleteBtn.addClickListener(_ -> {
                ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(
                        "Voulez-vous vraiment supprimer : "
                                + personnel.getNomPersonnel() + " " + personnel.getPrenomPersonnel() + " ?",
                        () -> {
                            personnelService.deletePersonnel(personnel);
                            refreshGrid();

                            Notification.show("Personnel supprimé", 3000, Notification.Position.BOTTOM_END)
                                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        }
                );

                dialog.open();
            });

            return new HorizontalLayout(modifierBtn, deleteBtn);
        }).setHeader("Actions");

        personnelGrid.setEmptyStateText("Aucun personnel enregistré");
        personnelGrid.setSizeFull();
        personnelGrid.addClassName("professional-grid");

        setSizeFull();
        addClassName("page-container");
        add(toolbar, personnelGrid);
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Ajouter un personnel");
        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");

        PersonnelForm form = new PersonnelForm(
                null,
                "Ajouter le personnel",
                data -> {
                    try {
                        personnelService.createPersonnel(
                                data.matricule(),
                                data.nomPersonnel(),
                                data.prenomPersonnel(),
                                data.specialite()
                        );

                        Notification.show("Personnel ajouté", 3000, Notification.Position.BOTTOM_END)
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

    private void openEditDialog(Personnel personnel) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Modifier le personnel");
        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");

        PersonnelForm form = new PersonnelForm(
                personnel,
                "Modifier le personnel",
                data -> {
                    try {
                        personnelService.updatePersonnel(
                                personnel,
                                data.matricule(),
                                data.nomPersonnel(),
                                data.prenomPersonnel(),
                                data.specialite()
                        );

                        Notification.show("Personnel modifié", 3000, Notification.Position.BOTTOM_END)
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