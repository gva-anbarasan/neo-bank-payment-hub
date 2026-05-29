package com.neobank.payment.saga;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@Slf4j
public class LedgerService {

    public void recordTransaction(TransactionContext ctx, String type) {
        log.info("Recording {} transaction to ledger:", type);
        log.info("  Transaction ID: {}", ctx.transactionId());
        log.info("  User ID: {}", ctx.userId());
        log.info("  Amount: {}", ctx.amount());
        log.info("  Timestamp: {}", LocalDateTime.now());
        log.info("  Type: {}", type);

        // Simulate ledger recording
        if ("COMPENSATION".equals(type)) {
            log.warn("Compensation transaction recorded for rollback");
        }
    }
}