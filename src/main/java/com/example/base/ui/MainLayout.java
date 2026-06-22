package com.example.base.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuEntry;

@Layout
public final class MainLayout extends AppLayout {

    MainLayout() {
        setPrimarySection(Section.DRAWER);
        addToDrawer(createApplicationHeader(), createApplicationDrawer());
    }

    private Component createApplicationHeader() {
        // TODO Replace with real application logo and name
        var appLogo = new Avatar("My Application");
        appLogo.addClassName("app-logo");
        appLogo.addThemeVariants(AvatarVariant.AURA_FILLED, AvatarVariant.XSMALL);

        var appName = new Span("My Application");
        appName.addClassName("app-name");

        var header = new HorizontalLayout(appLogo, appName);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(true);
        return header;
    }

    private Component createApplicationDrawer() {
        var scroller = new Scroller(createSideNav());
        scroller.addThemeVariants(ScrollerVariant.OVERFLOW_INDICATORS);
        return scroller;
    }

    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.setMinWidth(220, Unit.PIXELS);

        nav.addItem(new SideNavItem("Planning", "", new SvgIcon("icons/calendar.svg")));

        SideNavItem donneesBase = new SideNavItem("Données de base");
        donneesBase.addItem(new SideNavItem("Patients", "patients", new SvgIcon("icons/user.svg")));
        donneesBase.addItem(new SideNavItem("Personnel", "personnels", new SvgIcon("icons/users.svg")));
        donneesBase.addItem(new SideNavItem("Salles", "salles", new SvgIcon("icons/building.svg")));
        donneesBase.addItem(new SideNavItem("Matériel", "materiels", new SvgIcon("icons/package.svg")));

        SideNavItem bloc = new SideNavItem("Bloc opératoire");
        bloc.addItem(new SideNavItem("Interventions", "interventions"));
        bloc.addItem(new SideNavItem("Boîtes chirurgicales", "boites", new SvgIcon("icons/box.svg")));

        SideNavItem sterilisation = new SideNavItem("Stérilisation");
        sterilisation.addItem(new SideNavItem("Demandes", "demandes-sterilisation", new SvgIcon("icons/clipboard-check.svg")));
        sterilisation.addItem(new SideNavItem("Machines", "machines", new SvgIcon("icons/settings.svg")));
        sterilisation.addItem(new SideNavItem("Processus", "processus-sterilisation", new SvgIcon("icons/refresh.svg")));
        sterilisation.addItem(new SideNavItem("Mouvements", "mouvements-boites", new SvgIcon("icons/arrows-right-left.svg")));

        nav.addItem(donneesBase, bloc, sterilisation);

        return nav;
    }
}
