package com.geovannycode.bookstore.webapp.infrastructure.views.components;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;

public class CartBadge extends Div {

    private final Span cartText;
    private int itemCount = 0;

    public CartBadge() {
        getStyle()
                .set("cursor", "pointer")
                .set("display", "inline-block");

        cartText = new Span(formatCartText(itemCount));
        cartText.getStyle()
                .set("font-weight", "bold")
                .set("padding", "8px 12px")
                .set("border-radius", "16px")
                .set("color", "var(--lumo-primary-text-color)")
                .set("background-color", "var(--lumo-primary-color-10pct)");

        getElement().addEventListener("mouseover", e ->
                cartText.getStyle().set("background-color", "var(--lumo-primary-color-20pct)"));

        getElement().addEventListener("mouseout", e ->
                cartText.getStyle().set("background-color", "var(--lumo-primary-color-10pct)"));

        add(cartText);
    }

    public void updateCount(int count) {
        this.itemCount = count;
        cartText.setText(formatCartText(count));

        if (count > 0) {
            cartText.getStyle()
                    .set("color", "var(--lumo-primary-text-color)")
                    .set("background-color", "var(--lumo-primary-color-10pct)");
        } else {
            cartText.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("background-color", "var(--lumo-contrast-10pct)");
        }
    }

    private String formatCartText(int count) {
        return "Cart (" + count + ")";
    }

    public void incrementCount() {
        updateCount(itemCount + 1);
    }
}
