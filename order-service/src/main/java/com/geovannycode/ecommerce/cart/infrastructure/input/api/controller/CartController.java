package com.geovannycode.ecommerce.cart.infrastructure.input.api.controller;

import com.geovannycode.ecommerce.cart.application.ports.input.AddItemToCartUseCase;
import com.geovannycode.ecommerce.cart.application.ports.input.GetCartUseCase;
import com.geovannycode.ecommerce.cart.application.ports.input.RemoveCartItemUseCase;
import com.geovannycode.ecommerce.cart.application.ports.input.RemoveCartUseCase;
import com.geovannycode.ecommerce.cart.application.ports.input.UpdateCartItemQuantityUseCase;
import com.geovannycode.ecommerce.cart.domain.model.Cart;
import com.geovannycode.ecommerce.cart.application.dto.CartItemRequestDTO;
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
@RequestMapping("/api/carts")
public class CartController {
    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final AddItemToCartUseCase addItemToCartUseCase;
    private final GetCartUseCase getCartUseCase;
    private final RemoveCartItemUseCase removeCartItemUseCase;
    private final RemoveCartUseCase removeCartUseCase;
    private final UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase;

    public CartController(
            AddItemToCartUseCase addItemToCartUseCase,
            GetCartUseCase getCartUseCase,
            RemoveCartItemUseCase removeCartItemUseCase,
            RemoveCartUseCase removeCartUseCase,
            UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase) {
        this.addItemToCartUseCase = addItemToCartUseCase;
        this.getCartUseCase = getCartUseCase;
        this.removeCartItemUseCase = removeCartItemUseCase;
        this.removeCartUseCase = removeCartUseCase;
        this.updateCartItemQuantityUseCase = updateCartItemQuantityUseCase;
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(@RequestParam(name = "cartId", required = false) String cartId) {
        Cart cart = getCartUseCase.getCart(cartId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping
    public Cart addToCart(
            @RequestParam(name = "cartId", required = false) String cartId,
            @RequestBody @Valid CartItemRequestDTO cartItemRequest) {
        log.info("Adding to cart with cartId={} and request={}", cartId, cartItemRequest);
        return addItemToCartUseCase.addToCart(cartId, cartItemRequest);
    }

    @PutMapping
    public Cart updateCartItemQuantity(
            @RequestParam(name = "cartId") String cartId, @RequestBody @Valid CartItemRequestDTO cartItemRequest) {
        return updateCartItemQuantityUseCase.updateCartItemQuantity(cartId, cartItemRequest);
    }

    @DeleteMapping(value = "/items/{code}")
    public Cart removeCartItem(@RequestParam(name = "cartId") String cartId, @PathVariable("code") String code) {
        return removeCartItemUseCase.removeCartItem(cartId, code);
    }

    @DeleteMapping
    public void removeCart(@RequestParam(name = "cartId") String cartId) {
        removeCartUseCase.removeCart(cartId);
    }
}
