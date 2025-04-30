package com.geovannycode.bookstore.webapp.infrastructure.views;

import com.geovannycode.bookstore.webapp.domain.model.OrderSummary;
import com.geovannycode.bookstore.webapp.infrastructure.api.controller.OrderController;
import com.geovannycode.bookstore.webapp.infrastructure.views.components.CardComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "orders", layout = MainLayout.class)
@PageTitle("Mis Órdenes")
public class OrderListView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(OrderListView.class);

    private final OrderController orderController;
    private final Grid<OrderSummary> orderGrid = new Grid<>(OrderSummary.class, false);

    public OrderListView(@Autowired OrderController orderController) {
        this.orderController = orderController;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 viewTitle = new H2("Mis Órdenes");
        viewTitle.getStyle().set("margin-top", "0");

        configureOrderGrid();

        Button refreshButton = new Button("Actualizar");
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.setIcon(new Icon(VaadinIcon.REFRESH));
        refreshButton.addClickListener(e -> loadOrders());

        Button shopButton = new Button("Continuar Comprando");
        shopButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        shopButton.addClickListener(e -> UI.getCurrent().navigate(ProductGridView.class));

        HorizontalLayout actions = new HorizontalLayout(refreshButton, shopButton);
        actions.setSpacing(true);

        VerticalLayout mainContent = new VerticalLayout();
        mainContent.add(viewTitle, orderGrid, actions);
        mainContent.setPadding(false);
        mainContent.setSpacing(true);

        CardComponent card = new CardComponent();
        card.add(mainContent);

        add(card);

        // Cargar órdenes al iniciar
        loadOrders();
    }

    private void configureOrderGrid() {
        // Configurar columnas
        orderGrid
                .addColumn(OrderSummary::orderNumber)
                .setHeader("Número de Orden")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Columna de estado con color
        orderGrid
                .addColumn(new ComponentRenderer<>(item -> {
                    Span statusLabel = new Span(item.status().toString());
                    statusLabel.getElement().getThemeList().clear();

                    switch (item.status()) {
                        case NEW:
                            statusLabel.getElement().getThemeList().add("badge primary");
                            break;
                        case IN_PROCESS:
                            statusLabel.getElement().getThemeList().add("badge contrast");
                            break;
                        case DELIVERED:
                            statusLabel.getElement().getThemeList().add("badge success");
                            break;
                        case CANCELLED:
                        case ERROR:
                            statusLabel.getElement().getThemeList().add("badge error");
                            break;
                        default:
                            statusLabel.getElement().getThemeList().add("badge");
                    }

                    return statusLabel;
                }))
                .setHeader("Estado")
                .setAutoWidth(true);

        // Columna de acciones
        orderGrid
                .addColumn(new ComponentRenderer<>(item -> {
                    Button viewButton = new Button("Ver Detalles");
                    viewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                    viewButton.addClickListener(e -> navigateToOrderDetails(item.orderNumber()));
                    return viewButton;
                }))
                .setHeader("Acciones")
                .setAutoWidth(true);

        // Configuración adicional del grid
        orderGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        orderGrid.setHeight("400px");
        orderGrid.setWidthFull();

        // Doble clic en una fila para ver detalles
        orderGrid.addItemDoubleClickListener(
                event -> navigateToOrderDetails(event.getItem().orderNumber()));
    }

    private void loadOrders() {
        try {
            // Obtener las órdenes del controlador
            List<OrderSummary> orders = orderController.getOrders();

            if (orders != null && !orders.isEmpty()) {
                orderGrid.setItems(orders);
                log.info("Se cargaron {} órdenes", orders.size());
            } else {
                orderGrid.setItems();
                Notification.show("No se encontraron órdenes", 3000, Notification.Position.MIDDLE);
                log.info("No se encontraron órdenes");
            }
        } catch (Exception e) {
            log.error("Error al cargar las órdenes", e);
            Notification.show("Error al cargar las órdenes: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            orderGrid.setItems();
        }
    }

    private void navigateToOrderDetails(String orderNumber) {
        if (orderNumber != null && !orderNumber.isEmpty()) {
            UI.getCurrent().navigate("order/" + orderNumber);
        }
    }
}
