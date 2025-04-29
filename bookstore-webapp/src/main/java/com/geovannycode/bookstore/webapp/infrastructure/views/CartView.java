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
import com.geovannycode.bookstore.webapp.infrastructure.api.controller.CartController;
import com.geovannycode.bookstore.webapp.infrastructure.api.controller.OrderController;
import com.geovannycode.bookstore.webapp.infrastructure.views.components.CardComponent;
import com.geovannycode.bookstore.webapp.infrastructure.views.components.CartBadge;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClientException;

@Route(value = "cart", layout = MainLayout.class)
@PageTitle("Shopping Cart")
public class CartView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(CartView.class);

    private final CartController cartController;
    private final OrderController orderController;

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

    public CartView(@Autowired CartController cartController, @Autowired OrderController orderController) {
        this.cartController = cartController;
        this.orderController = orderController;

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

        // Quantity column with editable field - CORRECCIÓN: eliminado setHasControls
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

        cartGrid.setHeight("350px");
        cartGrid.getStyle().set("overflow", "auto");
        cartGrid.setColumnReorderingAllowed(true);
        cartGrid.setWidthFull();
        // cartGrid.setAllRowsVisible(true);
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
        placeOrderButton.addClickListener(e -> placeOrder());

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

    private void placeOrder() {
        try {
            if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                Notification.show("Cannot place order with empty cart", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Validar información del cliente
            if (!customerBinder.isValid()) {
                Notification.show(
                                "Please fill in all required customer information", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Validar dirección de envío
            if (!addressBinder.isValid()) {
                Notification.show(
                                "Please fill in all required shipping information", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Validar información de pago
            if (!paymentBinder.isValid()) {
                Notification.show("Please fill in all required payment information", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Convertir los elementos del carrito a OrderItems
            Set<OrderItem> orderItems = cart.getItems().stream()
                    .map(item -> new OrderItem(item.getCode(), item.getName(), item.getPrice(), item.getQuantity()))
                    .collect(Collectors.toSet());

            // Crear customer y address con valores reales de los campos
            String customerName =
                    customerBinder.getFields().findFirst().get().getValue().toString();
            String customerEmail =
                    ((EmailField) customerBinder.getFields().skip(1).findFirst().get()).getValue();
            String customerPhone = customerBinder
                    .getFields()
                    .skip(2)
                    .findFirst()
                    .get()
                    .getValue()
                    .toString();

            Customer customer = new Customer(customerName, customerEmail, customerPhone);

            String addressLine1 =
                    addressBinder.getFields().findFirst().get().getValue().toString();
            String addressLine2 = addressBinder
                    .getFields()
                    .skip(1)
                    .findFirst()
                    .get()
                    .getValue()
                    .toString();
            String city = addressBinder
                    .getFields()
                    .skip(2)
                    .findFirst()
                    .get()
                    .getValue()
                    .toString();
            String state = addressBinder
                    .getFields()
                    .skip(3)
                    .findFirst()
                    .get()
                    .getValue()
                    .toString();
            String zipCode = addressBinder
                    .getFields()
                    .skip(4)
                    .findFirst()
                    .get()
                    .getValue()
                    .toString();
            String country = addressBinder
                    .getFields()
                    .skip(5)
                    .findFirst()
                    .get()
                    .getValue()
                    .toString();

            Address address = new Address(addressLine1, addressLine2, city, state, zipCode, country);

            // Crear la solicitud de orden
            CreateOrderRequest orderRequest = new CreateOrderRequest(orderItems, customer, address);

            // Enviar la solicitud al controlador de órdenes
            OrderConfirmationDTO confirmation = orderController.createOrder(orderRequest);

            // Manejar la respuesta
            if (confirmation != null && confirmation.orderNumber() != null) {
                // Limpiar el carrito
                clearCart();

                // Navegar a la página de confirmación
                UI.getCurrent().navigate("orders/" + confirmation.orderNumber());

                Notification.show("Order placed successfully!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification.show("Failed to place order, please try again", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

        } catch (RestClientException e) {
            log.error("Error placing order", e);
            Notification.show("Failed to place order: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
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
