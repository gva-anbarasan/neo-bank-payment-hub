package com.neobank.payment.saga;

public record TransactionContext(
        String transactionId,
        String userId,
        Double amount,
        String idempotencyKey
) {}