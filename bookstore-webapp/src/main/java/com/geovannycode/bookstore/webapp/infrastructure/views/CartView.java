package com.geovannycode.bookstore.webapp.infrastructure.views;

import com.geovannycode.bookstore.webapp.domain.model.Cart;
import com.geovannycode.bookstore.webapp.domain.model.CartItem;
import com.geovannycode.bookstore.webapp.domain.model.CartItemRequestDTO;
import com.geovannycode.bookstore.webapp.infrastructure.api.controller.CartController;
import com.geovannycode.bookstore.webapp.infrastructure.views.components.CardComponent;
import com.geovannycode.bookstore.webapp.infrastructure.views.components.CartBadge;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.math.BigDecimal;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "cart", layout = MainLayout.class)
@PageTitle("Shopping Cart")
public class CartView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(CartView.class);

    private final CartController cartController;

    private String cartId;
    private Cart cart;

    private final VerticalLayout cartListLayout = new VerticalLayout();
    private final Span subtotalLabel = new Span("Subtotal (0 productos): $0.00");
    // private final Span totalLabel = new Span("Total: $0.00");
    private final Span bottomTotalLabel = new Span("Total: ");

    public CartView(@Autowired CartController cartController) {
        this.cartController = cartController;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 viewTitle = new H2("Shopping Cart");
        viewTitle.getStyle().set("margin-top", "0");

        // Main section with items
        VerticalLayout cartLayout = new VerticalLayout(viewTitle, cartListLayout);
        cartLayout.setPadding(false);
        cartLayout.setSpacing(true);
        cartLayout.setWidthFull();

        // Create bottom total layout
        HorizontalLayout bottomTotalLayout = new HorizontalLayout();
        bottomTotalLayout.setWidthFull();
        bottomTotalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        bottomTotalLayout.add(bottomTotalLabel);
        bottomTotalLabel
                .getStyle()
                .set("font-size", "18px")
                .set("font-weight", "bold")
                // .set("color", "#B12704")
                .set("margin-right", "20px")
                .set("margin-top", "20px");

        // Add bottom total to cart layout
        cartLayout.add(bottomTotalLayout);

        // Create the right sidebar with subtotal and checkout button
        VerticalLayout sidebarLayout = createSidebarLayout();
        sidebarLayout
                .getStyle()
                .set("border", "1px solid #E0E0E0")
                .set("padding", "20px")
                .set("border-radius", "8px")
                .set("background-color", "#f9f9f9");

        // Create main horizontal layout to hold cart and sidebar
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidthFull();
        mainLayout.setAlignItems(FlexComponent.Alignment.START);

        // Create cards for the two components
        CardComponent cartCard = new CardComponent();
        cartCard.add(cartLayout);
        cartCard.getStyle().set("flex", "3");

        Div sidebarCard = new Div();
        sidebarCard.add(sidebarLayout);
        sidebarCard.getStyle().set("flex", "1").set("margin-left", "20px").set("align-self", "flex-start");

        mainLayout.add(cartCard, sidebarCard);
        mainLayout.setFlexGrow(3, cartCard);
        mainLayout.setFlexGrow(1, sidebarCard);

        // Continue Shopping button placed at the left bottom
        Button continueShoppingButton = new Button("Continue Shopping");
        continueShoppingButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        continueShoppingButton
                .getStyle()
                .set("border-radius", "20px")
                .set("padding", "10px 20px")
                .set("margin-top", "20px");
        continueShoppingButton.addClickListener(e -> UI.getCurrent().navigate(ProductGridView.class));

        HorizontalLayout bottomButtonLayout = new HorizontalLayout(continueShoppingButton);
        bottomButtonLayout.setWidthFull();
        bottomButtonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        add(mainLayout, bottomButtonLayout);
        loadCart();
    }

    private VerticalLayout createSidebarLayout() {
        // Subtotal label - estilo Amazon
        subtotalLabel
                .getStyle()
                .set("font-size", "18px")
                .set("font-weight", "bold")
                .set("color", "#B12704")
                .set("margin-bottom", "10px")
                .set("white-space", "nowrap");

        // Total label (moved to sidebar) - estilo Amazon
        /*totalLabel.getStyle()
                        .set("font-size", "16px")
                        .set("font-weight", "bold")
                        .set("margin-bottom", "20px")
                        .set("padding", "10px 0")
                        .set("border-top", "1px solid #DDD")
                        .set("border-bottom", "1px solid #DDD");
        */
        // Checkout button
        Button proceedToCheckoutButton = new Button("Proceed to Checkout");
        proceedToCheckoutButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        proceedToCheckoutButton
                .getStyle()
                .set("border-radius", "20px")
                .set("padding", "12px 25px")
                .set("font-weight", "bold")
                .set("background-color", "#1E88E5")
                .set("width", "100%")
                .set("margin-top", "10px");
        proceedToCheckoutButton.setIcon(new Icon(VaadinIcon.ARROW_RIGHT));
        proceedToCheckoutButton.setIconAfterText(true);
        proceedToCheckoutButton.addClickListener(e -> UI.getCurrent().navigate(CheckoutView.class));

        VerticalLayout layout = new VerticalLayout(subtotalLabel, /*totalLabel,*/ proceedToCheckoutButton);
        layout.setPadding(false);
        layout.setSpacing(false); // Para que los elementos estén más juntos como en Amazon

        return layout;
    }

    private void loadCart() {
        cartId = getCartIdFromSession();
        cart = cartController.getCart(cartId).getBody();
        cartListLayout.removeAll();

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            showEmptyCartMessage();
            return;
        }

        for (CartItem item : cart.getItems()) {
            HorizontalLayout itemLayout = new HorizontalLayout();
            itemLayout.setAlignItems(Alignment.CENTER);
            itemLayout.setWidthFull();
            itemLayout.getStyle().set("border-bottom", "1px solid #e0e0e0").set("padding", "10px 0");

            Checkbox select = new Checkbox();
            select.setValue(true);
            select.getStyle().set("margin-right", "10px");

            Component imageComponent;
            String imageUrl = item.getImageUrl();

            if (imageUrl != null && !imageUrl.isBlank()) {
                Image image = new Image(imageUrl, item.getName());
                image.setWidth("80px");
                image.setHeight("auto");
                image.getStyle().set("margin-right", "15px");
                imageComponent = image;
            } else {
                Span placeholder = new Span("Imagen no disponible");
                placeholder
                        .getStyle()
                        .set("width", "80px")
                        .set("height", "100px")
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "center")
                        .set("background-color", "#f0f0f0")
                        .set("border-radius", "4px")
                        .set("font-size", "10px")
                        .set("text-align", "center")
                        .set("margin-right", "15px");
                imageComponent = placeholder;
            }

            VerticalLayout details = new VerticalLayout();
            details.setSpacing(false);
            details.setPadding(false);

            // Title with proper styling
            Span titleSpan = new Span(item.getName());
            titleSpan
                    .getStyle()
                    .set("font-weight", "bold")
                    .set("font-size", "16px")
                    .set("margin-bottom", "5px");

            // Price with proper styling
            Span priceSpan = new Span("Price: $" + item.getPrice());
            priceSpan
                    .getStyle()
                    .set("font-weight", "bold")
                    .set("color", "#B12704")
                    .set("font-size", "14px");

            details.add(titleSpan, priceSpan);
            details.getStyle().set("flex-grow", "1");

            // Quantity controls styled similar to Amazon
            HorizontalLayout quantityLayout = new HorizontalLayout();
            quantityLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            quantityLayout.getStyle().set("margin-right", "20px");

            // Declare the NumberField before using it
            NumberField quantityField = new NumberField();
            quantityField.setValue((double) item.getQuantity());
            quantityField.setMin(1);
            quantityField.setStep(1);
            quantityField.setWidth("50px");
            quantityField
                    .getStyle()
                    .set("text-align", "center")
                    .set("border-radius", "0")
                    .set("margin", "0");

            Button decrement = new Button("-", e -> {
                Double current = quantityField.getValue();
                if (current != null && current > 1) {
                    quantityField.setValue(current - 1);
                }
            });
            decrement
                    .getStyle()
                    .set("min-width", "30px")
                    .set("width", "30px")
                    .set("height", "30px")
                    .set("padding", "0")
                    .set("border-radius", "4px 0 0 4px")
                    .set("background-color", "#f0f0f0");

            Button increment = new Button("+", e -> {
                Double current = quantityField.getValue();
                if (current != null) {
                    quantityField.setValue(current + 1);
                }
            });
            increment
                    .getStyle()
                    .set("min-width", "30px")
                    .set("width", "30px")
                    .set("height", "30px")
                    .set("padding", "0")
                    .set("border-radius", "0 4px 4px 0")
                    .set("background-color", "#f0f0f0");

            quantityField.addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    int value = event.getValue().intValue();
                    CartItemRequestDTO request = new CartItemRequestDTO(item.getCode(), value);
                    cartController.updateCartItemQuantity(cartId, request);
                    loadCart();
                }
            });

            quantityLayout.add(decrement, quantityField, increment);

            // Delete button styled like Amazon
            Button delete = new Button(VaadinIcon.TRASH.create(), e -> {
                cartController.removeCartItem(cartId, item.getCode());
                loadCart();
            });
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            delete.getStyle()
                    .set("min-width", "40px")
                    .set("width", "40px")
                    .set("height", "40px")
                    .set("border-radius", "50%")
                    .set("padding", "0");

            itemLayout.add(select, imageComponent, details, quantityLayout, delete);
            cartListLayout.add(itemLayout);
        }

        // Calculate total
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Update subtotal, total and bottom total labels
        int itemCount = cart.getItems().size();
        String itemCountText = itemCount == 1 ? "1 producto" : itemCount + " productos";

        // Formato exacto como en Amazon
        subtotalLabel.setText("Subtotal (" + itemCount + " productos): $" + total);
        // totalLabel.setText("Total: $" + total);
        bottomTotalLabel.setText("Total: $" + total);
    }

    private void showEmptyCartMessage() {
        Span emptyMessage = new Span("Your cart is empty");
        emptyMessage
                .getStyle()
                .set("font-size", "1.2em")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("padding", "2em")
                .set("text-align", "center")
                .set("width", "100%");

        cartListLayout.add(emptyMessage);
        subtotalLabel.setText("Subtotal (0 productos): $0.00");
        // totalLabel.setText("Total: $0.00");
        bottomTotalLabel.setText("Total: $0.00");

        Notification.show("Your cart is empty. Continue shopping to add items.", 3000, Notification.Position.MIDDLE);
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

                loadCart();
                Notification.show("Cart cleared", 2000, Notification.Position.MIDDLE);
            }
        } catch (Exception e) {
            log.error("Error clearing cart", e);
            Notification.show("Failed to clear cart: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private String getCartIdFromSession() {
        Object cartIdObj = UI.getCurrent().getSession().getAttribute("cartId");
        return cartIdObj != null ? cartIdObj.toString() : null;
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
