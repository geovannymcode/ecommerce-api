package com.geovannycode.bookstore.webapp.infrastructure.views.components;


import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class CartBadge extends HorizontalLayout {

    private final Span cartText;
    private int itemCount = 0;

    public CartBadge() {
        // Configurar layout
        setMargin(false);
        setPadding(false);
        setSpacing(false);

        // Crear el texto del carrito
        cartText = new Span(formatCartText(itemCount));
        cartText.getStyle()
                .set("font-weight", "bold")
                .set("cursor", "pointer");

        add(cartText);
    }


    public void updateCount(int count) {
        this.itemCount = count;
        cartText.setText(formatCartText(count));
    }


    private String formatCartText(int count) {
        return "Cart (" + count + ")";
    }


    public void incrementCount() {
        updateCount(itemCount + 1);
    }
}
