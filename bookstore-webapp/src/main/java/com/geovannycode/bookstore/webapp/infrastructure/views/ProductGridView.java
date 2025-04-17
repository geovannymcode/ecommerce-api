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
    private long totalElements = 0;
    private Button prevButton;
    private Button nextButton;

    public ProductGridView(ProductService productService) {
        this.productService = productService;

        // Configurar el layout principal
        setPadding(true);
        setSpacing(true);
        setSizeFull();

        // Contenedor de libros con layout flexible para la cuadrícula
        booksContainer = new FlexLayout();
        booksContainer.setWidthFull();
        booksContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        booksContainer.setJustifyContentMode(JustifyContentMode.CENTER);
        booksContainer.getStyle().set("gap", "20px");

        // Vista de error
        errorView = new VerticalLayout();
        errorView.setAlignItems(Alignment.CENTER);
        errorView.setJustifyContentMode(JustifyContentMode.CENTER);
        errorView.setVisible(false);

        H3 errorTitle = new H3("No se pudieron cargar los productos");
        Span errorMessage = new Span("Parece que hay un problema de conexión con el servicio de catálogo.");
        Button retryButton = new Button("Reintentar");
        retryButton.addClickListener(e -> loadProducts());

        errorView.add(errorTitle, errorMessage, retryButton);

        // Componentes de paginación
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
            // Incrementar la página y cargar nuevos datos
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

        // Cargar los productos
        loadProducts();
    }

    private void loadProducts() {
        try {
            // Ocultar vista de error y mostrar loading
            errorView.setVisible(false);
            booksContainer.setVisible(true);
            paginationLayout.setVisible(true);

            // Vaciar contenedor actual para mostrar que estamos cargando nuevos datos
            booksContainer.removeAll();

            int apiPageNumber = currentPage + 1;

            // Usar ProductService
            PagedResult<Product> result = productService.getProducts(apiPageNumber);

            // Registrar los detalles de la respuesta para depuración
            log.info(
                    "Respuesta del servicio: página={}, totalPages={}, totalElements={}, isFirst={}, isLast={}",
                    result.pageNumber(),
                    result.totalPages(),
                    result.totalElements(),
                    result.isFirst(),
                    result.isLast());

            // Actualizar información de paginación
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

            // Desplazar al inicio de la página después de cargar nuevos productos
            UI.getCurrent().getPage().executeJs("window.scrollTo(0, 0);");

        } catch (ResourceAccessException e) {
            // Error de conexión
            showErrorView();

            Notification.show(
                            "Error de conexión: No se pudo contactar con el servicio de catálogo",
                            5000,
                            Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            // Otro tipo de error
            showErrorView();

            log.error("Error al cargar productos: ", e);
            Notification.show("Error al cargar productos: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updatePaginationInfo() {
        pageInfoLabel.setText(String.format("Página %d de %d", currentPage + 1, Math.max(1, totalPages)));

        // Habilitar/deshabilitar botones según corresponda
        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < totalPages - 1);

        // Aplicar estilos adicionales para indicar visualmente cuándo un botón está deshabilitado
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
        // Card container
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

        // Hover effect
        card.getElement().addEventListener("mouseover", e -> card.getStyle()
                .set("transform", "translateY(-5px)")
                .set("box-shadow", "0 5px 15px rgba(0,0,0,0.2)"));

        card.getElement().addEventListener("mouseout", e -> card.getStyle()
                .set("transform", "translateY(0)")
                .set("box-shadow", "0 2px 5px rgba(0,0,0,0.1)"));

        // Product image
        Image productImage = new Image();
        if (product.imageUrl() != null && !product.imageUrl().isEmpty()) {
            productImage.setSrc(product.imageUrl());
        } else {
            // Default image if none provided
            productImage.setSrc("images/books.png");
        }
        productImage.setAlt(product.name());
        productImage.setWidth("150px");
        productImage.setHeight("200px");
        productImage.getStyle().set("object-fit", "contain");

        // Center the image
        Div imageContainer = new Div(productImage);
        imageContainer
                .getStyle()
                .set("display", "flex")
                .set("justify-content", "center")
                .set("width", "100%")
                .set("margin-bottom", "10px");

        // Product title
        Span title = new Span(product.name());
        title.getStyle()
                .set("font-weight", "bold")
                .set("display", "block")
                .set("margin", "8px 0")
                .set("text-align", "center")
                .set("height", "40px") // Altura fija para los títulos
                .set("overflow", "hidden") // Ocultar el texto que desborde
                .set("text-overflow", "ellipsis") // Mostrar puntos suspensivos
                .set("display", "-webkit-box")
                .set("-webkit-line-clamp", "2") // Limitar a 2 líneas
                .set("-webkit-box-orient", "vertical");

        // Add to cart button
        Button addButton = new Button("ADD");
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.getStyle().set("display", "block").set("margin", "10px auto").set("padding", "5px 20px");

        nextButton.addClickListener(e -> {
            if (currentPage < totalPages - 1) {
                // Incrementar la página antes de cargar los nuevos datos
                currentPage++;

                // Registro detallado antes de cargar
                log.info("Navegando a la página siguiente: {}", currentPage);

                // Limpiar el contenedor para indicar visualmente que se está cargando
                booksContainer.removeAll();

                // Cargar productos de la nueva página
                UI.getCurrent().access(() -> {
                    loadProducts();
                });

                // Desplazar al inicio de la página
                UI.getCurrent().getPage().executeJs("window.scrollTo(0, 0);");
            }
        });

        // Add all components to the card
        card.add(imageContainer, title, addButton);

        // Make the card clickable to view product details
        card.addClickListener(e -> {
            // Navigate to product details page
            UI.getCurrent().navigate("product/" + product.code());
        });

        return card;
    }
}
