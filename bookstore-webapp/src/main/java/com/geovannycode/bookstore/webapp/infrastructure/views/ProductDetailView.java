package com.geovannycode.bookstore.webapp.infrastructure.views;

import com.geovannycode.bookstore.webapp.domain.model.Product;
import com.geovannycode.bookstore.webapp.domain.service.ProductService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.web.server.ResponseStatusException;

@Route(value = "product", layout = MainLayout.class)
@PageTitle("Product Detail")
public class ProductDetailView extends VerticalLayout implements HasUrlParameter<String> {

    private final ProductService productService;

    // UI components
    private final H2 title = new H2();
    private final Paragraph description = new Paragraph();
    private final Paragraph price = new Paragraph();
    private final Image productImage = new Image();
    private final Div errorMessage = new Div();

    public ProductDetailView(ProductService productService) {
        this.productService = productService;

        setMargin(true);
        setPadding(true);
        setSpacing(true);

        // Configure the layout
        configureLayout();
    }

    private void configureLayout() {
        // Image container
        Div imageContainer = new Div(productImage);
        imageContainer.getStyle().set("margin", "0 auto");

        productImage.setMaxHeight("300px");

        // Product information
        VerticalLayout infoLayout = new VerticalLayout(title, price, description);
        infoLayout.setSpacing(true);
        infoLayout.setPadding(true);

        // Button to add to cart
        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.addClickListener(e -> addToCart());

        // Button to go back
        Button backButton = new Button("Back to Products");
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(ProductGridView.class)));

        HorizontalLayout actions = new HorizontalLayout(addToCartButton, backButton);
        actions.setSpacing(true);

        // Error message container (initially hidden)
        errorMessage.getStyle().set("color", "red");
        errorMessage.setVisible(false);

        // Main detail layout
        HorizontalLayout detailLayout = new HorizontalLayout(imageContainer, infoLayout);
        detailLayout.setWidthFull();

        add(detailLayout, actions, errorMessage);
    }

    @Override
    public void setParameter(BeforeEvent event, String productCode) {
        try {
            // Usar ProductService
            Product product = productService.getProductByCode(productCode);
            displayProduct(product);
        } catch (ResponseStatusException e) {
            showError("Product not found: " + productCode);
        } catch (Exception e) {
            showError("Error loading product: " + e.getMessage());
        }
    }

    private void displayProduct(Product product) {
        title.setText(product.name());
        description.setText(product.description());

        // Format price nicely
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        price.setText("Price: " + currencyFormat.format(product.price()));
        price.getStyle().set("font-weight", "bold");

        // Set product image if available
        if (product.imageUrl() != null && !product.imageUrl().isEmpty()) {
            productImage.setSrc(product.imageUrl());
            productImage.setAlt(product.name());
            productImage.setVisible(true);
        } else {
            // Default image
            productImage.setSrc("images/books.png");
            productImage.setAlt(product.name());
            productImage.setVisible(true);
        }

        // Hide error message
        errorMessage.setVisible(false);
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);

        // Hide product details
        title.setVisible(false);
        description.setVisible(false);
        price.setVisible(false);
        productImage.setVisible(false);
    }

    private void addToCart() {
        // Esta funcionalidad se implementará más adelante
        Notification.show("Functionality coming soon!", 3000, Notification.Position.BOTTOM_CENTER);
    }
}
