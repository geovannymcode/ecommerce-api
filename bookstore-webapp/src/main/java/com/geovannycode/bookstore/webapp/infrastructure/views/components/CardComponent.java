package com.geovannycode.bookstore.webapp.infrastructure.views.components;

import com.vaadin.flow.component.html.Div;

public class CardComponent extends Div {

    public CardComponent() {
        getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-l)")
                .set("width", "100%")
                .set("max-width", "1000px")
                .set("margin-inline", "auto");
    }
}
