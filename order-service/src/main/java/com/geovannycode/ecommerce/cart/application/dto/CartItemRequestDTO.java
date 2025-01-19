package com.geovannycode.ecommerce.cart.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

public record CartItemRequestDTO(@NotEmpty String code, @Min(0) int quantity) {}
