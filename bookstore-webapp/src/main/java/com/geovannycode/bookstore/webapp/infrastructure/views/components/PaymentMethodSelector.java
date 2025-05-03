package com.geovannycode.bookstore.webapp.infrastructure.views.components;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CssImport("./styles/payment-selector.css")
public class PaymentMethodSelector extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(PaymentMethodSelector.class);

    private Div selectedPaymentMethod = null;
    private String selectedPaymentType = null;
    private PaymentSelectionListener listener;

    public PaymentMethodSelector() {
        setPadding(true);
        setSpacing(true);
        setWidthFull();
        addClassName("payment-selector");

        H3 title = new H3("Selecciona tu forma de pago");
        title.getStyle().set("margin-top", "0");
        add(title);

        // Crear las opciones de pago
        HorizontalLayout paymentOptions = createPaymentOptions();
        add(paymentOptions);
    }

    private HorizontalLayout createPaymentOptions() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setJustifyContentMode(JustifyContentMode.CENTER);
        layout.addClassName("payment-options-container");

        // Opción de Crédito
        Div creditOption = createPaymentOption("Crédito", "images/payment/credit-card.png", "VISA, MasterCard, AMEX");

        // Opción de Stripe
        Div stripeOption = createPaymentOption("Stripe", "images/payment/stripe.png", "Pago seguro con Stripe");

        // Opción de PayPal
        Div paypalOption = createPaymentOption("PayPal", "images/payment/paypal.png", "Pago con cuenta PayPal");

        // Opción de Mercado Pago
        Div mercadoPagoOption =
                createPaymentOption("Mercado Pago", "images/payment/mercadopago.png", "Múltiples opciones de pago");

        layout.add(creditOption, stripeOption, paypalOption, mercadoPagoOption);
        return layout;
    }

    private Div createPaymentOption(String type, String imagePath, String description) {
        Div option = new Div();
        option.addClassName("payment-option");
        option.getElement().setAttribute("payment-type", type);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(Alignment.CENTER);

        Image logo = new Image(imagePath, type);
        logo.addClassName("payment-logo");

        Span title = new Span(type);
        title.addClassName("payment-title");

        Span desc = new Span(description);
        desc.addClassName("payment-description");

        content.add(logo, title, desc);
        option.add(content);

        // Evento de clic
        option.addClickListener(e -> selectPaymentMethod(option, type));

        return option;
    }

    private void selectPaymentMethod(Div option, String type) {
        // Quitar selección anterior
        if (selectedPaymentMethod != null) {
            selectedPaymentMethod.removeClassName("selected");
        }

        // Aplicar selección
        option.addClassName("selected");
        selectedPaymentMethod = option;
        selectedPaymentType = type;

        log.info("Método de pago seleccionado: {}", type);

        // Notificar al listener
        if (listener != null) {
            listener.onPaymentMethodSelected(type);
        }
    }

    public void setPaymentSelectionListener(PaymentSelectionListener listener) {
        this.listener = listener;
    }

    public String getSelectedPaymentType() {
        return selectedPaymentType;
    }

    public interface PaymentSelectionListener {
        void onPaymentMethodSelected(String paymentType);
    }
}
