package com.neobank.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentOrchestratorApplication.class, args);
        System.out.println("Payment Orchestrator Service Started!");
        System.out.println("Saga pattern with idempotency is ready");
    }
}