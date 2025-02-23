package com.geovannycode.ecommerce.catalog.infrastructure.api.mapper;

import com.geovannycode.ecommerce.catalog.domain.model.Product;
import com.geovannycode.ecommerce.catalog.infrastructure.api.dto.ProductDto;
import java.math.BigDecimal;

public class ProductMapper {
    public static ProductDto toDto(Product product) {
        return new ProductDto(
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                product.getPrice(),
                product.getStock(),
                product.getDiscount(),
                calculateSalePrice(product.getPrice(), product.getDiscount()));
    }

    public static Product toEntity(ProductDto productDto) {
        return new Product(
                null,
                productDto.code(),
                productDto.name(),
                productDto.description(),
                productDto.imageUrl(),
                productDto.price(),
                productDto.stock(),
                productDto.discount(),
                false);
    }

    private static BigDecimal calculateSalePrice(BigDecimal price, BigDecimal discount) {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) == 0) {
            return price;
        }
        return price.subtract(price.multiply(discount));
    }
}
