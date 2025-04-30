package com.geovannycode.bookstore.webapp.infrastructure.views;

import com.geovannycode.bookstore.webapp.domain.model.Address;
import com.geovannycode.bookstore.webapp.domain.model.Customer;
import com.geovannycode.bookstore.webapp.domain.model.OrderDTO;
import com.geovannycode.bookstore.webapp.domain.model.OrderItem;
import com.geovannycode.bookstore.webapp.domain.model.OrderStatus;
import com.geovannycode.bookstore.webapp.infrastructure.api.controller.OrderController;
import com.geovannycode.bookstore.webapp.infrastructure.views.components.CardComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClientException;

@Route(value = "order", layout = MainLayout.class)
@PageTitle("Detalle de Orden")
public class OrderConfirmationView extends VerticalLayout implements HasUrlParameter<String> {

    private static final Logger log = LoggerFactory.getLogger(OrderConfirmationView.class);

    private final OrderController orderController;

    private final Grid<OrderItem> orderItemsGrid = new Grid<>(OrderItem.class, false);
    private final Span totalPriceLabel = new Span("Total: $0.00");
    private final Span orderNumberLabel = new Span();
    private final Span orderStatusLabel = new Span();

    private final VerticalLayout customerInfoLayout = new VerticalLayout();
    private final VerticalLayout deliveryAddressLayout = new VerticalLayout();

    private OrderDTO currentOrder;

    public OrderConfirmationView(@Autowired OrderController orderController) {
        this.orderController = orderController;

        setSizeFull();
        setPadding(true);

        // Reducir el espaciado vertical general
        setSpacing(false);

        H2 viewTitle = new H2("Detalle de Orden");
        viewTitle.getStyle().set("margin-top", "0").set("margin-bottom", "1rem");

        configureOrderItemsGrid();

        // Order header section with order number and status
        HorizontalLayout orderHeaderLayout = createOrderHeaderSection();

        // Order items section with grid and total
        VerticalLayout orderItemsSection = new VerticalLayout();
        orderItemsSection.add(orderItemsGrid, createTotalSection());
        orderItemsSection.setPadding(false);
        orderItemsSection.setSpacing(true);
        orderItemsSection.setMargin(false);

        // Main order details card
        CardComponent orderDetailsCard = new CardComponent();
        VerticalLayout orderDetailsContent =
                new VerticalLayout(viewTitle, orderHeaderLayout, new Hr(), orderItemsSection);
        orderDetailsContent.setPadding(false);
        orderDetailsContent.setSpacing(false);
        orderDetailsContent.setMargin(false);
        orderDetailsCard.add(orderDetailsContent);

        // Customer info and delivery address sections
        configureCustomerInfoSection();
        configureDeliveryAddressSection();

        CardComponent customerInfoCard = new CardComponent();
        customerInfoCard.getStyle().set("margin-top", "1rem");
        customerInfoCard.add(customerInfoLayout);

        CardComponent deliveryAddressCard = new CardComponent();
        deliveryAddressCard.getStyle().set("margin-top", "1rem");
        deliveryAddressCard.add(deliveryAddressLayout);

        // Action buttons
        HorizontalLayout actionsLayout = createActionsSection();

        add(orderDetailsCard, customerInfoCard, deliveryAddressCard, actionsLayout);

        // Ajustar el espaciado entre componentes
        setSpacing(false);
        getStyle().set("gap", "1rem");
    }

    private HorizontalLayout createOrderHeaderSection() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Order number section
        HorizontalLayout orderNumberSection = new HorizontalLayout();
        Span orderNumberTitle = new Span("Número de Orden: ");
        orderNumberTitle.getStyle().set("font-weight", "bold");
        orderNumberLabel.getStyle().set("margin-left", "8px");
        orderNumberSection.add(orderNumberTitle, orderNumberLabel);

        // Order status section
        HorizontalLayout orderStatusSection = new HorizontalLayout();
        Span orderStatusTitle = new Span("Estado: ");
        orderStatusTitle.getStyle().set("font-weight", "bold");
        orderStatusLabel.getStyle().set("margin-left", "8px");
        orderStatusSection.add(orderStatusTitle, orderStatusLabel);

        layout.add(orderNumberSection, orderStatusSection);
        return layout;
    }

    private void configureOrderItemsGrid() {
        orderItemsGrid.addColumn(OrderItem::code).setHeader("Código").setAutoWidth(true);
        orderItemsGrid
                .addColumn(OrderItem::name)
                .setHeader("Producto")
                .setAutoWidth(true)
                .setFlexGrow(1);
        orderItemsGrid
                .addColumn(item -> formatCurrency(item.price()))
                .setHeader("Precio")
                .setAutoWidth(true);
        orderItemsGrid.addColumn(OrderItem::quantity).setHeader("Cantidad").setAutoWidth(true);
        orderItemsGrid
                .addColumn(item -> formatCurrency(item.price().multiply(BigDecimal.valueOf(item.quantity()))))
                .setHeader("Subtotal")
                .setAutoWidth(true);

        orderItemsGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        // Configuración dinámica - inicialmente sin altura fija
        orderItemsGrid.setWidthFull();
    }

    /**
     * Actualiza la altura del grid según la cantidad de elementos.
     * Para 5 elementos o menos, el grid mostrará todos los elementos sin desplazamiento.
     * Para más de 5 elementos, el grid mantendrá una altura fija que muestra aproximadamente 5 elementos
     * y habilitará el desplazamiento para el resto.
     */
    private void updateGridHeight() {
        if (currentOrder != null && currentOrder.items() != null) {
            int itemCount = currentOrder.items().size();

            // Hacer el grid visible
            orderItemsGrid.setVisible(true);

            if (itemCount <= 5) {
                // Para 5 elementos o menos, ajustar altura para mostrar todos sin scroll
                // Aproximadamente 53px por fila (incluyendo cabecera) basado en el estilo por defecto de Vaadin
                int gridHeight = (itemCount + 1) * 53; // +1 para la fila de cabecera
                orderItemsGrid.setHeight(gridHeight + "px");
                orderItemsGrid.getStyle().set("overflow", "hidden");

                log.info("Orden con {} elementos, altura ajustada a {}px sin scroll", itemCount, gridHeight);
            } else {
                // Para más de 5 elementos, fijar la altura para mostrar aproximadamente 5 elementos
                // y habilitar el desplazamiento para el resto
                int gridHeight = 6 * 53; // 5 elementos + 1 cabecera
                orderItemsGrid.setHeight(gridHeight + "px");
                orderItemsGrid.getStyle().set("overflow", "auto");

                log.info("Orden con {} elementos, altura fijada a {}px con scroll habilitado", itemCount, gridHeight);
            }
        } else {
            // Si no hay orden, configuramos una altura mínima
            orderItemsGrid.setHeight("100px");
        }
    }

    private HorizontalLayout createTotalSection() {
        totalPriceLabel
                .getStyle()
                .set("font-weight", "bold")
                .set("font-size", "1.2em")
                .set("margin-left", "auto");

        HorizontalLayout layout = new HorizontalLayout(totalPriceLabel);
        layout.setWidthFull();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return layout;
    }

    private void configureCustomerInfoSection() {
        H3 sectionTitle = new H3("Información del Cliente");
        sectionTitle.getStyle().set("margin-top", "0").set("margin-bottom", "0.5rem");

        Div customerInfo = new Div();
        customerInfo.setWidthFull();

        customerInfoLayout.add(sectionTitle, customerInfo);
        customerInfoLayout.setPadding(true);
        customerInfoLayout.setSpacing(false);
        customerInfoLayout.setMargin(false);
    }

    private void configureDeliveryAddressSection() {
        H3 sectionTitle = new H3("Dirección de Entrega");
        sectionTitle.getStyle().set("margin-top", "0").set("margin-bottom", "0.5rem");

        Div addressInfo = new Div();
        addressInfo.setWidthFull();

        deliveryAddressLayout.add(sectionTitle, addressInfo);
        deliveryAddressLayout.setPadding(true);
        deliveryAddressLayout.setSpacing(false);
        deliveryAddressLayout.setMargin(false);
    }

    private HorizontalLayout createActionsSection() {
        Button backButton = new Button("Volver a mis órdenes");
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate(OrderListView.class));

        Button homeButton = new Button("Volver a la Tienda");
        homeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        homeButton.addClickListener(e -> UI.getCurrent().navigate(ProductGridView.class));

        HorizontalLayout actionsLayout = new HorizontalLayout(backButton, homeButton);
        actionsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        actionsLayout.setWidthFull();
        actionsLayout.setMargin(true);
        actionsLayout.getStyle().set("margin-top", "1rem");

        return actionsLayout;
    }

    @Override
    public void setParameter(BeforeEvent event, String orderNumber) {
        try {
            log.info("Cargando orden con número: {}", orderNumber);
            loadOrder(orderNumber);
        } catch (Exception e) {
            log.error("Error cargando la orden", e);
            Notification.show("No se pudo cargar la orden: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void loadOrder(String orderNumber) {
        try {
            currentOrder = orderController.getOrder(orderNumber);

            if (currentOrder != null) {
                log.info("Orden cargada con éxito: {}", orderNumber);

                // Update order header info
                orderNumberLabel.setText(currentOrder.orderNumber());
                orderStatusLabel.setText(currentOrder.status().toString());

                // Configurar el color según el estado
                setStatusColor(currentOrder.status());

                // Update order items grid
                orderItemsGrid.setItems(currentOrder.items());

                // Actualizar la altura del grid según la cantidad de elementos
                updateGridHeight();

                // Update total
                totalPriceLabel.setText("Total: " + formatCurrency(currentOrder.getTotalAmount()));

                // Update customer info
                updateCustomerInfo(currentOrder.customer());

                // Update delivery address
                updateDeliveryAddress(currentOrder.deliveryAddress());
            } else {
                Notification.show("Orden no encontrada: " + orderNumber, 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (RestClientException e) {
            log.error("Error cargando la orden", e);
            Notification.show("Error al cargar la orden: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void setStatusColor(OrderStatus status) {
        if (status == OrderStatus.IN_PROCESS) {
            orderStatusLabel.getStyle().set("color", "var(--lumo-primary-color)");
        } else if (status == OrderStatus.DELIVERED) {
            orderStatusLabel.getStyle().set("color", "var(--lumo-success-color)");
        } else if (status == OrderStatus.CANCELLED || status == OrderStatus.ERROR) {
            orderStatusLabel.getStyle().set("color", "var(--lumo-error-color)");
        } else if (status == OrderStatus.NEW) {
            orderStatusLabel.getStyle().set("color", "var(--lumo-primary-color)");
        }
    }

    private void updateCustomerInfo(Customer customer) {
        if (customer != null) {
            VerticalLayout infoLayout = new VerticalLayout();
            infoLayout.setPadding(false);
            infoLayout.setSpacing(false);
            infoLayout.setMargin(false);

            // Nombre del cliente
            HorizontalLayout nameLayout = new HorizontalLayout();
            Span nameLabel = new Span("Nombre: ");
            nameLabel.getStyle().set("font-weight", "bold");
            Span nameValue = new Span(customer.name());
            nameLayout.add(nameLabel, nameValue);
            nameLayout.setWidthFull();
            nameLayout.setSpacing(false);
            nameLayout.setPadding(false);
            nameLayout.setMargin(false);

            // Email
            HorizontalLayout emailLayout = new HorizontalLayout();
            Span emailLabel = new Span("Email: ");
            emailLabel.getStyle().set("font-weight", "bold");
            Span emailValue = new Span(customer.email());
            emailLayout.add(emailLabel, emailValue);
            emailLayout.setWidthFull();
            emailLayout.setSpacing(false);
            emailLayout.setPadding(false);
            emailLayout.setMargin(false);

            // Teléfono
            HorizontalLayout phoneLayout = new HorizontalLayout();
            Span phoneLabel = new Span("Teléfono: ");
            phoneLabel.getStyle().set("font-weight", "bold");
            Span phoneValue = new Span(customer.phone());
            phoneLayout.add(phoneLabel, phoneValue);
            phoneLayout.setWidthFull();
            phoneLayout.setSpacing(false);
            phoneLayout.setPadding(false);
            phoneLayout.setMargin(false);

            infoLayout.add(nameLayout, emailLayout, phoneLayout);

            // Reemplazar el contenido anterior
            if (customerInfoLayout.getComponentCount() > 1) {
                customerInfoLayout.replace(customerInfoLayout.getComponentAt(1), infoLayout);
            }
        }
    }

    private void updateDeliveryAddress(Address address) {
        if (address != null) {
            VerticalLayout addressLayout = new VerticalLayout();
            addressLayout.setPadding(false);
            addressLayout.setSpacing(false);
            addressLayout.setMargin(false);

            if (address.addressLine1() != null && !address.addressLine1().isEmpty()) {
                HorizontalLayout line1Layout = new HorizontalLayout();
                Span line1Label = new Span("Dirección: ");
                line1Label.getStyle().set("font-weight", "bold");
                Span line1Value = new Span(address.addressLine1());
                line1Layout.add(line1Label, line1Value);
                line1Layout.setWidthFull();
                line1Layout.setSpacing(false);
                line1Layout.setPadding(false);
                line1Layout.setMargin(false);
                addressLayout.add(line1Layout);
            }

            if (address.addressLine2() != null && !address.addressLine2().isEmpty()) {
                HorizontalLayout line2Layout = new HorizontalLayout();
                Span line2Label = new Span("Complemento: ");
                line2Label.getStyle().set("font-weight", "bold");
                Span line2Value = new Span(address.addressLine2());
                line2Layout.add(line2Label, line2Value);
                line2Layout.setWidthFull();
                line2Layout.setSpacing(false);
                line2Layout.setPadding(false);
                line2Layout.setMargin(false);
                addressLayout.add(line2Layout);
            }

            HorizontalLayout cityStateLayout = new HorizontalLayout();
            cityStateLayout.setWidthFull();
            cityStateLayout.setSpacing(false);
            cityStateLayout.setPadding(false);
            cityStateLayout.setMargin(false);

            if (address.city() != null && !address.city().isEmpty()) {
                Span cityLabel = new Span("Ciudad: ");
                cityLabel.getStyle().set("font-weight", "bold");
                Span cityValue = new Span(address.city());
                cityStateLayout.add(cityLabel, cityValue);
            }

            if (address.state() != null && !address.state().isEmpty()) {
                Span stateLabel = new Span(" / Estado: ");
                stateLabel.getStyle().set("font-weight", "bold");
                Span stateValue = new Span(address.state());
                cityStateLayout.add(stateLabel, stateValue);
            }

            addressLayout.add(cityStateLayout);

            HorizontalLayout zipCountryLayout = new HorizontalLayout();
            zipCountryLayout.setWidthFull();
            zipCountryLayout.setSpacing(false);
            zipCountryLayout.setPadding(false);
            zipCountryLayout.setMargin(false);

            if (address.zipCode() != null && !address.zipCode().isEmpty()) {
                Span zipLabel = new Span("Código Postal: ");
                zipLabel.getStyle().set("font-weight", "bold");
                Span zipValue = new Span(address.zipCode());
                zipCountryLayout.add(zipLabel, zipValue);
            }

            if (address.country() != null && !address.country().isEmpty()) {
                Span countryLabel = new Span(" / País: ");
                countryLabel.getStyle().set("font-weight", "bold");
                Span countryValue = new Span(address.country());
                zipCountryLayout.add(countryLabel, countryValue);
            }

            addressLayout.add(zipCountryLayout);

            // Reemplazar el contenido anterior
            if (deliveryAddressLayout.getComponentCount() > 1) {
                deliveryAddressLayout.replace(deliveryAddressLayout.getComponentAt(1), addressLayout);
            }
        }
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyFormat.format(amount);
    }
}
