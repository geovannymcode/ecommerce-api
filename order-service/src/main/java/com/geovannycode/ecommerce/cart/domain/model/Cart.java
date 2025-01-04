package com.geovannycode.ecommerce.cart.domain.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Builder
@RedisHash("carts")
public class Cart {

    @Id
    private String cartId;

    private Set<CartItem> items = new HashSet<>();

    public Cart() {
        this.cartId = UUID.randomUUID().toString();
        this.items = new HashSet<>();
    }

    public Cart(String cartId) {
        this.cartId = cartId;
    }

    public Cart(String cartId, Set<CartItem> items) {
        this.cartId = cartId;
        this.items = items != null ? new HashSet<>(items) : new HashSet<>();
    }

    public static Cart withNewId() {
        return new Cart(UUID.randomUUID().toString(), new HashSet<>());
    }

    public void addItem(CartItem item) {
        for (CartItem cartItem : items) {
            if (cartItem.getCode().equals(item.getCode())) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                return;
            }
        }
        this.items.add(item);
    }

    public void updateItemQuantity(String code, int quantity) {
        for (CartItem cartItem : items) {
            if (cartItem.getCode().equals(code)) {
                cartItem.setQuantity(quantity);
            }
        }
    }

    public void removeItem(String code) {
        CartItem item = null;
        for (CartItem cartItem : items) {
            if (cartItem.getCode().equals(code)) {
                item = cartItem;
                break;
            }
        }
        if (item != null) {
            items.remove(item);
        }
    }

    public void clearItems() {
        items = new HashSet<>();
    }

    public BigDecimal getCartTotal() {
        return items.stream().map(CartItem::getSubTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getId() {
        return cartId;
    }

    public void setId(String cartId) {
        this.cartId = cartId;
    }

    public Set<CartItem> getItems() {
        return items;
    }

    public void setItems(Set<CartItem> items) {
        this.items = items;
    }
}
