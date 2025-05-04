package com.geovannycode.bookstore.webapp.infrastructure.views.components;

import com.geovannycode.bookstore.webapp.domain.model.OrderStatus;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class StatusBadge extends HorizontalLayout {

    private final Span badge;

    public StatusBadge() {
        badge = new Span();
        badge.getElement().getThemeList().add("badge");

        // Estilos base para el badge
        badge.getStyle()
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "500")
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)");

        setSpacing(false);
        setPadding(false);
        setMargin(false);
        add(badge);
    }

    public void setStatus(OrderStatus status) {
        // Limpiar cualquier tema anterior
        badge.getElement().getThemeList().clear();
        badge.getElement().getThemeList().add("badge");

        // Establecer el texto
        badge.setText(status.toString());

        // Aplicar estilo según el estado
        switch (status) {
            case NEW:
                badge.getStyle()
                        .set("background-color", "var(--lumo-primary-color-10pct)")
                        .set("color", "var(--lumo-primary-text-color)");
                break;
            case IN_PROCESS:
                badge.getStyle()
                        .set("background-color", "var(--lumo-contrast-10pct)")
                        .set("color", "var(--lumo-contrast)");
                break;
            case DELIVERED:
                badge.getStyle()
                        .set("background-color", "var(--lumo-success-color-10pct)")
                        .set("color", "var(--lumo-success-text-color)");
                break;
            case CANCELLED:
            case ERROR:
                badge.getStyle()
                        .set("background-color", "var(--lumo-error-color-10pct)")
                        .set("color", "var(--lumo-error-text-color)");
                break;
            default:
                // Estilo por defecto
                badge.getStyle()
                        .set("background-color", "var(--lumo-contrast-10pct)")
                        .set("color", "var(--lumo-contrast)");
                break;
        }
    }

    public Span getBadge() {
        return badge;
    }
}
