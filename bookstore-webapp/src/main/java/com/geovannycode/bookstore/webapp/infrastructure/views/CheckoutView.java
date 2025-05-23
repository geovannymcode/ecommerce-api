package com.geovannycode.bookstore.webapp.infrastructure.views;

import com.geovannycode.bookstore.webapp.domain.model.Address;
import com.geovannycode.bookstore.webapp.domain.model.Cart;
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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabVariant;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Route(value = "checkout", layout = MainLayout.class)
@PageTitle("Checkout")
public class CheckoutView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(CheckoutView.class);

    private final CartController cartController;
    private final OrderController orderController;
    private final PaymentController paymentController;

    private String cartId;
    private Cart cart;

    // Form binders
    private final Binder<Customer> customerBinder = new Binder<>();
    private final Binder<Address> addressBinder = new Binder<>();
    private final Binder<PaymentRequest> paymentBinder = new Binder<>(PaymentRequest.class);

    // Selected payment method
    private String selectedPaymentMethod = null;

    // Form field references
    private TextField nameField;
    private EmailField emailField;
    private TextField phoneField;
    private TextField addressLine1Field;
    private TextField addressLine2Field;
    private TextField cityField;
    private TextField stateField;
    private TextField zipCodeField;
    private TextField countryField;
    private TextField cardNumberField;
    private TextField cardHolderNameField;
    private IntegerField expiryMonthField;
    private IntegerField expiryYearField;
    private TextField cvvField;
    private Tab customerTab;
    private Tab addressTab;
    private Tab paymentTab;

    private final Button placeOrderButton = new Button("Place Order");
    private Button next;
    private Button previous;
    private HorizontalLayout navigationButtons = new HorizontalLayout();

    private final Tabs stepTabs = new Tabs();
    private final Div stepContent = new Div();
    private final Map<Tab, Component> tabToComponent = new HashMap<>();

    public CheckoutView(
            @Autowired CartController cartController,
            @Autowired OrderController orderController,
            @Autowired PaymentController paymentController) {
        this.cartController = cartController;
        this.orderController = orderController;
        this.paymentController = paymentController;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 viewTitle = new H2("Checkout");

        // Order summary card
        CardComponent orderSummaryCard = new CardComponent();
        VerticalLayout orderSummaryLayout = createOrderSummaryLayout();
        orderSummaryCard.add(orderSummaryLayout);

        // Checkout steps
        customerTab = new Tab(VaadinIcon.USER.create(), new Span("Customer Info"));
        addressTab = new Tab(VaadinIcon.HOME.create(), new Span("Delivery Address"));
        paymentTab = new Tab(VaadinIcon.CREDIT_CARD.create(), new Span("Payment Method"));

        for (Tab tab : new Tab[] {customerTab, addressTab, paymentTab}) {
            tab.addThemeVariants(TabVariant.LUMO_ICON_ON_TOP);
        }

        addressTab.setEnabled(false);
        paymentTab.setEnabled(false);

        stepTabs.add(customerTab, addressTab, paymentTab);
        stepTabs.setWidthFull();

        // Configurar el listener para mostrar/ocultar el botón según el tab activo
        stepTabs.addSelectedChangeListener(event -> {
            // Si intentan cambiar a un tab deshabilitado, revertir a la selección anterior
            if (!event.getSelectedTab().isEnabled()) {
                stepTabs.setSelectedTab(event.getPreviousTab());
            }
            // De lo contrario, cambiar el contenido
            else {
                switchTabContent(event.getSelectedTab());
            }
        });

        Component customerSection = createCustomerSection();
        Component addressSection = createDeliveryAddressSection();
        Component paymentSection = createPaymentSection();

        tabToComponent.put(customerTab, customerSection);
        tabToComponent.put(addressTab, addressSection);
        tabToComponent.put(paymentTab, paymentSection);

        stepContent.add(customerSection);

        navigationButtons = new HorizontalLayout();
        previous = new Button("Previous", e -> navigatePrevious());
        next = new Button("Next", e -> validateAndNavigateNext());

        previous.setWidth("120px");
        next.setWidth("120px");
        next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        placeOrderButton.setWidth("120px");
        placeOrderButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        placeOrderButton.addClickListener(e -> placeOrder());
        placeOrderButton.setVisible(false);

        navigationButtons.add(previous, next);
        navigationButtons.setJustifyContentMode(JustifyContentMode.BETWEEN);
        navigationButtons.setWidthFull();

        navigationButtons.add(placeOrderButton);

        CardComponent checkoutCard = new CardComponent();
        checkoutCard.add(stepTabs, stepContent, navigationButtons);

        add(viewTitle, orderSummaryCard, checkoutCard);

        addFieldValueChangeListeners();
        addPhoneFieldEnterKeyListener();
        addCountryFieldEnterKeyListener();
        loadCart();
    }

    private void navigatePrevious() {
        int currentIndex = stepTabs.getSelectedIndex();
        if (currentIndex > 0) {
            stepTabs.setSelectedIndex(currentIndex - 1);
        }
    }

    private void validateAndNavigateNext() {
        int currentIndex = stepTabs.getSelectedIndex();

        if (currentIndex == 0) {
            // Validar Customer Info
            if (validateCustomerInfo()) {
                // Activar el siguiente tab
                stepTabs.setEnabled(true);
                stepTabs.setSelectedIndex(1);
            }
        } else if (currentIndex == 1) {
            // Validar Delivery Address
            if (validateDeliveryAddress()) {
                // Activar el siguiente tab
                stepTabs.setEnabled(true);
                stepTabs.setSelectedIndex(2);
            }
        }
    }

    private boolean validateDeliveryAddress() {
        if (!addressBinder.isValid()) {
            StringBuilder errorMessage = new StringBuilder("Please complete delivery address:\n");
            addressBinder.validate().getFieldValidationErrors().forEach(error -> errorMessage
                    .append("- ")
                    .append(error.getMessage())
                    .append("\n"));

            Notification.show(errorMessage.toString(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        return true;
    }

    private void checkCustomerInfoCompletion() {
        if (customerBinder.isValid()) {
            addressTab.setEnabled(true);
        } else {
            addressTab.setEnabled(false);
        }
    }

    private void checkAddressCompletion() {
        if (addressBinder.isValid()) {
            paymentTab.setEnabled(true);
        } else {
            paymentTab.setEnabled(false);
        }
    }

    private void addFieldValueChangeListeners() {
        // Verificar todos los campos de Customer Info cuando cambian
        nameField.addValueChangeListener(e -> checkCustomerInfoCompletion());
        emailField.addValueChangeListener(e -> checkCustomerInfoCompletion());
        phoneField.addValueChangeListener(e -> {
            checkCustomerInfoCompletion();

            // Si se presiona Enter y los datos son válidos, navegar automáticamente
            if (e.isFromClient() && e.getValue() != null && !e.getValue().isEmpty() && customerBinder.isValid()) {
                addressTab.setEnabled(true);
                stepTabs.setSelectedIndex(1);
            }
        });

        // Verificar todos los campos de Address cuando cambian
        countryField.addValueChangeListener(e -> {
            checkAddressCompletion();

            // Auto-navegación cuando se completa el país (y toda la dirección es válida)
            if (e.isFromClient() && addressBinder.isValid()) {
                // Habilitar pestaña de pago
                stepTabs.setEnabled(true);
                stepTabs.setSelectedIndex(2);
            }
        });

        // También añadir listeners a los otros campos de dirección
        addressLine1Field.addValueChangeListener(e -> checkAddressCompletion());
        cityField.addValueChangeListener(e -> checkAddressCompletion());
        stateField.addValueChangeListener(e -> checkAddressCompletion());
        zipCodeField.addValueChangeListener(e -> checkAddressCompletion());
    }

    private boolean validateCustomerInfo() {
        if (!customerBinder.isValid()) {
            StringBuilder errorMessage = new StringBuilder("Please complete customer information:\n");
            customerBinder.validate().getFieldValidationErrors().forEach(error -> errorMessage
                    .append("- ")
                    .append(error.getMessage())
                    .append("\n"));

            Notification.show(errorMessage.toString(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        return true;
    }

    private VerticalLayout createOrderSummaryLayout() {
        H3 summaryTitle = new H3("Order Summary");
        Span itemsCountLabel = new Span("Items: 0");
        Span totalLabel = new Span("Total: $0.00");
        totalLabel.getStyle().set("font-weight", "bold");

        Button viewCartButton = new Button("View Cart", e -> UI.getCurrent().navigate(CartView.class));
        viewCartButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        VerticalLayout layout = new VerticalLayout(summaryTitle, itemsCountLabel, totalLabel, viewCartButton);
        layout.setPadding(false);
        layout.setSpacing(true);

        return layout;
    }

    private void switchTabContent(Tab selectedTab) {
        stepContent.removeAll();
        stepContent.add(tabToComponent.get(selectedTab));

        // Determinar si estamos en la pestaña de pago
        boolean isPaymentTab = selectedTab.equals(stepTabs.getComponentAt(2));

        if (isPaymentTab) {

            next.setVisible(false);

            placeOrderButton.setVisible(selectedPaymentMethod != null);

            // Asegurarse de que el botón Place Order esté en el layout de navegación
            if (!navigationButtons.getChildren().anyMatch(c -> c == placeOrderButton)) {
                navigationButtons.add(placeOrderButton);
            }
            placeOrderButton.setVisible(selectedPaymentMethod != null);
        } else {
            // En otras pestañas, mostrar Next y ocultar Place Order
            next.setVisible(true);

            // Remover el botón Place Order si está en el layout
            navigationButtons
                    .getChildren()
                    .filter(c -> c == placeOrderButton)
                    .findFirst()
                    .ifPresent(button -> navigationButtons.remove(button));
        }
    }

    private VerticalLayout createCustomerSection() {
        H3 sectionTitle = new H3("Customer Information");

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

        nameField = new TextField("Full Name");
        emailField = new EmailField("Email");
        phoneField = new TextField("Phone");

        // Configure field validations and bindings
        customerBinder
                .forField(nameField)
                .asRequired("Name is required")
                .bind(Customer::getName, Customer::setName);

        customerBinder
                .forField(emailField)
                .asRequired("Email is required")
                .withValidator(email -> email.contains("@"), "Enter a valid email address")
                .bind(Customer::getEmail, Customer::setEmail);

        customerBinder
                .forField(phoneField)
                .asRequired("Phone is required")
                .bind(Customer::getPhone, Customer::setPhone);

        // Setting up the model
        customerBinder.setBean(new Customer("", "", ""));

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

        addressLine1Field = new TextField("Address Line 1");
        addressLine2Field = new TextField("Address Line 2"); // Opcional
        cityField = new TextField("City");
        stateField = new TextField("State");
        zipCodeField = new TextField("Zip Code");
        countryField = new TextField("Country");

        // Configure field validations and bindings (usando setters y getters)
        addressBinder.forField(addressLine1Field)
                .asRequired("Address is required")
                .bind(Address::getAddressLine1, Address::setAddressLine1);

        addressBinder.forField(addressLine2Field)
                .bind(Address::getAddressLine2, Address::setAddressLine2); // No asRequired()

        addressBinder.forField(cityField)
                .asRequired("City is required")
                .bind(Address::getCity, Address::setCity);

        addressBinder.forField(stateField)
                .asRequired("State is required")
                .bind(Address::getState, Address::setState);

        addressBinder.forField(zipCodeField)
                .asRequired("Zip code is required")
                .bind(Address::getZipCode, Address::setZipCode);

        addressBinder.forField(countryField)
                .asRequired("Country is required")
                .bind(Address::getCountry, Address::setCountry);

        // Setting up the model
        addressBinder.setBean(new Address());

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
        H3 sectionTitle = new H3("Select your payment method");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        // Container for payment options
        HorizontalLayout paymentOptionsLayout = new HorizontalLayout();
        paymentOptionsLayout.setWidthFull();
        paymentOptionsLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        paymentOptionsLayout.setSpacing(true);
        paymentOptionsLayout.getStyle().set("margin-top", "var(--lumo-space-m)");

        // Div for credit card form (initially hidden)
        Div creditCardFormDiv = new Div();
        creditCardFormDiv.setVisible(false);
        creditCardFormDiv.setWidth("100%");

        // Create payment options with images
        VerticalLayout creditCard = createPaymentOptionCard("Credit Card", "VISA, MasterCard, AMEX", "credit-card.png");

        VerticalLayout stripeCard = createPaymentOptionCard("Stripe", "Secure payment with Stripe", "stripe.png");

        VerticalLayout paypalCard = createPaymentOptionCard("PayPal", "Pay with your PayPal account", "paypal.png");

        VerticalLayout mercadoPagoCard =
                createPaymentOptionCard("Mercado Pago", "Multiple payment options", "mercadopago.png");

        // Add click events to cards
        creditCard.addClickListener(e -> {
            // Verificar que la dirección está completa antes de permitir seleccionar método de pago
            if (!addressBinder.isValid()) {
                Notification.show("Please complete all required address fields", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Si la dirección es válida, proceder normalmente
            selectPaymentCard(paymentOptionsLayout, creditCard);
            creditCardFormDiv.setVisible(true);
            selectedPaymentMethod = "credit";
            placeOrderButton.setVisible(true);
        });

        stripeCard.addClickListener(e -> {
            if (!addressBinder.isValid()) {
                Notification.show("Please complete all required address fields", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            selectPaymentCard(paymentOptionsLayout, stripeCard);
            creditCardFormDiv.setVisible(false);
            selectedPaymentMethod = "stripe";
            placeOrderButton.setVisible(true);
        });

        paypalCard.addClickListener(e -> {
            if (!addressBinder.isValid()) {
                Notification.show("Please complete all required address fields", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            selectPaymentCard(paymentOptionsLayout, paypalCard);
            creditCardFormDiv.setVisible(false);
            selectedPaymentMethod = "paypal";
            placeOrderButton.setVisible(true);
        });

        mercadoPagoCard.addClickListener(e -> {
            if (!addressBinder.isValid()) {
                Notification.show("Please complete all required address fields", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            selectPaymentCard(paymentOptionsLayout, mercadoPagoCard);
            creditCardFormDiv.setVisible(false);
            selectedPaymentMethod = "mercadopago";
            placeOrderButton.setVisible(true);
        });

        // Add cards to layout with correct spacing
        paymentOptionsLayout.add(creditCard, stripeCard, paypalCard, mercadoPagoCard);
        paymentOptionsLayout.setAlignItems(FlexComponent.Alignment.STRETCH);

        // Create credit card form
        FormLayout creditCardForm = createCreditCardForm();
        creditCardFormDiv.add(creditCardForm);

        // Add everything to main layout
        layout.add(sectionTitle, paymentOptionsLayout, creditCardFormDiv);
        layout.getStyle().set("margin-bottom", "var(--lumo-space-l)");

        return layout;
    }

    private VerticalLayout createPaymentOptionCard(String title, String description, String imageName) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("payment-option-card");
        card.setSpacing(false);
        card.setPadding(true);
        card.setAlignItems(Alignment.CENTER);
        card.setWidth("280px"); // Make card wider
        card.setHeight("160px"); // Fixed height to align all cards
        card.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        card.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        card.getStyle().set("cursor", "pointer");
        card.getStyle().set("transition", "box-shadow 0.3s, border-color 0.3s");
        card.getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.05)");

        // Hover effect
        card.getElement().addEventListener("mouseover", e -> {
            card.getStyle().set("box-shadow", "0 5px 15px rgba(0,0,0,0.1)");
            card.getStyle().set("border-color", "var(--lumo-primary-color-50pct)");
        });
        card.getElement().addEventListener("mouseout", e -> {
            card.getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.05)");
            card.getStyle().set("border-color", "var(--lumo-contrast-10pct)");
        });

        // Image logo - with proper sizing
        com.vaadin.flow.component.html.Image logo =
                new com.vaadin.flow.component.html.Image("images/payment/" + imageName, title);

        // Set fixed height to maintain consistency
        if (imageName.equals("credit-card.png")) {
            logo.setHeight("60px"); // Credit card image needs specific height
        } else {
            logo.setHeight("40px"); // Other logos need consistent height
        }
        logo.getStyle().set("object-fit", "contain");
        logo.getStyle().set("margin-bottom", "1em");

        // Title
        H3 titleLabel = new H3(title);
        titleLabel.getStyle().set("margin", "0.5em 0 0.2em 0");
        titleLabel.getStyle().set("font-size", "1.2em");

        // Description
        Span descLabel = new Span(description);
        descLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
        descLabel.getStyle().set("font-size", "0.9em");
        descLabel.getStyle().set("text-align", "center");

        card.add(logo, titleLabel, descLabel);
        return card;
    }

    private void selectPaymentCard(HorizontalLayout container, VerticalLayout selectedCard) {
        // Remove selection from all cards
        container.getChildren().forEach(component -> {
            if (component instanceof VerticalLayout) {
                VerticalLayout card = (VerticalLayout) component;
                card.getStyle().remove("border-color");
                card.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
                card.getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.05)");
            }
        });

        // Mark selected card
        selectedCard.getStyle().set("border-color", "var(--lumo-primary-color)");
        selectedCard.getStyle().set("border-width", "2px");
        selectedCard.getStyle().set("box-shadow", "0 5px 15px rgba(0,0,0,0.1)");
    }

    private FormLayout createCreditCardForm() {
        // Crear el formulario de tarjeta de crédito
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("900px", 3));

        // Agregar título al formulario
        H3 formTitle = new H3("Payment Details");
        form.add(formTitle);
        form.setColspan(formTitle, 3);

        cardNumberField = new TextField("Card Number");
        cardNumberField.setValueChangeMode(ValueChangeMode.EAGER);
        cardNumberField.setPattern("[0-9]{13,19}");

        cardHolderNameField = new TextField("Cardholder Name");

        expiryMonthField = new IntegerField("Expiration Month (MM)");
        expiryMonthField.setMin(1);
        expiryMonthField.setMax(12);
        expiryMonthField.setStepButtonsVisible(true);

        expiryYearField = new IntegerField("Expiration Year (YYYY)");
        expiryYearField.setMin(2023);
        expiryYearField.setMax(2030);
        expiryYearField.setStepButtonsVisible(true);

        cvvField = new TextField("CVV");
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

        // Setting up the model
        paymentBinder.setBean(new PaymentRequest());

        // Add fields to form
        form.add(cardNumberField, cardHolderNameField, expiryMonthField, expiryYearField, cvvField);

        return form;
    }

    private void loadCart() {
        cartId = getCartIdFromSession();
        cart = cartController.getCart(cartId).getBody();

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            Notification.show(
                    "Your cart is empty. Please add items before checkout.", 3000, Notification.Position.MIDDLE);
            UI.getCurrent().navigate(CartView.class);
            return;
        }

        // Update summary
        updateOrderSummary();
    }

    private void updateOrderSummary() {
        if (cart != null && cart.getItems() != null) {
            // Find the order summary components
            Component orderSummaryCard = getChildren()
                    .filter(component -> component instanceof CardComponent)
                    .findFirst()
                    .orElse(null);

            if (orderSummaryCard != null) {
                VerticalLayout summaryLayout = (VerticalLayout) ((CardComponent) orderSummaryCard)
                        .getChildren()
                        .findFirst()
                        .orElse(null);

                if (summaryLayout != null) {
                    // Update item count
                    summaryLayout
                            .getChildren()
                            .filter(c ->
                                    c instanceof Span && ((Span) c).getText().startsWith("Items:"))
                            .findFirst()
                            .ifPresent(c -> ((Span) c)
                                    .setText("Items: " + cart.getItems().size()));

                    // Update total
                    BigDecimal total = cart.getItems().stream()
                            .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    summaryLayout
                            .getChildren()
                            .filter(c ->
                                    c instanceof Span && ((Span) c).getText().startsWith("Total:"))
                            .findFirst()
                            .ifPresent(c -> ((Span) c).setText("Total: " + formatCurrency(total)));
                }
            }
        }
    }

    private void placeOrder() {
        try {
            // Validate cart is not empty
            if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                Notification.show("Cannot process an order with an empty cart", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            log.info("Starting validation process to complete the order");

            // Validate customer information
            if (!customerBinder.isValid()) {
                // Mostrar errores y navegar al tab correspondiente
                showCustomerValidationErrors();
                stepTabs.setSelectedIndex(0);
                return;
            }

            // Validate delivery address
            if (!addressBinder.isValid()) {
                // Mostrar errores y navegar al tab correspondiente
                showAddressValidationErrors();
                stepTabs.setSelectedIndex(1);
                return;
            }

            // Validate payment method is selected
            if (selectedPaymentMethod == null) {
                Notification.show("Please select a payment method", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if ("credit".equals(selectedPaymentMethod) && !paymentBinder.isValid()) {
                StringBuilder errorMessage = new StringBuilder("Please complete payment information:\n");
                paymentBinder.validate().getFieldValidationErrors().forEach(error -> errorMessage
                        .append("- ")
                        .append(error.getMessage())
                        .append("\n"));

                Notification.show(errorMessage.toString(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Proceder con el pago según el método seleccionado
            if ("credit".equals(selectedPaymentMethod)) {
                PaymentRequest paymentRequest = paymentBinder.getBean();
                validatePaymentAndProceed(paymentRequest);
            } else {
                processAlternativePaymentMethod(selectedPaymentMethod);
            }

        } catch (Exception e) {
            log.error("Error validating order", e);
            Notification.show("Error processing order: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void processAlternativePaymentMethod(String method) {
        try {
            // Mostrar indicador de carga
            placeOrderButton.setEnabled(false);
            placeOrderButton.setText("Processing " + method + " payment...");
            placeOrderButton.setIcon(new Icon(VaadinIcon.SPINNER));

            // Simulamos procesamiento del pago
            Thread.sleep(2000);

            // Restauramos el botón
            placeOrderButton.setEnabled(true);
            placeOrderButton.setText("Place Order");
            placeOrderButton.setIcon(null);

            // Mostramos diálogo de confirmación
            showOrderConfirmationDialog();

        } catch (Exception e) {
            // Restauramos el botón
            placeOrderButton.setEnabled(true);
            placeOrderButton.setText("Place Order");
            placeOrderButton.setIcon(null);

            log.error("Error processing alternative payment", e);
            Notification.show("Error processing payment: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void validatePaymentAndProceed(PaymentRequest paymentRequest) {
        try {
            // Show loading indicator while validating payment
            placeOrderButton.setEnabled(false);
            placeOrderButton.setText("Validating payment...");
            placeOrderButton.setIcon(new Icon(VaadinIcon.SPINNER));

            // Validate payment using payment service
            PaymentResponse paymentResponse = paymentController.validate(paymentRequest);

            // Restore button
            placeOrderButton.setEnabled(true);
            placeOrderButton.setText("Place Order");
            placeOrderButton.setIcon(null);

            if (paymentResponse != null && paymentResponse.getStatus() == PaymentStatus.ACCEPTED) {
                // Valid payment, show confirmation
                showOrderConfirmationDialog();
            } else {
                // Invalid payment, show error
                String errorMsg = "Payment information could not be validated";

                Notification.show("Payment validation error: " + errorMsg, 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (HttpClientErrorException e) {
            // Restore button
            placeOrderButton.setEnabled(true);
            placeOrderButton.setText("Place Order");
            placeOrderButton.setIcon(null);

            // Handle different HTTP error types
            String errorMessage;
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                errorMessage = "Invalid payment information";
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                errorMessage = "Not authorized to process this payment";
            } else {
                errorMessage = "Error validating payment: " + e.getMessage();
            }

            log.error("Error in payment validation: {}", errorMessage, e);
            Notification.show(errorMessage, 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);

        } catch (Exception e) {
            // Restore button
            placeOrderButton.setEnabled(true);
            placeOrderButton.setText("Place Order");
            placeOrderButton.setIcon(null);

            log.error("Error validating payment", e);
            Notification.show("Error validating payment: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void showOrderConfirmationDialog() {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Confirm Order");

        // Obtener el bean actualizado desde el binder
        Address address = addressBinder.getBean();

        // Validación adicional (por si acaso)
        if (address == null || address.getAddressLine1() == null || address.getCity() == null) {
            Notification.show("Address information is incomplete.", 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // Log de depuración
        log.info(
                "Confirming order to address: {}, {}, {}, {}, {}",
                address.getAddressLine1(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getCountry());

        // Construcción de la dirección formateada
        String fullAddress = String.format(
                "%s%s, %s, %s %s, %s",
                address.getAddressLine1(),
                address.getAddressLine2() != null && !address.getAddressLine2().isEmpty()
                        ? " " + address.getAddressLine2()
                        : "",
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getCountry());

        // Construir resumen de orden
        StringBuilder orderSummary = new StringBuilder();
        orderSummary
                .append("Total: ")
                .append(formatCurrency(cart.getTotalAmount()))
                .append("\n");
        orderSummary.append("Products: ").append(cart.getItems().size()).append("\n");
        orderSummary.append("Shipping to: ").append(fullAddress);

        // Mostrar resumen en el diálogo
        confirmDialog.setText(orderSummary.toString());

        confirmDialog.setCancelable(true);
        confirmDialog.setCancelText("Cancel");

        confirmDialog.setConfirmText("Confirm Order");
        confirmDialog.setConfirmButtonTheme("primary success");

        confirmDialog.addConfirmListener(event -> processOrder());

        confirmDialog.open();
    }



    private void restorePlaceOrderButton() {
        placeOrderButton.setEnabled(true);
        placeOrderButton.setText("Place Order");
        placeOrderButton.setIcon(null);
        placeOrderButton.getStyle().remove("cursor");
    }

    private void processOrder() {
        try {
            UI.getCurrent().setPollInterval(500);
            placeOrderButton.setEnabled(false);
            placeOrderButton.setText("Processing...");
            placeOrderButton.setIcon(new Icon(VaadinIcon.SPINNER));
            placeOrderButton.getStyle().set("cursor", "wait");

            Set<OrderItem> orderItems = cart.getItems().stream()
                    .map(item -> new OrderItem(item.getCode(), item.getName(), item.getPrice(), item.getQuantity()))
                    .collect(Collectors.toSet());

            Customer customer = new Customer(
                    nameField.getValue(),
                    emailField.getValue(),
                    phoneField.getValue()
            );

            // Escribimos los valores del formulario en el objeto Address
            Address address = new Address();
            if (!addressBinder.writeBeanIfValid(address)) {
                restorePlaceOrderButton();
                Notification.show("Please complete all required address fields", 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                UI.getCurrent().setPollInterval(-1);
                return;
            }

            log.info(
                    "Address for creating order - addressLine1: '{}', addressLine2: '{}', city: '{}', state: '{}', zipCode: '{}', country: '{}'",
                    address.getAddressLine1(),
                    address.getAddressLine2(),
                    address.getCity(),
                    address.getState(),
                    address.getZipCode(),
                    address.getCountry());

            CreateOrderRequest orderRequest = new CreateOrderRequest(orderItems, customer, address);

            OrderConfirmationDTO confirmation = orderController.createOrder(orderRequest);

            if (confirmation != null && confirmation.orderNumber() != null) {
                clearCart();
                Notification successNotification = Notification.show(
                        "Order processed successfully! Order number: " + confirmation.orderNumber(),
                        5000,
                        Notification.Position.MIDDLE);
                successNotification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate("order/" + confirmation.orderNumber());
            } else {
                restorePlaceOrderButton();
                Notification.show("Could not process the order, please try again", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            UI.getCurrent().setPollInterval(-1);

        } catch (RestClientException e) {
            restorePlaceOrderButton();
            log.error("Error processing order", e);
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Country is required")) {
                errorMessage = "Country is a required field. Please complete all address fields correctly.";
            }
            Notification.show("Error processing order: " + errorMessage, 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            UI.getCurrent().setPollInterval(-1);

        } catch (Exception e) {
            restorePlaceOrderButton();
            log.error("Unexpected error processing order", e);
            Notification.show("Unexpected error: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            UI.getCurrent().setPollInterval(-1);
        }
    }



    private void clearCart() {
        try {
            if (cartId != null) {
                cartController.removeCart(cartId);
                cartId = null;
                cart = new Cart();
            }
        } catch (Exception e) {
            log.error("Error clearing cart", e);
        }
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyFormat.format(amount);
    }

    private String getCartIdFromSession() {
        Object cartIdObj = UI.getCurrent().getSession().getAttribute("cartId");
        return cartIdObj != null ? cartIdObj.toString() : null;
    }

    private void addPhoneFieldEnterKeyListener() {
        phoneField
                .getElement()
                .addEventListener("keydown", event -> {
                    String key = event.getEventData().getString("event.key");
                    Double keyCode = event.getEventData().getNumber("event.keyCode");

                    boolean isEnterPressed = "Enter".equalsIgnoreCase(key) || keyCode == 13;

                    if (isEnterPressed) {
                        if (customerBinder.isValid()) {
                            addressTab.setEnabled(true);
                            stepTabs.setSelectedIndex(1);
                        } else {
                            showCustomerValidationErrors();
                        }
                    }
                })
                .addEventData("event.key")
                .addEventData("event.keyCode");
    }

    private void addCountryFieldEnterKeyListener() {
        countryField.getElement()
                .addEventListener("keypress", event -> {
                    String key = event.getEventData().getString("event.key");
                    double keyCode = event.getEventData().getNumber("event.keyCode");

                    if ("Enter".equalsIgnoreCase(key) || keyCode == 13) {
                        if (addressBinder.isValid()) {
                            paymentTab.setEnabled(true);
                            stepTabs.setSelectedIndex(2);
                        } else {
                            showAddressValidationErrors();
                        }
                    }
                })
                .addEventData("event.key")
                .addEventData("event.keyCode");
    }


    private void showCustomerValidationErrors() {
        StringBuilder errorMessage = new StringBuilder("Please complete customer information:\n");
        customerBinder.validate().getFieldValidationErrors().forEach(error -> errorMessage
                .append("- ")
                .append(error.getMessage())
                .append("\n"));

        Notification.show(errorMessage.toString(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showAddressValidationErrors() {
        StringBuilder errorMessage = new StringBuilder("Please complete delivery address:\n");
        addressBinder.validate().getFieldValidationErrors().forEach(error -> errorMessage
                .append("- ")
                .append(error.getMessage())
                .append("\n"));

        Notification.show(errorMessage.toString(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
