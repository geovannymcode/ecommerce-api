package com.geovannycode.bookstore.webapp.infrastructure.api.controller;

import com.geovannycode.bookstore.webapp.domain.model.Cart;
import com.geovannycode.bookstore.webapp.domain.model.CartItemRequestDTO;
import com.geovannycode.bookstore.webapp.infrastructure.clients.cart.CartServiceClient;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final CartServiceClient cartServiceClient;

    public CartController(CartServiceClient cartServiceClient) {
        this.cartServiceClient = cartServiceClient;
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(@RequestParam(name = "cartId", required = false) String cartId) {
        log.info("Getting cart with cartId={}", cartId);
        try {
            Cart cart = cartServiceClient.getCart(cartId);
            log.info("Retrieved cart with ID: {}", cart != null ? cart.getId() : "null");
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            log.error("Error getting cart: {}", e.getMessage(), e);
            // Si no podemos obtener el carrito, creamos uno nuevo
            return ResponseEntity.ok(new Cart());
        }
    }

    @PostMapping
    public Cart addToCart(
            @RequestParam(name = "cartId", required = false) String cartId,
            @RequestBody @Valid CartItemRequestDTO cartItemRequest) {
        log.info("Adding to cart with cartId={} and request={}", cartId, cartItemRequest);
        try {
            Cart cart = cartServiceClient.addToCart(cartId, cartItemRequest);
            log.info("Item added to cart. New cart ID: {}", cart.getId());
            return cart;
        } catch (Exception e) {
            log.error("Error adding to cart: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping
    public Cart updateCartItemQuantity(
            @RequestParam(name = "cartId") String cartId, @RequestBody @Valid CartItemRequestDTO cartItemRequest) {
        log.info("Updating cart item quantity with cartId={} and request={}", cartId, cartItemRequest);
        return cartServiceClient.updateCartItemQuantity(cartId, cartItemRequest);
    }

    @DeleteMapping(value = "/items/{code}")
    public Cart removeCartItem(@RequestParam(name = "cartId") String cartId, @PathVariable("code") String code) {
        log.info("Removing cart item with cartId={} and code={}", cartId, code);
        return cartServiceClient.removeCartItem(cartId, code);
    }

    @DeleteMapping
    public void removeCart(@RequestParam(name = "cartId") String cartId) {
        log.info("Removing cart with cartId={}", cartId);
        cartServiceClient.removeCart(cartId);
    }
}
