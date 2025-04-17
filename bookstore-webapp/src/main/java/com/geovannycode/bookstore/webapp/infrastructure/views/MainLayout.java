package com.geovannycode.bookstore.webapp.infrastructure.views;

import com.geovannycode.bookstore.webapp.infrastructure.views.components.CartBadge;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {

    private final CartBadge cartBadge;

    public MainLayout() {
        cartBadge = new CartBadge();
        cartBadge.updateCount(2);
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("BookStore");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");

        HorizontalLayout headerLayout = new HorizontalLayout(toggle, title, cartBadge);
        headerLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        headerLayout.setWidthFull();
        headerLayout.expand(title);
        headerLayout.setPadding(true);
        headerLayout.setSpacing(true);

        headerLayout.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BoxShadow.SMALL);

        addToNavbar(headerLayout);
    }

    private void createDrawer() {
        SideNav nav = getSideNav();
        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);
        addToDrawer(scroller);
    }

    private SideNav getSideNav() {
        SideNav nav = new SideNav();

        nav.addItem(new SideNavItem("Productos", ProductGridView.class));
        nav.addItem(new SideNavItem("Carrito", "cart"));

        return nav;
    }

}
