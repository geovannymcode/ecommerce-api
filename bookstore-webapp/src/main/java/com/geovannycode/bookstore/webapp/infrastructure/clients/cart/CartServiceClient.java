package com.geovannycode.bookstore.webapp.infrastructure.clients.cart;

import com.geovannycode.bookstore.webapp.domain.model.Cart;
import com.geovannycode.bookstore.webapp.domain.model.CartItemRequestDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

public interface CartServiceClient {

    @GetExchange("/carts/api/carts")
    Cart getCart(@RequestParam(name = "cartId", required = false) String cartId);

    @PostExchange("/carts/api/carts")
    Cart addToCart(
            @RequestParam(name = "cartId", required = false) String cartId,
            @RequestBody CartItemRequestDTO cartItemRequest);

    @PutExchange("/carts/api/carts")
    Cart updateCartItemQuantity(
            @RequestParam(name = "cartId") String cartId,
            @RequestBody CartItemRequestDTO cartItemRequest);

    @DeleteExchange("/carts/api/carts/items/{code}")
    Cart removeCartItem(
            @RequestParam(name = "cartId") String cartId,
            @PathVariable("code") String code);

    @DeleteExchange("/carts/api/carts")
    void removeCart(@RequestParam(name = "cartId") String cartId);
}
