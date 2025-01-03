package com.geovannycode.ecommerce.cart.infrastructure.adapter.persistence;

import com.geovannycode.ecommerce.cart.domain.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCartRepository extends JpaRepository<Cart, String> {}
