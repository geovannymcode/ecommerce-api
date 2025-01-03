package com.geovannycode.ecommerce.cart.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("carts")
public class Cart {

    @Id
    private String cartId;
    private String customerId;
    private Set<CartItem> items = new HashSet<>();

    public Cart(String cartId) {
        this.cartId = cartId;
    }

    public static Cart withNewId() {
        return new Cart(UUID.randomUUID().toString());
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
}
