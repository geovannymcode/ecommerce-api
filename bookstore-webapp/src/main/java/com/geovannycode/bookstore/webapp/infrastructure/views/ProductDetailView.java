package com.geovannycode.bookstore.webapp.infrastructure.views;

import com.geovannycode.bookstore.webapp.domain.model.Cart;
import com.geovannycode.bookstore.webapp.domain.model.CartItemRequestDTO;
import com.geovannycode.bookstore.webapp.domain.model.Product;
import com.geovannycode.bookstore.webapp.domain.service.ProductService;
import com.geovannycode.bookstore.webapp.infrastructure.api.controller.CartController;
import com.geovannycode.bookstore.webapp.infrastructure.views.components.CartBadge;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

@Route(value = "product", layout = MainLayout.class)
@PageTitle("Product Detail")
public class ProductDetailView extends VerticalLayout implements HasUrlParameter<String> {

    private static final Logger log = LoggerFactory.getLogger(ProductDetailView.class);

    private final ProductService productService;
    private final CartController cartController;

    private final H2 title = new H2();
    private final Paragraph description = new Paragraph();
    private final Paragraph price = new Paragraph();
    private final Image productImage = new Image();
    private final Div errorMessage = new Div();

    private Product currentProduct;
    private String cartId;

    public ProductDetailView(@Autowired ProductService productService, @Autowired CartController cartController) {
        this.productService = productService;
        this.cartController = cartController;

        setMargin(true);
        setPadding(true);
        setSpacing(true);

        configureLayout();
    }

    private void configureLayout() {
        Div imageContainer = new Div(productImage);
        imageContainer.getStyle().set("margin", "0 auto");

        productImage.setMaxHeight("300px");

        VerticalLayout infoLayout = new VerticalLayout(title, price, description);
        infoLayout.setSpacing(true);
        infoLayout.setPadding(true);

        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addToCartButton.addClickListener(e -> addToCart());

        Button backButton = new Button("Back to Products");
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(ProductGridView.class)));

        HorizontalLayout actions = new HorizontalLayout(addToCartButton, backButton);
        actions.setSpacing(true);

        errorMessage.getStyle().set("color", "red");
        errorMessage.setVisible(false);

        HorizontalLayout detailLayout = new HorizontalLayout(imageContainer, infoLayout);
        detailLayout.setWidthFull();

        add(detailLayout, actions, errorMessage);
    }

    @Override
    public void setParameter(BeforeEvent event, String productCode) {
        try {
            currentProduct = productService.getProductByCode(productCode);
            displayProduct(currentProduct);

            // Get cart ID from session
            cartId = getCartIdFromSession();
        } catch (ResponseStatusException e) {
            showError("Product not found: " + productCode);
        } catch (Exception e) {
            log.error("Error loading product", e);
            showError("Error loading product: " + e.getMessage());
        }
    }

    private void displayProduct(Product product) {
        title.setText(product.name());
        description.setText(product.description());

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        price.setText("Price: " + currencyFormat.format(product.price()));
        price.getStyle().set("font-weight", "bold");

        if (product.imageUrl() != null && !product.imageUrl().isEmpty()) {
            productImage.setSrc(product.imageUrl());
            productImage.setAlt(product.name());
            productImage.setVisible(true);
        } else {
            productImage.setSrc("images/books.png");
            productImage.setAlt(product.name());
            productImage.setVisible(true);
        }

        errorMessage.setVisible(false);
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);

        title.setVisible(false);
        description.setVisible(false);
        price.setVisible(false);
        productImage.setVisible(false);
    }

    private void addToCart() {
        try {
            if (currentProduct == null) {
                Notification.show("No product to add to cart!", 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Create request object
            CartItemRequestDTO request = new CartItemRequestDTO();
            request.setCode(currentProduct.code());
            request.setQuantity(1);

            // Call the cart controller
            Cart cart = cartController.addToCart(cartId, request);

            if (cart != null) {
                // Get the updated cart ID if it's a new cart
                if (cartId == null && cart.getId() != null) {
                    cartId = cart.getId();
                    storeCartIdInSession(cartId);
                }

                // Update the cart badge count in the main layout
                updateCartBadge();

                // Show success notification
                Notification.show("Added to cart: " + currentProduct.name(), 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                // Show error notification
                Notification.show("Failed to add to cart", 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            log.error("Error adding product to cart", e);
            Notification.show("Error: " + e.getMessage(), 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateCartBadge() {
        // Get cart from the controller
        try {
            var cart = cartController.getCart(cartId).getBody();
            if (cart != null && cart.getItems() != null) {
                // Update cart badge in main layout
                Optional<MainLayout> mainLayout = getParentLayout();
                if (mainLayout.isPresent()) {
                    CartBadge cartBadge = mainLayout.get().getCartBadge();
                    cartBadge.updateCount(cart.getItems().size());
                }
            }
        } catch (Exception e) {
            log.error("Error updating cart badge", e);
        }
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
