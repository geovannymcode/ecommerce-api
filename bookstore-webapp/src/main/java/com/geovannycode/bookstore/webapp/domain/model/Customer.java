package com.geovannycode.bookstore.webapp.domain.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class Customer {

    @NotBlank(message = "Customer Name is required")
    private String name;

    @NotBlank(message = "Customer email is required")
    @Email
    private String email;

    @NotBlank(message = "Customer Phone number is required")
    private String phone;

    // Constructor vacío requerido por frameworks
    public Customer() {
    }

    // Constructor con todos los campos
    public Customer(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Getters y setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
