package com.geovannycode.bookstore.webapp.infrastructure.views;

import com.geovannycode.bookstore.webapp.domain.model.PagedResult;
import com.geovannycode.bookstore.webapp.domain.model.Product;
import com.geovannycode.bookstore.webapp.domain.service.ProductService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;

@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "products", layout = MainLayout.class)
@PageTitle("Bookstore")
public class ProductGridView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(ProductGridView.class);
    private final ProductService productService;
    private final FlexLayout booksContainer;
    private final VerticalLayout errorView;
    private final HorizontalLayout paginationLayout;
    private final Span pageInfoLabel;
    private int currentPage = 0;
    private int totalPages = 0;
    private Long totalElements;
    private Button prevButton;
    private Button nextButton;

    public ProductGridView(ProductService productService) {
        this.productService = productService;

        setPadding(true);
        setSpacing(true);
        setSizeFull();

        booksContainer = new FlexLayout();
        booksContainer.setWidthFull();
        booksContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        booksContainer.setJustifyContentMode(JustifyContentMode.CENTER);
        booksContainer.getStyle().set("gap", "20px");

        errorView = new VerticalLayout();
        errorView.setAlignItems(Alignment.CENTER);
        errorView.setJustifyContentMode(JustifyContentMode.CENTER);
        errorView.setVisible(false);

        H3 errorTitle = new H3("No se pudieron cargar los productos");
        Span errorMessage = new Span("Parece que hay un problema de conexión con el servicio de catálogo.");
        Button retryButton = new Button("Reintentar");
        retryButton.addClickListener(e -> loadProducts());

        errorView.add(errorTitle, errorMessage, retryButton);

        prevButton = new Button("Anterior");
        prevButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        prevButton.addClickListener(e -> {
            if (currentPage > 0) {
                log.info("Navegando a la página anterior: {}", currentPage - 1);
                currentPage--;
                loadProducts();
            }
        });

        pageInfoLabel = new Span("Página 1 de 1");
        pageInfoLabel.getStyle().set("margin", "0 16px").set("font-weight", "bold");

        nextButton = new Button("Siguiente");
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nextButton.addClickListener(e -> {
            log.info("Navegando a la página siguiente: {}", currentPage + 1);
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadProducts();
            }
        });

        paginationLayout = new HorizontalLayout(prevButton, pageInfoLabel, nextButton);
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        paginationLayout.setWidthFull();
        paginationLayout.setMargin(true);
        paginationLayout.setPadding(true);
        paginationLayout.setSpacing(true);

        add(booksContainer, paginationLayout, errorView);

        loadProducts();
    }

    private void loadProducts() {
        try {
            errorView.setVisible(false);
            booksContainer.setVisible(true);
            paginationLayout.setVisible(true);

            booksContainer.removeAll();

            int apiPageNumber = currentPage + 1;

            PagedResult<Product> result = productService.getProducts(apiPageNumber);

            log.info(
                    "Respuesta del servicio: página={}, totalPages={}, totalElements={}, isFirst={}, isLast={}",
                    result.pageNumber(),
                    result.totalPages(),
                    result.totalElements(),
                    result.isFirst(),
                    result.isLast());

            totalPages = result.totalPages();
            totalElements = result.totalElements();
            updatePaginationInfo();

            if (result.data().isEmpty()) {
                showEmptyState();
            } else {
                log.info(
                        "Mostrando {} productos en la página {} de {}",
                        result.data().size(),
                        currentPage + 1,
                        totalPages);

                for (Product product : result.data()) {
                    booksContainer.add(createProductCard(product));
                }
            }

            UI.getCurrent().getPage().executeJs("window.scrollTo(0, 0);");

        } catch (ResourceAccessException e) {
            showErrorView();
            Notification.show(
                            "Error de conexión: No se pudo contactar con el servicio de catálogo",
                            5000,
                            Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            showErrorView();
            log.error("Error al cargar productos: ", e);
            Notification.show("Error al cargar productos: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updatePaginationInfo() {
        pageInfoLabel.setText(String.format("Página %d de %d", currentPage + 1, Math.max(1, totalPages)));

        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < totalPages - 1);

        if (!prevButton.isEnabled()) {
            prevButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        } else {
            prevButton.removeThemeVariants(ButtonVariant.LUMO_TERTIARY);
            prevButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }

        if (!nextButton.isEnabled()) {
            nextButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        } else {
            nextButton.removeThemeVariants(ButtonVariant.LUMO_TERTIARY);
            nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }

        log.info(
                "Controles de paginación actualizados: página {}/{}, anterior: {}, siguiente: {}",
                currentPage + 1,
                totalPages,
                prevButton.isEnabled(),
                nextButton.isEnabled());
    }

    private void showEmptyState() {
        VerticalLayout emptyState = new VerticalLayout();
        emptyState.setAlignItems(Alignment.CENTER);
        emptyState.setJustifyContentMode(JustifyContentMode.CENTER);

        H3 title = new H3("No hay productos disponibles");
        Button refreshButton = new Button("Refrescar");
        refreshButton.addClickListener(e -> loadProducts());

        emptyState.add(title, refreshButton);
        booksContainer.add(emptyState);
    }

    private void showErrorView() {
        booksContainer.setVisible(false);
        paginationLayout.setVisible(false);
        errorView.setVisible(true);
    }

    private Div createProductCard(Product product) {
        Div card = new Div();
        card.addClassName("product-card");
        card.setWidth("220px");
        card.getStyle()
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "4px")
                .set("padding", "15px")
                .set("box-shadow", "0 2px 5px rgba(0,0,0,0.1)")
                .set("transition", "transform 0.2s, box-shadow 0.2s")
                .set("cursor", "pointer");

        card.getElement().addEventListener("mouseover", e -> card.getStyle()
                .set("transform", "translateY(-5px)")
                .set("box-shadow", "0 5px 15px rgba(0,0,0,0.2)"));

        card.getElement().addEventListener("mouseout", e -> card.getStyle()
                .set("transform", "translateY(0)")
                .set("box-shadow", "0 2px 5px rgba(0,0,0,0.1)"));

        Image productImage = new Image();
        if (product.imageUrl() != null && !product.imageUrl().isEmpty()) {
            productImage.setSrc(product.imageUrl());
        } else {
            productImage.setSrc("images/books.png");
        }
        productImage.setAlt(product.name());
        productImage.setWidth("150px");
        productImage.setHeight("200px");
        productImage.getStyle().set("object-fit", "contain");

        Div imageContainer = new Div(productImage);
        imageContainer
                .getStyle()
                .set("display", "flex")
                .set("justify-content", "center")
                .set("width", "100%")
                .set("margin-bottom", "10px");

        Span title = new Span(product.name());
        title.getStyle()
                .set("font-weight", "bold")
                .set("display", "block")
                .set("margin", "8px 0")
                .set("text-align", "center")
                .set("height", "40px")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("display", "-webkit-box")
                .set("-webkit-line-clamp", "2")
                .set("-webkit-box-orient", "vertical");

        Button addButton = new Button("ADD");
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.getStyle().set("display", "block").set("margin", "10px auto").set("padding", "5px 20px");

        nextButton.addClickListener(e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                log.info("Navegando a la página siguiente: {}", currentPage);
                booksContainer.removeAll();
                UI.getCurrent().access(() -> {
                    loadProducts();
                });

                UI.getCurrent().getPage().executeJs("window.scrollTo(0, 0);");
            }
        });

        card.add(imageContainer, title, addButton);

        card.addClickListener(e -> {
            UI.getCurrent().navigate("product/" + product.code());
        });

        return card;
    }
}
