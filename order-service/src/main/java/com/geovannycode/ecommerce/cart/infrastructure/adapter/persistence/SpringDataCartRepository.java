package com.geovannycode.ecommerce.cart.infrastructure.adapter.persistence;

import com.geovannycode.ecommerce.cart.domain.model.Cart;
import org.springframework.data.repository.CrudRepository;

public interface SpringDataCartRepository extends CrudRepository<Cart, String> {}
