package com.geovannycode.bookstore.webapp.infrastructure.views;

import com.geovannycode.bookstore.webapp.domain.model.Address;
import com.geovannycode.bookstore.webapp.domain.model.Cart;
import com.geovannycode.bookstore.webapp.domain.model.CreateOrderRequest;
import com.geovannycode.bookstore.webapp.domain.model.Customer;
import com.geovannycode.bookstore.webapp.domain.model.OrderConfirmationDTO;
import com.geovannycode.bookstore.webapp.domain.model.OrderItem;
import com.geovannycode.bookstore.webapp.infrastructure.api.controller.CartController;
import com.geovannycode.bookstore.webapp.infrastructure.api.controller.OrderController;
import com.geovannycode.bookstore.webapp.infrastructure.views.components.CardComponent;
import com.geovannycode.bookstore.webapp.infrastructure.views.components.CartBadge;
import com.geovannycode.bookstore.webapp.infrastructure.views.components.PaymentMethodSelector;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
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

@Route(value = "checkout", layout = MainLayout.class)
@PageTitle("Finalizar Compra")
public class CheckoutView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(CheckoutView.class);

    private final CartController cartController;
    private final OrderController orderController;

    private String cartId;
    private Cart cart;

    // Binders para los formularios
    private final Binder<Customer> customerBinder = new Binder<>(Customer.class);
    private final Binder<Address> addressBinder = new Binder<>(Address.class);

    // Componentes de la interfaz
    private Tabs checkoutTabs;
    private Div stepContainers;
    private Div customerInfoContainer;
    private Div deliveryAddressContainer;
    private Div paymentMethodContainer;
    private Button previousButton;
    private Button nextButton;

    // Paso actual del checkout (0-2)
    private int currentStep = 0;

    // Método de pago seleccionado
    private String selectedPaymentMethod;

    public CheckoutView(@Autowired CartController cartController, @Autowired OrderController orderController) {
        this.cartController = cartController;
        this.orderController = orderController;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Título de la página
        H2 pageTitle = new H2("Finalizar Compra");
        pageTitle.getStyle().set("margin-top", "0");
        add(pageTitle);

        // Inicializar componentes
        initializeTabs();
        initializeStepContainers();
        initializeNavigationButtons();

        // Cargar carrito
        cartId = getCartIdFromSession();
        loadCart();

        // Inicializar paso actual
        updateVisibleStep();
    }

    /**
     * Inicializa las pestañas para los pasos del checkout.
     */
    private void initializeTabs() {
        Tab customerTab = new Tab("1. Información del Cliente");
        Tab deliveryTab = new Tab("2. Dirección de Entrega");
        Tab paymentTab = new Tab("3. Método de Pago");

        checkoutTabs = new Tabs(customerTab, deliveryTab, paymentTab);
        checkoutTabs.setWidthFull();

        // No permitir cambio directo de pestañas
        checkoutTabs.addSelectedChangeListener(event -> {
            if (event.isFromClient()) {
                checkoutTabs.setSelectedIndex(currentStep);
            }
        });

        // Agregar pestañas a la vista dentro de una tarjeta
        CardComponent tabsCard = new CardComponent();
        tabsCard.add(checkoutTabs);
        add(tabsCard);
    }

    /**
     * Inicializa los contenedores para cada paso.
     */
    private void initializeStepContainers() {
        stepContainers = new Div();
        stepContainers.setWidthFull();

        // Contenedor para información del cliente
        customerInfoContainer = new Div();
        customerInfoContainer.setWidthFull();
        populateCustomerInfoContainer();

        // Contenedor para dirección de entrega
        deliveryAddressContainer = new Div();
        deliveryAddressContainer.setWidthFull();
        deliveryAddressContainer.setVisible(false);
        populateDeliveryAddressContainer();

        // Contenedor para método de pago
        paymentMethodContainer = new Div();
        paymentMethodContainer.setWidthFull();
        paymentMethodContainer.setVisible(false);
        populatePaymentMethodContainer();

        // Agregar contenedores al contenedor principal
        stepContainers.add(customerInfoContainer, deliveryAddressContainer, paymentMethodContainer);

        // Agregar contenedor principal a una tarjeta
        CardComponent stepsCard = new CardComponent();
        stepsCard.add(stepContainers);
        add(stepsCard);
    }

    /**
     * Inicializa los botones de navegación.
     */
    private void initializeNavigationButtons() {
        previousButton = new Button("Anterior");
        previousButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        previousButton.addClickListener(e -> navigateToPreviousStep());
        previousButton.setEnabled(false);

        nextButton = new Button("Siguiente");
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nextButton.addClickListener(e -> navigateToNextStep());

        HorizontalLayout navigationButtons = new HorizontalLayout(previousButton, nextButton);
        navigationButtons.setWidthFull();
        navigationButtons.setJustifyContentMode(JustifyContentMode.BETWEEN);
        navigationButtons.setPadding(true);

        CardComponent navigationCard = new CardComponent();
        navigationCard.add(navigationButtons);
        add(navigationCard);
    }

    /**
     * Puebla el contenedor de información del cliente.
     */
    private void populateCustomerInfoContainer() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        H3 title = new H3("Información del Cliente");
        content.add(title);

        // Formulario para información del cliente
        FormLayout customerForm = new FormLayout();
        customerForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

        TextField nameField = new TextField("Nombre Completo");
        nameField.setRequired(true);

        EmailField emailField = new EmailField("Email");
        emailField.setRequired(true);

        TextField phoneField = new TextField("Teléfono");
        phoneField.setRequired(true);

        // Configurar validaciones y bindings
        customerBinder.forField(nameField).asRequired("El nombre es requerido").bind(Customer::name, (c, v) -> {});

        customerBinder.forField(emailField).asRequired("El email es requerido").bind(Customer::email, (c, v) -> {});

        customerBinder
                .forField(phoneField)
                .asRequired("El teléfono es requerido")
                .bind(Customer::phone, (c, v) -> {});

        customerForm.add(nameField, emailField, phoneField);
        content.add(customerForm);

        customerInfoContainer.add(content);
    }

    /**
     * Puebla el contenedor de dirección de entrega.
     */
    private void populateDeliveryAddressContainer() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        H3 title = new H3("Dirección de Entrega");
        content.add(title);

        // Formulario para dirección de entrega
        FormLayout addressForm = new FormLayout();
        addressForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("900px", 3));

        TextField addressLine1Field = new TextField("Dirección");
        addressLine1Field.setRequired(true);

        TextField addressLine2Field = new TextField("Complemento");

        TextField cityField = new TextField("Ciudad");
        cityField.setRequired(true);

        TextField stateField = new TextField("Estado/Provincia");
        stateField.setRequired(true);

        TextField zipCodeField = new TextField("Código Postal");
        zipCodeField.setRequired(true);

        TextField countryField = new TextField("País");
        countryField.setRequired(true);

        // Configurar validaciones y bindings
        addressBinder
                .forField(addressLine1Field)
                .asRequired("La dirección es requerida")
                .bind(Address::addressLine1, (a, v) -> {});

        addressBinder.forField(cityField).asRequired("La ciudad es requerida").bind(Address::city, (a, v) -> {});

        addressBinder
                .forField(stateField)
                .asRequired("El estado/provincia es requerido")
                .bind(Address::state, (a, v) -> {});

        addressBinder
                .forField(zipCodeField)
                .asRequired("El código postal es requerido")
                .bind(Address::zipCode, (a, v) -> {});

        addressBinder.forField(countryField).asRequired("El país es requerido").bind(Address::country, (a, v) -> {});

        addressBinder.forField(addressLine2Field).bind(Address::addressLine2, (a, v) -> {});

        addressForm.add(addressLine1Field, addressLine2Field, cityField, stateField, zipCodeField, countryField);

        // Configurar colspan para campos largos
        addressForm.setColspan(addressLine1Field, 2);
        addressForm.setColspan(addressLine2Field, 2);

        content.add(addressForm);

        deliveryAddressContainer.add(content);
    }

    /**
     * Puebla el contenedor de método de pago.
     */
    private void populatePaymentMethodContainer() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        // Crear el selector de métodos de pago
        PaymentMethodSelector paymentSelector = new PaymentMethodSelector();

        // Agregar listener para la selección
        paymentSelector.setPaymentSelectionListener(paymentType -> {
            selectedPaymentMethod = paymentType;
            log.info("Método de pago seleccionado: {}", paymentType);

            // Habilitar botón siguiente si se ha seleccionado un método de pago
            nextButton.setEnabled(true);
            nextButton.setText("Finalizar Compra");
        });

        content.add(paymentSelector);

        // Resumen del pedido
        content.add(createOrderSummary());

        paymentMethodContainer.add(content);
    }

    /**
     * Crea el resumen del pedido.
     *
     * @return Componente con el resumen
     */
    private Div createOrderSummary() {
        Div summaryContainer = new Div();
        summaryContainer
                .getStyle()
                .set("margin-top", "2rem")
                .set("padding", "1rem")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)");

        H3 title = new H3("Resumen del Pedido");
        title.getStyle().set("margin-top", "0");

        summaryContainer.add(title);

        // Si no hay carrito, mostrar mensaje
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            Span emptyCart = new Span("No hay productos en el carrito");
            summaryContainer.add(emptyCart);
            return summaryContainer;
        }

        // Mostrar productos
        Div productsContainer = new Div();
        cart.getItems().forEach(item -> {
            HorizontalLayout itemRow = new HorizontalLayout();
            itemRow.setWidthFull();
            itemRow.setJustifyContentMode(JustifyContentMode.BETWEEN);

            Span itemName = new Span(item.getName() + " x" + item.getQuantity());
            Span itemPrice = new Span(formatCurrency(item.getPrice().multiply(new BigDecimal(item.getQuantity()))));

            itemRow.add(itemName, itemPrice);
            productsContainer.add(itemRow);
        });

        summaryContainer.add(productsContainer);

        // Mostrar total
        HorizontalLayout totalRow = new HorizontalLayout();
        totalRow.setWidthFull();
        totalRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        totalRow.getStyle()
                .set("margin-top", "1rem")
                .set("font-weight", "bold")
                .set("border-top", "1px solid var(--lumo-contrast-10pct)")
                .set("padding-top", "1rem");

        Span totalLabel = new Span("Total:");
        Span totalAmount = new Span(formatCurrency(cart.getTotalAmount()));

        totalRow.add(totalLabel, totalAmount);
        summaryContainer.add(totalRow);

        return summaryContainer;
    }

    /**
     * Navega al paso anterior.
     */
    private void navigateToPreviousStep() {
        if (currentStep > 0) {
            currentStep--;
            updateVisibleStep();
        }
    }

    /**
     * Navega al paso siguiente.
     */
    private void navigateToNextStep() {
        // Validar paso actual
        if (validateCurrentStep()) {
            if (currentStep < 2) {
                // Avanzar al siguiente paso
                currentStep++;
                updateVisibleStep();
            } else {
                // Estamos en el último paso, procesar la orden
                processOrder();
            }
        }
    }

    /**
     * Valida el paso actual.
     *
     * @return true si la validación es exitosa
     */
    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 0: // Información del cliente
                if (!customerBinder.isValid()) {
                    Notification.show(
                                    "Por favor completa la información del cliente correctamente",
                                    3000,
                                    Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return false;
                }
                return true;

            case 1: // Dirección de entrega
                if (!addressBinder.isValid()) {
                    Notification.show(
                                    "Por favor completa la dirección de entrega correctamente",
                                    3000,
                                    Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return false;
                }
                return true;

            case 2: // Método de pago
                if (selectedPaymentMethod == null) {
                    Notification.show("Por favor selecciona un método de pago", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return false;
                }
                return true;

            default:
                return false;
        }
    }

    /**
     * Actualiza la visibilidad de los contenedores según el paso actual.
     */
    private void updateVisibleStep() {
        // Actualizar pestaña seleccionada
        checkoutTabs.setSelectedIndex(currentStep);

        // Actualizar visibilidad de contenedores
        customerInfoContainer.setVisible(currentStep == 0);
        deliveryAddressContainer.setVisible(currentStep == 1);
        paymentMethodContainer.setVisible(currentStep == 2);

        // Actualizar botones
        previousButton.setEnabled(currentStep > 0);

        if (currentStep == 2 && selectedPaymentMethod != null) {
            nextButton.setText("Finalizar Compra");
        } else {
            nextButton.setText("Siguiente");
        }
    }

    /**
     * Carga el carrito.
     */
    private void loadCart() {
        try {
            if (cartId != null) {
                cart = cartController.getCart(cartId).getBody();

                if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                    // Carrito vacío, redirigir a la vista de productos
                    Notification.show(
                            "El carrito está vacío. Añade productos para continuar.",
                            3000,
                            Notification.Position.MIDDLE);
                    UI.getCurrent().navigate(ProductGridView.class);
                }
            } else {
                // No hay carrito, redirigir a la vista de productos
                Notification.show("No se encontró un carrito activo.", 3000, Notification.Position.MIDDLE);
                UI.getCurrent().navigate(ProductGridView.class);
            }
        } catch (Exception e) {
            log.error("Error al cargar el carrito", e);
            Notification.show("Error al cargar el carrito: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            UI.getCurrent().navigate(ProductGridView.class);
        }
    }

    /**
     * Procesa la orden.
     */
    private void processOrder() {
        try {
            // Mostrar diálogo de confirmación
            showConfirmationDialog();
        } catch (Exception e) {
            log.error("Error al procesar la orden", e);
            Notification.show("Error al procesar la orden: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Muestra un diálogo de confirmación antes de procesar la orden.
     */
    private void showConfirmationDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmar Pedido");

        // Crear mensaje de confirmación
        StringBuilder message = new StringBuilder();
        message.append("¿Estás seguro de que deseas finalizar la compra?");
        message.append("\n\nTotal: ").append(formatCurrency(cart.getTotalAmount()));
        message.append("\nMétodo de pago: ").append(selectedPaymentMethod);

        dialog.setText(message.toString());

        // Botones
        dialog.setCancelable(true);
        dialog.setCancelText("Cancelar");

        dialog.setConfirmText("Confirmar");
        dialog.setConfirmButtonTheme("primary success");

        // Acción de confirmación
        dialog.addConfirmListener(event -> finalizeOrder());

        dialog.open();
    }

    /**
     * Finaliza la orden.
     */
    private void finalizeOrder() {
        try {
            // Deshabilitar botones para evitar doble envío
            previousButton.setEnabled(false);
            nextButton.setEnabled(false);
            nextButton.setText("Procesando...");

            // Extraer datos del formulario
            Customer customer = extractCustomerData();
            Address address = extractAddressData();

            // Convertir items del carrito a items de orden
            Set<OrderItem> orderItems = cart.getItems().stream()
                    .map(item -> new OrderItem(item.getCode(), item.getName(), item.getPrice(), item.getQuantity()))
                    .collect(Collectors.toSet());

            // Crear solicitud de orden
            CreateOrderRequest orderRequest = new CreateOrderRequest(orderItems, customer, address);

            // Enviar solicitud al controlador
            OrderConfirmationDTO confirmation = orderController.createOrder(orderRequest);

            if (confirmation != null && confirmation.orderNumber() != null) {
                // Limpiar el carrito
                clearCart();

                // Mostrar confirmación
                Notification notification = Notification.show(
                        "¡Orden procesada con éxito! Número de orden: " + confirmation.orderNumber(),
                        5000,
                        Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Navegar a la vista de confirmación
                UI.getCurrent().navigate(OrderConfirmationView.class, confirmation.orderNumber());
            } else {
                // Restaurar botones
                previousButton.setEnabled(true);
                nextButton.setEnabled(true);
                nextButton.setText("Finalizar Compra");

                Notification.show(
                                "No se pudo procesar la orden, por favor intenta nuevamente",
                                3000,
                                Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            log.error("Error al finalizar la orden", e);

            // Restaurar botones
            previousButton.setEnabled(true);
            nextButton.setEnabled(true);
            nextButton.setText("Finalizar Compra");

            Notification.show("Error al procesar la orden: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Extrae los datos del cliente desde los campos del formulario.
     *
     * @return Objeto Customer con los datos
     */
    private Customer extractCustomerData() {
        // Nombres y valores de los campos
        String name = ((TextField) customerBinder.getFields().findFirst().orElse(null)).getValue();
        String email =
                ((EmailField) customerBinder.getFields().skip(1).findFirst().orElse(null)).getValue();
        String phone =
                ((TextField) customerBinder.getFields().skip(2).findFirst().orElse(null)).getValue();

        return new Customer(name, email, phone);
    }

    /**
     * Extrae los datos de dirección desde los campos del formulario.
     *
     * @return Objeto Address con los datos
     */
    private Address extractAddressData() {
        // Obtener referencias a los campos
        TextField addressLine1Field =
                (TextField) addressBinder.getFields().findFirst().orElse(null);
        TextField addressLine2Field =
                (TextField) addressBinder.getFields().skip(1).findFirst().orElse(null);
        TextField cityField =
                (TextField) addressBinder.getFields().skip(2).findFirst().orElse(null);
        TextField stateField =
                (TextField) addressBinder.getFields().skip(3).findFirst().orElse(null);
        TextField zipCodeField =
                (TextField) addressBinder.getFields().skip(4).findFirst().orElse(null);
        TextField countryField =
                (TextField) addressBinder.getFields().skip(5).findFirst().orElse(null);

        // Obtener valores
        String addressLine1 = addressLine1Field != null ? addressLine1Field.getValue() : "";
        String addressLine2 = addressLine2Field != null ? addressLine2Field.getValue() : "";
        String city = cityField != null ? cityField.getValue() : "";
        String state = stateField != null ? stateField.getValue() : "";
        String zipCode = zipCodeField != null ? zipCodeField.getValue() : "";
        String country = countryField != null ? countryField.getValue() : "";

        return new Address(addressLine1, addressLine2, city, state, zipCode, country);
    }

    /**
     * Limpia el carrito.
     */
    private void clearCart() {
        try {
            if (cartId != null) {
                cartController.removeCart(cartId);

                // Actualizar badge del carrito en el layout principal
                Optional<MainLayout> mainLayout = getParentLayout();
                if (mainLayout.isPresent()) {
                    CartBadge cartBadge = mainLayout.get().getCartBadge();
                    cartBadge.updateCount(0);
                }

                cartId = null;
                cart = null;
            }
        } catch (Exception e) {
            log.error("Error al limpiar el carrito", e);
        }
    }

    /**
     * Formatea un valor como moneda.
     *
     * @param amount Monto a formatear
     * @return Cadena formateada como moneda
     */
    private String formatCurrency(BigDecimal amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyFormat.format(amount != null ? amount : BigDecimal.ZERO);
    }

    /**
     * Obtiene el ID del carrito desde la sesión.
     *
     * @return ID del carrito o null si no existe
     */
    private String getCartIdFromSession() {
        Object cartIdObj = UI.getCurrent().getSession().getAttribute("cartId");
        return cartIdObj != null ? cartIdObj.toString() : null;
    }

    /**
     * Obtiene el layout padre (MainLayout).
     *
     * @return Optional con el MainLayout
     */
    private Optional<MainLayout> getParentLayout() {
        return Optional.ofNullable(UI.getCurrent()
                .getChildren()
                .filter(component -> component instanceof MainLayout)
                .findFirst()
                .map(component -> (MainLayout) component)
                .orElse(null));
    }
}
