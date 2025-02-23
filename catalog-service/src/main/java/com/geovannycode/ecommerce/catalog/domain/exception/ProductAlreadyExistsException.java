package com.geovannycode.ecommerce.catalog.domain.exception;

import java.net.URI;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class ProductAlreadyExistsException extends ErrorResponseException {

    public ProductAlreadyExistsException(String code) {
        super(HttpStatus.BAD_REQUEST, asProblemDetail("Product with code '" + code + "' already exist"), null);
    }

    private static ProblemDetail asProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setTitle("Product already exist");
        problemDetail.setType(URI.create("https://api.geovannycode-bookstore.com/errors/product-exist"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
