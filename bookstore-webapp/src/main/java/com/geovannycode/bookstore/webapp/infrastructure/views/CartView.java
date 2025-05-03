package com.geovannycode.bookstore.webapp.infrastructure.views;

import com.geovannycode.bookstore.webapp.domain.model.Address;
import com.geovannycode.bookstore.webapp.domain.model.Cart;
import com.geovannycode.bookstore.webapp.domain.model.CartItem;
import com.geovannycode.bookstore.webapp.domain.model.CartItemRequestDTO;
import com.geovannycode.bookstore.webapp.domain.model.CreateOrderRequest;
import com.geovannycode.bookstore.webapp.domain.model.Customer;
import com.geovannycode.bookstore.webapp.domain.model.OrderConfirmationDTO;
import com.geovannycode.bookstore.webapp.domain.model.OrderItem;
import com.geovannycode.bookstore.webapp.domain.model.PaymentRequest;
import com.geovannycode.bookstore.webapp.domain.model.PaymentResponse;
import com.geovannycode.bookstore.webapp.domain.model.enums.PaymentStatus;
import com.geovannycode.bookstore.webapp.infrastructure.api.controller.CartController;
import com.geovannycode.bookstore.webapp.infrastructure.api.controller.OrderController;
import com.geovannycode.bookstore.webapp.infrastructure.api.controller.PaymentController;
import com.geovannycode.bookstore.webapp.infrastructure.views.components.CardComponent;
import com.geovannycode.bookstore.webapp.infrastructure.views.components.CartBadge;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Route(value = "cart", layout = MainLayout.class)
@PageTitle("Shopping Cart")
public class CartView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(CartView.class);

    private final CartController cartController;
    private final OrderController orderController;
    private final PaymentController paymentController; // Añadido el controlador de pagos

    private String cartId;
    private Cart cart;

    private final Grid<CartItem> cartGrid = new Grid<>(CartItem.class, false);
    private final Span totalPriceLabel = new Span("Total: $0.00");

    // Modelos para formularios
    private final Customer customer = new Customer("", "", "");
    private final Address deliveryAddress = new Address("", "", "", "", "", "");
    private final PaymentRequest paymentRequest = new PaymentRequest();

    // Binders para formularios
    private final Binder<Customer> customerBinder = new Binder<>(Customer.class);
    private final Binder<Address> addressBinder = new Binder<>(Address.class);
    private final Binder<PaymentRequest> paymentBinder = new Binder<>(PaymentRequest.class);

    private final Button placeOrderButton = new Button("Place Order");

    public CartView(
            @Autowired CartController cartController,
            @Autowired OrderController orderController,
            @Autowired PaymentController paymentController) {
        this.cartController = cartController;
        this.orderController = orderController;
        this.paymentController = paymentController;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 viewTitle = new H2("Shopping Cart");
        viewTitle.getStyle().set("margin-top", "0");

        configureCartGrid();

        VerticalLayout cartSection = new VerticalLayout(viewTitle, cartGrid, createTotalSection());
        cartSection.setPadding(false);
        cartSection.setSpacing(true);

        VerticalLayout formSection = new VerticalLayout(
                createCustomerSection(),
                createDeliveryAddressSection(),
                createPaymentSection(),
                createOrderButtonSection());
        formSection.setPadding(false);
        formSection.setSpacing(true);

        formSection.setWidth("100%");
        formSection.setMaxWidth("1000px");
        formSection.getStyle().set("margin-inline", "auto");

        // add(cartSection, new Hr(),formSection);
        CardComponent cartCard = new CardComponent();
        cartCard.add(cartSection);

        CardComponent formCard = new CardComponent();
        formCard.add(formSection);

        add(cartCard, new Hr(), formCard);
        // Load cart data
        loadCart();
    }

    private void configureCartGrid() {
        cartGrid.addColumn(CartItem::getCode).setHeader("Product Code").setAutoWidth(true);
        cartGrid.addColumn(CartItem::getName)
                .setHeader("Product")
                .setAutoWidth(true)
                .setFlexGrow(1);
        cartGrid.addColumn(item -> formatCurrency(item.getPrice()))
                .setHeader("Price")
                .setAutoWidth(true);

        // Quantity column with editable field
        cartGrid.addColumn(new ComponentRenderer<>(item -> {
                    IntegerField quantityField = new IntegerField();
                    quantityField.setValue(item.getQuantity());
                    quantityField.setMin(1);
                    // Añadimos flechas para incrementar/decrementar dentro del campo
                    quantityField.setStepButtonsVisible(true);
                    quantityField.setWidth("100px");

                    quantityField.addValueChangeListener(e -> {
                        if (e.getValue() != null && e.getValue() > 0) {
                            updateItemQuantity(item.getCode(), e.getValue());
                        }
                    });

                    return quantityField;
                }))
                .setHeader("Quantity")
                .setAutoWidth(true);

        // Subtotal column
        cartGrid.addColumn(CartItem::getSubTotal)
                .setHeader("Subtotal")
                .setAutoWidth(true)
                .setRenderer(new ComponentRenderer<>(item -> new Span(formatCurrency(item.getSubTotal()))));

        // Actions column
        cartGrid.addColumn(new ComponentRenderer<>(item -> {
                    Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
                    removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                    removeButton.getElement().setAttribute("aria-label", "Remove item");

                    removeButton.addClickListener(e -> removeItem(item.getCode()));

                    return removeButton;
                }))
                .setHeader("Actions")
                .setAutoWidth(true);

        cartGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        cartGrid.setWidthFull();
        cartGrid.setColumnReorderingAllowed(true);

        // Configuramos la altura dinámica
        updateGridHeight();
    }

    /**
     * Actualiza la altura del grid del carrito según la cantidad de elementos.
     * Para 5 elementos o menos, el grid mostrará todos los elementos sin desplazamiento.
     * Para más de 5 elementos, el grid mantendrá una altura fija que muestra aproximadamente 5 elementos
     * y habilitará el desplazamiento para el resto.
     */
    private void updateGridHeight() {
        if (cart != null && cart.getItems() != null) {
            int itemCount = cart.getItems().size();

            // Hacer el grid visible
            cartGrid.setVisible(true);

            if (itemCount <= 5) {
                // Para 5 elementos o menos, ajustar altura para mostrar todos sin scroll
                // Aproximadamente 53px por fila (incluyendo cabecera) basado en el estilo por defecto de Vaadin
                int gridHeight = (itemCount + 1) * 53; // +1 para la fila de cabecera
                cartGrid.setHeight(gridHeight + "px");
                cartGrid.getStyle().set("overflow", "hidden");

                log.info("Carrito con {} elementos, altura ajustada a {}px sin scroll", itemCount, gridHeight);
            } else {
                // Para más de 5 elementos, fijar la altura para mostrar aproximadamente 5 elementos
                // y habilitar el desplazamiento para el resto
                int gridHeight = 6 * 53; // 5 elementos + 1 cabecera
                cartGrid.setHeight(gridHeight + "px");
                cartGrid.getStyle().set("overflow", "auto");

                log.info("Carrito con {} elementos, altura fijada a {}px con scroll habilitado", itemCount, gridHeight);
            }
        } else {
            // Si no hay carrito o está vacío, configuramos una altura mínima
            cartGrid.setHeight("100px");
        }
    }

    private HorizontalLayout createTotalSection() {
        Button continueShoppingButton = new Button("Continue Shopping");
        continueShoppingButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        continueShoppingButton.addClickListener(e -> UI.getCurrent().navigate(ProductGridView.class));

        Button clearCartButton = new Button("Clear Cart");
        clearCartButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        clearCartButton.addClickListener(e -> clearCart());

        totalPriceLabel.getStyle().set("font-weight", "bold").set("font-size", "1.2em");

        HorizontalLayout layout = new HorizontalLayout(continueShoppingButton, clearCartButton, totalPriceLabel);
        layout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        layout.setWidthFull();
        layout.setAlignItems(Alignment.CENTER);
        return layout;
    }

    private VerticalLayout createCustomerSection() {
        H3 sectionTitle = new H3("Customer Information");

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

        TextField nameField = new TextField("Full Name");
        EmailField emailField = new EmailField("Email");
        TextField phoneField = new TextField("Phone");

        // Configure field validations and bindings
        customerBinder.forField(nameField).asRequired("Name is required").bind(c -> c.name(), (c, v) -> {});

        customerBinder.forField(emailField).asRequired("Email is required").bind(c -> c.email(), (c, v) -> {});

        customerBinder.forField(phoneField).asRequired("Phone is required").bind(c -> c.phone(), (c, v) -> {});

        // Add fields to form
        form.add(nameField, emailField, phoneField);

        VerticalLayout layout = new VerticalLayout(sectionTitle, form);
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.getStyle().set("margin-bottom", "var(--lumo-space-l)");
        return layout;
    }

    private VerticalLayout createDeliveryAddressSection() {
        H3 sectionTitle = new H3("Delivery Address");

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("900px", 3));

        TextField addressLine1Field = new TextField("Address Line 1");
        TextField addressLine2Field = new TextField("Address Line 2");
        TextField cityField = new TextField("City");
        TextField stateField = new TextField("State");
        TextField zipCodeField = new TextField("Zip Code");
        TextField countryField = new TextField("Country");

        // Configure field validations and bindings
        addressBinder
                .forField(addressLine1Field)
                .asRequired("Address is required")
                .bind(a -> a.addressLine1(), (a, v) -> {});

        addressBinder.forField(cityField).asRequired("City is required").bind(a -> a.city(), (a, v) -> {});

        addressBinder.forField(stateField).asRequired("State is required").bind(a -> a.state(), (a, v) -> {});

        addressBinder.forField(zipCodeField).asRequired("Zip code is required").bind(a -> a.zipCode(), (a, v) -> {});

        addressBinder.forField(countryField).asRequired("Country is required").bind(a -> a.country(), (a, v) -> {});

        addressBinder.forField(addressLine2Field).bind(a -> a.addressLine2(), (a, v) -> {});

        // Add fields to form
        form.add(addressLine1Field, addressLine2Field, cityField, stateField, zipCodeField, countryField);

        // Configure form layout - 2 columns
        form.setColspan(addressLine1Field, 2);
        form.setColspan(addressLine2Field, 2);

        VerticalLayout layout = new VerticalLayout(sectionTitle, form);
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.getStyle().set("margin-bottom", "var(--lumo-space-l)");
        return layout;
    }

    private VerticalLayout createPaymentSection() {
        H3 sectionTitle = new H3("Payment Details");

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("900px", 3));

        TextField cardNumberField = new TextField("Card Number");
        cardNumberField.setValueChangeMode(ValueChangeMode.EAGER);
        cardNumberField.setPattern("[0-9]{13,19}");

        TextField cardHolderNameField = new TextField("Cardholder Name");

        IntegerField expiryMonthField = new IntegerField("Expiration Month (MM)");
        expiryMonthField.setMin(1);
        expiryMonthField.setMax(12);
        expiryMonthField.setStepButtonsVisible(true);

        IntegerField expiryYearField = new IntegerField("Expiration Year (YYYY)");
        expiryYearField.setMin(2023);
        expiryYearField.setMax(2030);
        expiryYearField.setStepButtonsVisible(true);

        TextField cvvField = new TextField("CVV");
        cvvField.setPattern("[0-9]{3,4}");
        cvvField.setMaxLength(4);

        // Configure field validations and bindings
        paymentBinder
                .forField(cardNumberField)
                .asRequired("Card number is required")
                .withValidator(value -> value.matches("[0-9]{13,19}"), "Invalid card number")
                .bind(PaymentRequest::getCardNumber, PaymentRequest::setCardNumber);

        paymentBinder
                .forField(cardHolderNameField)
                .asRequired("Cardholder name is required")
                .bind(p -> "", (p, v) -> {});

        paymentBinder
                .forField(expiryMonthField)
                .asRequired("Expiration month is required")
                .withValidator(month -> month >= 1 && month <= 12, "Month must be between 1-12")
                .bind(PaymentRequest::getExpiryMonth, PaymentRequest::setExpiryMonth);

        paymentBinder
                .forField(expiryYearField)
                .asRequired("Expiration year is required")
                .withValidator(year -> year >= 2023 && year <= 2030, "Year must be between 2023-2030")
                .bind(PaymentRequest::getExpiryYear, PaymentRequest::setExpiryYear);

        paymentBinder
                .forField(cvvField)
                .asRequired("CVV is required")
                .withValidator(cvv -> cvv.matches("[0-9]{3,4}"), "CVV must be 3-4 digits")
                .bind(PaymentRequest::getCvv, PaymentRequest::setCvv);

        // Add fields to form
        form.add(cardNumberField, cardHolderNameField, expiryMonthField, expiryYearField, cvvField);

        VerticalLayout layout = new VerticalLayout(sectionTitle, form);
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.getStyle().set("margin-bottom", "var(--lumo-space-l)");
        return layout;
    }

    private HorizontalLayout createOrderButtonSection() {
        placeOrderButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        placeOrderButton.getStyle().set("margin-top", "20px");
        //placeOrderButton.addClickListener(e -> placeOrder());
        placeOrderButton.addClickListener(e -> UI.getCurrent().navigate(CheckoutView.class));

        HorizontalLayout layout = new HorizontalLayout(placeOrderButton);
        layout.setJustifyContentMode(JustifyContentMode.END);
        layout.setWidthFull();
        return layout;
    }

    private void loadCart() {
        try {
            // Get cart from session or cookies if available
            cartId = getCartIdFromSession();

            // Fetch the cart from the API
            cart = cartController.getCart(cartId).getBody();

            if (cart != null) {
                cartId = cart.getId();

                // Store cart ID in session
                storeCartIdInSession(cartId);

                // Update cart UI
                updateCartUI();

                // Update cart badge in main layout
                Optional<MainLayout> mainLayout = getParentLayout();
                if (mainLayout.isPresent()) {
                    CartBadge cartBadge = mainLayout.get().getCartBadge();
                    cartBadge.updateCount(cart.getItems().size());
                }
            } else {
                cart = new Cart();
                showEmptyCartMessage();
            }
        } catch (Exception e) {
            log.error("Error loading cart", e);
            Notification.show("Failed to load cart: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            cart = new Cart();
            showEmptyCartMessage();
        }
    }

    private void updateCartUI() {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            cartGrid.setItems();
            totalPriceLabel.setText("Total: " + formatCurrency(BigDecimal.ZERO));
            showEmptyCartMessage();
            placeOrderButton.setEnabled(false);
        } else {
            cartGrid.setItems(cart.getItems());
            totalPriceLabel.setText("Total: " + formatCurrency(cart.getTotalAmount()));
            placeOrderButton.setEnabled(true);

            // Actualizamos la altura del grid
            updateGridHeight();
        }
    }

    private void showEmptyCartMessage() {
        cartGrid.setVisible(false);
        Notification.show("Your cart is empty. Continue shopping to add items.", 3000, Notification.Position.MIDDLE);
    }

    private void updateItemQuantity(String code, int quantity) {
        try {
            CartItemRequestDTO request = new CartItemRequestDTO();
            request.setCode(code);
            request.setQuantity(quantity);

            cart = cartController.updateCartItemQuantity(cartId, request);
            updateCartUI();
        } catch (Exception e) {
            log.error("Error updating item quantity", e);
            Notification.show("Failed to update quantity: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void removeItem(String code) {
        try {
            cart = cartController.removeCartItem(cartId, code);

            // Update cart badge in main layout
            Optional<MainLayout> mainLayout = getParentLayout();
            if (mainLayout.isPresent()) {
                CartBadge cartBadge = mainLayout.get().getCartBadge();
                if (cart != null && cart.getItems() != null) {
                    cartBadge.updateCount(cart.getItems().size());
                } else {
                    cartBadge.updateCount(0);
                }
            }

            updateCartUI();
            Notification.show("Item removed from cart", 2000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            log.error("Error removing item", e);
            Notification.show("Failed to remove item: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearCart() {
        try {
            if (cartId != null) {
                cartController.removeCart(cartId);
                cartId = null;
                cart = new Cart();

                // Update cart badge in main layout
                Optional<MainLayout> mainLayout = getParentLayout();
                if (mainLayout.isPresent()) {
                    CartBadge cartBadge = mainLayout.get().getCartBadge();
                    cartBadge.updateCount(0);
                }

                updateCartUI();
                Notification.show("Cart cleared", 2000, Notification.Position.MIDDLE);
            }
        } catch (Exception e) {
            log.error("Error clearing cart", e);
            Notification.show("Failed to clear cart: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    // NUEVO MÉTODO DE PROCESAMIENTO DE ORDEN
    private void placeOrder() {
        try {
            // Validar que el carrito no esté vacío
            if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                Notification.show(
                                "No se puede procesar la orden con un carrito vacío",
                                3000,
                                Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            log.info("Iniciando proceso de validación para completar la orden");

            // Validar información del cliente
            if (!customerBinder.isValid()) {
                // Mostrar errores específicos
                StringBuilder errorMessage = new StringBuilder("Por favor complete la información del cliente:\n");
                customerBinder.validate().getFieldValidationErrors().forEach(error -> errorMessage
                        .append("- ")
                        .append(error.getMessage())
                        .append("\n"));

                Notification.show(errorMessage.toString(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);

                // Hacer scroll al formulario de cliente
                UI.getCurrent()
                        .getPage()
                        .executeJs(
                                "document.querySelector('h3:contains(\"Customer Information\")').scrollIntoView({behavior: 'smooth'})");
                return;
            }

            // Validar dirección de envío
            if (!addressBinder.isValid()) {
                // Mostrar errores específicos
                StringBuilder errorMessage = new StringBuilder("Por favor complete la dirección de envío:\n");
                addressBinder.validate().getFieldValidationErrors().forEach(error -> errorMessage
                        .append("- ")
                        .append(error.getMessage())
                        .append("\n"));

                Notification.show(errorMessage.toString(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);

                // Hacer scroll al formulario de dirección
                UI.getCurrent()
                        .getPage()
                        .executeJs(
                                "document.querySelector('h3:contains(\"Delivery Address\")').scrollIntoView({behavior: 'smooth'})");
                return;
            }

            // Validar información de pago
            if (!paymentBinder.isValid()) {
                // Mostrar errores específicos
                StringBuilder errorMessage = new StringBuilder("Por favor complete la información de pago:\n");
                paymentBinder.validate().getFieldValidationErrors().forEach(error -> errorMessage
                        .append("- ")
                        .append(error.getMessage())
                        .append("\n"));

                Notification.show(errorMessage.toString(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);

                // Hacer scroll al formulario de pago
                UI.getCurrent()
                        .getPage()
                        .executeJs(
                                "document.querySelector('h3:contains(\"Payment Details\")').scrollIntoView({behavior: 'smooth'})");
                return;
            }

            // Extraer los datos de pago para validar con el servicio de pagos
            PaymentRequest paymentRequest = extractPaymentRequestFromForm();

            // Validar el pago antes de procesar la orden
            validatePaymentAndProceed(paymentRequest);

        } catch (Exception e) {
            log.error("Error al validar la orden", e);
            Notification.show("Error al procesar la orden: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Valida el pago usando el servicio de pago y continúa con la confirmación
     * si el pago es válido
     */
    private void validatePaymentAndProceed(PaymentRequest paymentRequest) {
        try {
            // Mostrar un indicador de carga mientras se valida el pago
            placeOrderButton.setEnabled(false);
            placeOrderButton.setText("Validando pago...");
            placeOrderButton.setIcon(new Icon(VaadinIcon.SPINNER));

            // Validar el pago mediante el servicio de pago
            PaymentResponse paymentResponse = paymentController.validate(paymentRequest);

            // Restaurar el botón
            placeOrderButton.setEnabled(true);
            placeOrderButton.setText("Place Order");
            placeOrderButton.setIcon(null);

            if (paymentResponse != null && paymentResponse.getStatus() == PaymentStatus.ACCEPTED) {
                // Pago válido, mostrar confirmación
                showOrderConfirmationDialog();
            } else {
                // Pago inválido, mostrar error
                String errorMsg = "La información de pago no pudo ser validada";

                Notification.show("Error de validación de pago: " + errorMsg, 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);

                // Hacer scroll al formulario de pago
                UI.getCurrent()
                        .getPage()
                        .executeJs(
                                "document.querySelector('h3:contains(\"Payment Details\")').scrollIntoView({behavior: 'smooth'})");
            }

        } catch (HttpClientErrorException e) {
            // Restaurar el botón
            placeOrderButton.setEnabled(true);
            placeOrderButton.setText("Place Order");
            placeOrderButton.setIcon(null);

            // Manejar diferentes tipos de errores HTTP
            String errorMessage;
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                errorMessage = "Información de pago inválida";
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                errorMessage = "No autorizado para procesar este pago";
            } else {
                errorMessage = "Error al validar el pago: " + e.getMessage();
            }

            log.error("Error en la validación del pago: {}", errorMessage, e);
            Notification.show(errorMessage, 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);

        } catch (Exception e) {
            // Restaurar el botón
            placeOrderButton.setEnabled(true);
            placeOrderButton.setText("Place Order");
            placeOrderButton.setIcon(null);

            log.error("Error al validar el pago", e);
            Notification.show("Error al validar el pago: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Muestra el diálogo de confirmación antes de procesar la orden
     */
    private void showOrderConfirmationDialog() {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Confirmar orden");

        // Construir un resumen de la orden para mostrar
        StringBuilder orderSummary = new StringBuilder();
        orderSummary
                .append("Total: ")
                .append(formatCurrency(cart.getTotalAmount()))
                .append("\n");
        orderSummary.append("Productos: ").append(cart.getItems().size()).append("\n");
        orderSummary
                .append("Envío a: ")
                .append(extractAddressFromForm().city())
                .append(", ")
                .append(extractAddressFromForm().country());

        confirmDialog.setText(orderSummary.toString());

        confirmDialog.setCancelable(true);
        confirmDialog.setCancelText("Cancelar");

        confirmDialog.setConfirmText("Confirmar orden");
        confirmDialog.setConfirmButtonTheme("primary success");

        confirmDialog.addConfirmListener(event -> processOrder());

        confirmDialog.open();
    }

    /**
     * Procesa la orden con validación adicional y corrección de campos
     * para evitar el error de país vacío
     */
    private void processOrder() {
        try {
            // Mostrar indicador de carga mientras se procesa la orden
            UI.getCurrent().setPollInterval(500);
            placeOrderButton.setEnabled(false);
            placeOrderButton.setText("Procesando...");
            placeOrderButton.setIcon(new Icon(VaadinIcon.SPINNER));
            placeOrderButton.getStyle().set("cursor", "wait");

            // Convertir los elementos del carrito a OrderItems
            Set<OrderItem> orderItems = cart.getItems().stream()
                    .map(item -> new OrderItem(item.getCode(), item.getName(), item.getPrice(), item.getQuantity()))
                    .collect(Collectors.toSet());

            // Crear customer con valores reales de los campos
            Customer customer = extractCustomerFromForm();

            // Obtener y verificar manualmente los campos de dirección para asegurar que sean correctos
            // (Corrige el problema específico de zipCode y country invertidos)
            String addressLine1 = "";
            String addressLine2 = "";
            String city = "";
            String state = "";
            String zipCode = "";
            String country = "";

            // Recorrer todos los campos del binder para extraer sus valores por etiqueta
            for (HasValue<?, ?> field : addressBinder.getFields().collect(Collectors.toList())) {
                if (field instanceof TextField textField) {
                    switch (textField.getLabel()) {
                        case "Address Line 1":
                            addressLine1 = textField.getValue();
                            break;
                        case "Address Line 2":
                            addressLine2 = textField.getValue();
                            break;
                        case "City":
                            city = textField.getValue();
                            break;
                        case "State":
                            state = textField.getValue();
                            break;
                        case "Zip Code":
                            zipCode = textField.getValue();
                            break;
                        case "Country":
                            country = textField.getValue();
                            break;
                    }
                }
            }

            // Verificar manualmente cada campo requerido
            StringBuilder errors = new StringBuilder();
            if (addressLine1.isEmpty()) {
                errors.append("Address Line 1 is required\n");
            }
            if (city.isEmpty()) {
                errors.append("City is required\n");
            }
            if (state.isEmpty()) {
                errors.append("State is required\n");
            }
            if (zipCode.isEmpty()) {
                errors.append("Zip Code is required\n");
            }
            if (country.isEmpty()) {
                errors.append("Country is required\n");
            }

            if (errors.length() > 0) {
                // Restaurar el botón
                placeOrderButton.setEnabled(true);
                placeOrderButton.setText("Place Order");
                placeOrderButton.setIcon(null);
                placeOrderButton.getStyle().remove("cursor");

                // Mostrar errores
                Notification.show(
                                "Por favor complete todos los campos requeridos:\n" + errors,
                                5000,
                                Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);

                UI.getCurrent().setPollInterval(-1);
                return;
            }

            // Registrar los valores para depuración
            log.info(
                    "Dirección para crear orden - addressLine1: '{}', addressLine2: '{}', city: '{}', state: '{}', zipCode: '{}', country: '{}'",
                    addressLine1,
                    addressLine2,
                    city,
                    state,
                    zipCode,
                    country);

            // Crear dirección con los valores validados
            log.info(
                    "⚠️ ATENCIÓN - VERIFICANDO CAMPOS FINALES: addressLine1='{}', addressLine2='{}', city='{}', state='{}', zipCode='{}', country='{}'",
                    addressLine1,
                    addressLine2,
                    city,
                    state,
                    zipCode,
                    country);

            // Y si el campo country está vacío a pesar de que el usuario lo ingresó,
            // puedes forzar un valor tomándolo directamente del campo en la UI:
            if (country.isEmpty()) {
                // Buscar el campo Country directamente
                for (HasValue<?, ?> field : addressBinder.getFields().collect(Collectors.toList())) {
                    if (field instanceof TextField textField && "Country".equals(textField.getLabel())) {
                        country = textField.getValue();
                        log.info("⚠️ Recuperando country directamente del campo UI: '{}'", country);
                        break;
                    }
                }
            }

            // Finalmente crear el objeto Address con los valores correctos
            Address address = new Address(addressLine1, addressLine2, city, state, zipCode, country);

            // Crear la solicitud de orden
            CreateOrderRequest orderRequest = new CreateOrderRequest(orderItems, customer, address);

            // Enviar la solicitud al controlador de órdenes
            OrderConfirmationDTO confirmation = orderController.createOrder(orderRequest);

            // Manejar la respuesta
            if (confirmation != null && confirmation.orderNumber() != null) {
                // Limpiar el carrito
                clearCart();

                // Mostrar notificación de éxito
                Notification successNotification = Notification.show(
                        "¡Orden procesada con éxito! Número de orden: " + confirmation.orderNumber(),
                        5000,
                        Notification.Position.MIDDLE);
                successNotification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Navegar a la página de confirmación
                UI.getCurrent().navigate("orders/" + confirmation.orderNumber());
            } else {
                // Restaurar el botón
                placeOrderButton.setEnabled(true);
                placeOrderButton.setText("Place Order");
                placeOrderButton.setIcon(null);
                placeOrderButton.getStyle().remove("cursor");

                Notification.show(
                                "No se pudo procesar la orden, por favor intente nuevamente",
                                3000,
                                Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            UI.getCurrent().setPollInterval(-1);

        } catch (RestClientException e) {
            // Restaurar el botón
            placeOrderButton.setEnabled(true);
            placeOrderButton.setText("Place Order");
            placeOrderButton.setIcon(null);
            placeOrderButton.getStyle().remove("cursor");

            log.error("Error al procesar la orden", e);

            // Extraer el mensaje de error de la excepción
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Country is required")) {
                errorMessage =
                        "El país es un campo obligatorio. Por favor complete todos los campos de dirección correctamente.";
            }

            Notification.show("Error al procesar la orden: " + errorMessage, 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);

            UI.getCurrent().setPollInterval(-1);
        } catch (Exception e) {
            // Restaurar el botón
            placeOrderButton.setEnabled(true);
            placeOrderButton.setText("Place Order");
            placeOrderButton.setIcon(null);
            placeOrderButton.getStyle().remove("cursor");

            log.error("Error inesperado al procesar la orden", e);
            Notification.show("Error inesperado: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);

            UI.getCurrent().setPollInterval(-1);
        }
    }

    /**
     * Extrae la información del cliente del formulario
     */
    private Customer extractCustomerFromForm() {
        String customerName = customerBinder
                .getFields()
                .findFirst()
                .map(HasValue::getValue)
                .map(Object::toString)
                .orElse("");

        String customerEmail = customerBinder
                .getFields()
                .skip(1)
                .findFirst()
                .filter(field -> field instanceof EmailField)
                .map(HasValue::getValue)
                .map(Object::toString)
                .orElse("");

        String customerPhone = customerBinder
                .getFields()
                .skip(2)
                .findFirst()
                .map(HasValue::getValue)
                .map(Object::toString)
                .orElse("");

        return new Customer(customerName, customerEmail, customerPhone);
    }

    /**
     * Extrae la dirección del formulario
     */
    private Address extractAddressFromForm() {
        try {
            // Extraer valores por etiqueta en lugar de por posición
            String addressLine1 = "";
            String addressLine2 = "";
            String city = "";
            String state = "";
            String zipCode = "";
            String country = "";

            // Recorrer todos los campos del formulario para extraer valores por etiqueta
            for (HasValue<?, ?> field : addressBinder.getFields().collect(Collectors.toList())) {
                if (field instanceof TextField textField) {
                    String label = textField.getLabel();
                    String value = textField.getValue() != null ? textField.getValue() : "";

                    switch (label) {
                        case "Address Line 1":
                            addressLine1 = value;
                            break;
                        case "Address Line 2":
                            addressLine2 = value;
                            break;
                        case "City":
                            city = value;
                            break;
                        case "State":
                            state = value;
                            break;
                        case "Zip Code":
                            zipCode = value;
                            break;
                        case "Country":
                            country = value;
                            break;
                    }
                }
            }

            // Log para diagnóstico
            log.info(
                    "Valores extraídos del formulario - addressLine1: '{}', addressLine2: '{}', city: '{}', state: '{}', zipCode: '{}', country: '{}'",
                    addressLine1,
                    addressLine2,
                    city,
                    state,
                    zipCode,
                    country);

            // El método es llamado desde showOrderConfirmationDialog, donde solo necesitamos
            // mostrar información, no validarla completamente
            // Determinar si es llamado desde el diálogo de confirmación
            boolean isCalledFromDialog = false;
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : stackTrace) {
                if (element.getMethodName().contains("showOrderConfirmationDialog")) {
                    isCalledFromDialog = true;
                    break;
                }
            }

            if (isCalledFromDialog) {
                // Para el diálogo solo necesitamos mostrar la información disponible
                return new Address(addressLine1, addressLine2, city, state, zipCode, country);
            } else {
                // Para procesamiento real verificamos que todos los campos requeridos tengan valores
                if (addressLine1.isEmpty()) {
                    throw new IllegalArgumentException("Address Line 1 is required");
                }
                if (city.isEmpty()) {
                    throw new IllegalArgumentException("City is required");
                }
                if (state.isEmpty()) {
                    throw new IllegalArgumentException("State is required");
                }
                if (zipCode.isEmpty()) {
                    throw new IllegalArgumentException("Zip Code is required");
                }
                if (country.isEmpty()) {
                    throw new IllegalArgumentException("Country is required");
                }

                return new Address(addressLine1, addressLine2, city, state, zipCode, country);
            }

        } catch (Exception e) {
            log.error("Error al extraer dirección del formulario", e);
            // Si ocurre algún error, devolvemos una dirección vacía pero no lanzamos excepción
            // para evitar bloquear el flujo del diálogo de confirmación
            if (Thread.currentThread().getStackTrace()[2].getMethodName().contains("showOrderConfirmationDialog")) {
                return new Address("", "", "", "", "", "");
            } else {
                throw e; // Re-lanzar la excepción si no es llamado desde el diálogo
            }
        }
    }

    /**
     * Método alternativo para extraer la dirección que busca los campos por etiqueta
     * en lugar de confiar en el orden de los campos
     */
    private Address extractAddressFromFormByLabel() {
        // Helper function to find a field by its label
        Function<String, Optional<HasValue<?, ?>>> findFieldByLabel = label -> addressBinder
                .getFields()
                .filter(field -> field instanceof HasValue && field instanceof Component)
                .filter(field -> {
                    if (field instanceof TextField textField) {
                        return label.equals(textField.getLabel());
                    }
                    return false;
                })
                .findFirst();

        // Get values from fields identified by their labels
        String addressLine1 = findFieldByLabel
                .apply("Address Line 1")
                .map(HasValue::getValue)
                .map(Object::toString)
                .orElse("");

        String addressLine2 = findFieldByLabel
                .apply("Address Line 2")
                .map(HasValue::getValue)
                .map(Object::toString)
                .orElse("");

        String city = findFieldByLabel
                .apply("City")
                .map(HasValue::getValue)
                .map(Object::toString)
                .orElse("");

        String state = findFieldByLabel
                .apply("State")
                .map(HasValue::getValue)
                .map(Object::toString)
                .orElse("");

        String zipCode = findFieldByLabel
                .apply("Zip Code")
                .map(HasValue::getValue)
                .map(Object::toString)
                .orElse("");

        String country = findFieldByLabel
                .apply("Country")
                .map(HasValue::getValue)
                .map(Object::toString)
                .orElse("");

        // Log all extracted values for debugging
        log.info(
                "Dirección extraída del formulario - addressLine1: '{}', addressLine2: '{}', city: '{}', state: '{}', zipCode: '{}', country: '{}'",
                addressLine1,
                addressLine2,
                city,
                state,
                zipCode,
                country);

        // Validación adicional para asegurar que los campos requeridos no estén vacíos
        if (addressLine1.isEmpty()) {
            throw new IllegalArgumentException("Address Line 1 is required");
        }
        if (city.isEmpty()) {
            throw new IllegalArgumentException("City is required");
        }
        if (state.isEmpty()) {
            throw new IllegalArgumentException("State is required");
        }
        if (zipCode.isEmpty()) {
            throw new IllegalArgumentException("Zip Code is required");
        }
        if (country.isEmpty()) {
            throw new IllegalArgumentException("Country is required");
        }

        return new Address(addressLine1, addressLine2, city, state, zipCode, country);
    }

    /**
     * Método que extrae la dirección accediendo directamente a los campos del formulario
     * Debe ser invocado dentro del método placeOrder() para asegurar que los campos estén disponibles
     */
    private Address extractAddressWithDirectAccess(FormLayout addressForm) {
        // Encontrar los campos de dirección directamente del formulario
        TextField addressLine1Field = (TextField) addressForm
                .getChildren()
                .filter(component ->
                        component instanceof TextField && "Address Line 1".equals(((TextField) component).getLabel()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Address Line 1 field not found"));

        TextField addressLine2Field = (TextField) addressForm
                .getChildren()
                .filter(component ->
                        component instanceof TextField && "Address Line 2".equals(((TextField) component).getLabel()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Address Line 2 field not found"));

        TextField cityField = (TextField) addressForm
                .getChildren()
                .filter(component ->
                        component instanceof TextField && "City".equals(((TextField) component).getLabel()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("City field not found"));

        TextField stateField = (TextField) addressForm
                .getChildren()
                .filter(component ->
                        component instanceof TextField && "State".equals(((TextField) component).getLabel()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("State field not found"));

        TextField zipCodeField = (TextField) addressForm
                .getChildren()
                .filter(component ->
                        component instanceof TextField && "Zip Code".equals(((TextField) component).getLabel()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Zip Code field not found"));

        TextField countryField = (TextField) addressForm
                .getChildren()
                .filter(component ->
                        component instanceof TextField && "Country".equals(((TextField) component).getLabel()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Country field not found"));

        // Obtener los valores de los campos
        String addressLine1 = addressLine1Field.getValue();
        String addressLine2 = addressLine2Field.getValue();
        String city = cityField.getValue();
        String state = stateField.getValue();
        String zipCode = zipCodeField.getValue();
        String country = countryField.getValue();

        // Registrar los valores para depuración
        log.info(
                "Dirección extraída por acceso directo - addressLine1: '{}', addressLine2: '{}', city: '{}', state: '{}', zipCode: '{}', country: '{}'",
                addressLine1,
                addressLine2,
                city,
                state,
                zipCode,
                country);

        return new Address(addressLine1, addressLine2, city, state, zipCode, country);
    }

    /**
     * Extrae los datos de pago del formulario
     */
    private PaymentRequest extractPaymentRequestFromForm() {
        PaymentRequest request = new PaymentRequest();

        // Capturar el número de tarjeta
        paymentBinder
                .getFields()
                .findFirst()
                .ifPresent(field -> request.setCardNumber(field.getValue().toString()));

        // Capturar el mes de expiración
        paymentBinder.getFields().skip(2).findFirst().ifPresent(field -> {
            if (field.getValue() != null) {
                request.setExpiryMonth((Integer) field.getValue());
            }
        });

        // Capturar el año de expiración
        paymentBinder.getFields().skip(3).findFirst().ifPresent(field -> {
            if (field.getValue() != null) {
                request.setExpiryYear((Integer) field.getValue());
            }
        });

        // Capturar el CVV
        paymentBinder
                .getFields()
                .skip(4)
                .findFirst()
                .ifPresent(field -> request.setCvv(field.getValue().toString()));

        return request;
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyFormat.format(amount);
    }

    private String getCartIdFromSession() {
        // In a real application, this would retrieve the cart ID from session or cookies
        // For now, using a simple UI session attribute
        Object cartIdObj = UI.getCurrent().getSession().getAttribute("cartId");
        return cartIdObj != null ? cartIdObj.toString() : null;
    }

    private void storeCartIdInSession(String cartId) {
        // In a real application, this would store the cart ID in session or cookies
        // For now, using a simple UI session attribute
        UI.getCurrent().getSession().setAttribute("cartId", cartId);
    }

    private Optional<MainLayout> getParentLayout() {
        return Optional.ofNullable(UI.getCurrent()
                .getChildren()
                .filter(component -> component instanceof MainLayout)
                .findFirst()
                .map(component -> (MainLayout) component)
                .orElse(null));
    }
}
