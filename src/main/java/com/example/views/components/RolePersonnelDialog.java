package com.example.views.components;

import com.example.entity.Personnel;
import com.example.entity.enums.RoleIntervention;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RolePersonnelDialog extends Dialog {

    public RolePersonnelDialog(Collection<Personnel> personnels,
                               Consumer<Map<Long, RoleIntervention>> onConfirm) {

        setHeaderTitle("Rôles du personnel");

        VerticalLayout layout = new VerticalLayout();
        Map<Personnel, ComboBox<RoleIntervention>> roleMap = new HashMap<>();

        for (Personnel personnel : personnels) {
            ComboBox<RoleIntervention> roleComboBox = new ComboBox<>();
            roleComboBox.setLabel(personnel.getNomPersonnel() + " " + personnel.getPrenomPersonnel());
            roleComboBox.setItems(RoleIntervention.values());
            roleComboBox.setPlaceholder("Choisir un rôle");

            roleMap.put(personnel, roleComboBox);
            layout.add(roleComboBox);
        }

        Button confirmBtn = new Button("Confirmer", event -> {
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

        Button cancelBtn = new Button("Annuler", event -> close());

        layout.add(new HorizontalLayout(confirmBtn, cancelBtn));
        layout.setWidth("500px");

        add(layout);
    }
}