package com.geovannycode.ecommerce.cart.infrastructure.output.repository;

import com.geovannycode.ecommerce.cart.domain.model.Cart;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataCartRepository extends CrudRepository<Cart, String> {}
