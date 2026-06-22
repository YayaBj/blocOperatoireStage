package com.example.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ConfirmDeleteDialog extends Dialog {

    public ConfirmDeleteDialog(String message, Runnable onConfirm) {
        setHeaderTitle("Confirmation de suppression");

        Button confirmBtn = new Button("Oui, supprimer", event -> {
            onConfirm.run();
            close();
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Annuler", event -> close());

        VerticalLayout layout = new VerticalLayout(
                new Span(message),
                new HorizontalLayout(confirmBtn, cancelBtn)
        );

        add(layout);
    }
}