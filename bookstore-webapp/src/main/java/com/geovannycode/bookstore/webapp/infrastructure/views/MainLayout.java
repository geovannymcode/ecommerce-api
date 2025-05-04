package com.geovannycode.bookstore.webapp.infrastructure.views;

import com.geovannycode.bookstore.webapp.domain.model.Cart;
import com.geovannycode.bookstore.webapp.infrastructure.api.controller.CartController;
import com.geovannycode.bookstore.webapp.infrastructure.views.components.CartBadge;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

public class MainLayout extends AppLayout {

    private static final Logger log = LoggerFactory.getLogger(MainLayout.class);
    private final CartBadge cartBadge;
    private final CartController cartController;

    public MainLayout(@Autowired CartController cartController) {
        this.cartController = cartController;

        // Inicializamos el badge con 0
        cartBadge = new CartBadge();
        cartBadge.updateCount(0);

        createHeader();
        createDrawer();

        // Hacer que el badge sea clickeable para ir al carrito
        cartBadge.getElement().addEventListener("click", e -> {
            UI.getCurrent().navigate(CartView.class);
        });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Actualizar el contador del carrito al cargar
        updateCartBadge();
    }

    private void updateCartBadge() {
        try {
            String cartId = getCartIdFromSession();
            if (cartId != null) {
                // Intentar obtener el carrito
                ResponseEntity<Cart> response = cartController.getCart(cartId);
                if (response != null && response.getBody() != null) {
                    Cart cart = response.getBody();
                    // Actualizar el badge con la cantidad de elementos
                    int itemCount = cart.getItems() != null ? cart.getItems().size() : 0;
                    cartBadge.updateCount(itemCount); // Usar cartBadge, no cartLink
                } else {
                    // Si no hay carrito o está vacío
                    cartBadge.updateCount(0);

                    UI.getCurrent().getSession().setAttribute("cartId", null);
                }
            } else {
                // Si no hay ID de carrito en la sesión
                cartBadge.updateCount(0);
            }
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            // Manejar el caso específico de "Cart Not Found"
            log.debug("El carrito no existe o ha sido eliminado: {}", e.getMessage());

            // Eliminar el ID del carrito de la sesión
            UI.getCurrent().getSession().setAttribute("cartId", null);

            // Restablecer el badge
            cartBadge.updateCount(0);
        } catch (Exception e) {
            // Manejar otros errores
            log.error("Error al actualizar el badge del carrito", e);
            cartBadge.updateCount(0);
        }
    }

    private String getCartIdFromSession() {
        Object cartIdObj = UI.getCurrent().getSession().getAttribute("cartId");
        return cartIdObj != null ? cartIdObj.toString() : null;
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

        headerLayout.addClassNames(LumoUtility.Background.BASE, LumoUtility.BoxShadow.SMALL);

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

        nav.addItem(new SideNavItem("Products", ProductGridView.class, VaadinIcon.PACKAGE.create()));
        nav.addItem(new SideNavItem("Cart", CartView.class, VaadinIcon.CART.create()));
        nav.addItem(new SideNavItem("My Orders", OrderListView.class, VaadinIcon.CLIPBOARD_TEXT.create()));
        return nav;
    }

    public CartBadge getCartBadge() {
        return cartBadge;
    }
}
