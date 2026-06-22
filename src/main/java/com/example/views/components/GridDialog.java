package com.example.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class GridDialog<T> extends Dialog {

    public GridDialog(String title, Grid<T> grid) {
        setHeaderTitle(title);
        setWidth("900px");
        setMaxWidth("95vw");
        setHeight("650px");

        grid.setSizeFull();
        grid.addClassName("professional-grid");

        Button closeBtn = new Button("Fermer", event -> close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        closeBtn.addClassName("primary-action");

        HorizontalLayout actions = new HorizontalLayout(closeBtn);
        actions.addClassName("form-actions");
        actions.setWidthFull();

        VerticalLayout layout = new VerticalLayout(grid, actions);
        layout.addClassName("intervention-form");
        layout.setSizeFull();

        add(layout);
    }
}