package com.geovannycode.ecommerce.cart.application.service.impl;

import com.geovannycode.ecommerce.cart.application.ports.input.AddItemToCartUseCase;
import com.geovannycode.ecommerce.cart.application.ports.input.GetCartUseCase;
import com.geovannycode.ecommerce.cart.application.ports.input.RemoveCartItemUseCase;
import com.geovannycode.ecommerce.cart.application.ports.input.RemoveCartUseCase;
import com.geovannycode.ecommerce.cart.application.ports.input.UpdateCartItemQuantityUseCase;
import com.geovannycode.ecommerce.cart.application.ports.output.CartRepository;
import com.geovannycode.ecommerce.cart.domain.exception.CartNotFoundException;
import com.geovannycode.ecommerce.cart.domain.model.Cart;
import com.geovannycode.ecommerce.cart.domain.model.CartItem;
import com.geovannycode.ecommerce.cart.infrastructure.adapter.client.catalog.Product;
import com.geovannycode.ecommerce.cart.infrastructure.adapter.client.catalog.ProductNotFoundException;
import com.geovannycode.ecommerce.cart.infrastructure.adapter.client.catalog.ProductServiceClient;
import com.geovannycode.ecommerce.cart.infrastructure.api.dto.CartItemRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CartServiceImpl
        implements AddItemToCartUseCase,
                GetCartUseCase,
                UpdateCartItemQuantityUseCase,
                RemoveCartItemUseCase,
                RemoveCartUseCase {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;

    public CartServiceImpl(CartRepository cartRepository, ProductServiceClient productServiceClient) {
        this.cartRepository = cartRepository;
        this.productServiceClient = productServiceClient;
    }

    @Override
    public Cart addToCart(String cartId, CartItemRequestDTO cartItemRequest) {
        Cart cart;
        if (!StringUtils.hasText(cartId)) {
            cart = Cart.withNewId();
        } else {
            cart = cartRepository.findById(cartId).orElseThrow(() -> new CartNotFoundException(cartId));
        }
        log.info("Add code: {} to cart", cartItemRequest.code());
        Product product = productServiceClient
                .getProductByCode(cartItemRequest.code())
                .orElseThrow(() -> new ProductNotFoundException(cartItemRequest.code()));
        CartItem cartItem = new CartItem(
                product.code(),
                product.name(),
                product.description(),
                product.price(),
                cartItemRequest.quantity() > 0 ? cartItemRequest.quantity() : 1);
        cart.addItem(cartItem);
        return cartRepository.save(cart);
    }

    @Override
    public Cart getCart(String cartId) {
        if (!StringUtils.hasText(cartId)) {
            return cartRepository.save(Cart.withNewId());
        }
        return cartRepository.findById(cartId).orElseThrow(() -> new CartNotFoundException(cartId));
    }

    @Override
    public Cart removeCartItem(String cartId, String code) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new CartNotFoundException(cartId));
        log.info("Remove cart line item code: {}", code);
        cart.removeItem(code);
        return cartRepository.save(cart);
    }

    @Override
    public void removeCart(String cartId) {
        cartRepository.deleteById(cartId);
    }

    @Override
    public Cart updateCartItemQuantity(String cartId, CartItemRequestDTO cartItemRequest) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new CartNotFoundException(cartId));
        log.info(
                "Update quantity: {} for code:{} quantity in cart: {}",
                cartItemRequest.quantity(),
                cartItemRequest.code(),
                cartId);

        if (cartItemRequest.quantity() <= 0) {
            cart.removeItem(cartItemRequest.code());
        } else {
            Product product = productServiceClient
                    .getProductByCode(cartItemRequest.code())
                    .orElseThrow(() -> new ProductNotFoundException(cartItemRequest.code()));
            cart.updateItemQuantity(product.code(), cartItemRequest.quantity());
        }
        return cartRepository.save(cart);
    }
}
