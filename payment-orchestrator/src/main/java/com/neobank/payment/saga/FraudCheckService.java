package com.neobank.payment.saga;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FraudCheckService {

    public boolean check(TransactionContext ctx) {
        log.info("Performing fraud check for transaction: {}", ctx.transactionId());

        // Fraud detection rules
        if (ctx.amount() > 50000) {
            log.warn("Fraud detected: Amount exceeds limit: {}", ctx.amount());
            return false;
        }

        if (ctx.userId().startsWith("suspicious")) {
            log.warn("Fraud detected: Suspicious user: {}", ctx.userId());
            return false;
        }

        log.info("Fraud check passed for transaction: {}", ctx.transactionId());
        return true;
    }
}