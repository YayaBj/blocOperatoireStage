package com.example.views.components;

import com.example.entity.Personnel;
import com.example.entity.enums.RoleIntervention;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RolePersonnelDialog extends Dialog {

    public RolePersonnelDialog(Collection<Personnel> personnels,
                               Consumer<Map<Long, RoleIntervention>> onConfirm) {

        setHeaderTitle("Affectation des rôles");
        setWidth("850px");
        setMaxWidth("95vw");

        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("intervention-form");
        layout.setWidthFull();

        Map<Personnel, ComboBox<RoleIntervention>> roleMap = new HashMap<>();

        Div section = new Div();
        section.addClassName("form-section");

        Span title = new Span("Rôles du personnel");
        title.addClassName("form-section-title");

        Span description = new Span("Attribuer un rôle à chaque membre du personnel sélectionné pour l’intervention.");
        description.addClassName("form-section-description");

        section.add(title, description);

        for (Personnel personnel : personnels) {
            ComboBox<RoleIntervention> roleComboBox = new ComboBox<>(
                    personnel.getNomPersonnel() + " " + personnel.getPrenomPersonnel()
            );

            roleComboBox.setItems(RoleIntervention.values());
            roleComboBox.setPlaceholder("Choisir un rôle");
            roleComboBox.setWidthFull();

            roleMap.put(personnel, roleComboBox);

            HorizontalLayout row = new HorizontalLayout(roleComboBox);
            row.addClassName("form-row");
            row.setWidthFull();

            section.add(row);
        }

        Button confirmBtn = new Button("Confirmer les rôles", event -> {
            Map<Long, RoleIntervention> personnelsAvecRoles = new HashMap<>();

            for (Map.Entry<Personnel, ComboBox<RoleIntervention>> entry : roleMap.entrySet()) {
                RoleIntervention role = entry.getValue().getValue();

                if (role == null) {
                    entry.getValue().setInvalid(true);
                    entry.getValue().setErrorMessage("Le rôle est obligatoire");
                    return;
                }

                personnelsAvecRoles.put(entry.getKey().getId(), role);
            }

            onConfirm.accept(personnelsAvecRoles);
            close();
        });

        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmBtn.addClassName("primary-action");

        Button cancelBtn = new Button("Annuler", _ -> close());

        HorizontalLayout actions = new HorizontalLayout(confirmBtn, cancelBtn);
        actions.addClassName("form-actions");
        actions.setWidthFull();

        layout.add(section, actions);
        add(layout);
    }
}