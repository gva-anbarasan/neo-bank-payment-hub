package com.neobank.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
@Slf4j
public class PaymentController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/stats/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok("{\"status\": \"alive\", \"service\": \"api-gateway\"}");
    }

    @PostMapping("/payments")
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequest request,
                                            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        try {
            log.info("Processing payment for user: {}, amount: {}", request.userId(), request.amount());
            // For now, return success
            return ResponseEntity.ok(new PaymentResponse("SUCCESS", "Payment processed"));
        } catch (Exception e) {
            log.error("Payment failed", e);
            return ResponseEntity.ok(new PaymentResponse("FAILED", e.getMessage()));
        }
    }
}

// Move records outside the controller class
record PaymentRequest(String userId, Double amount, String currency, String cardNumber) {}
record PaymentResponse(String status, String message) {}