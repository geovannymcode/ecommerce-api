package com.geovannycode.ecommerce.payment;

import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestPaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(PaymentServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
