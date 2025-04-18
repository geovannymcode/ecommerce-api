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
        Notification.show("Functionality coming soon!", 3000, Notification.Position.BOTTOM_CENTER);
    }
}
