package com.neobank.payment.saga;

import com.neobank.common.idempotency.DistributedIdempotencyStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSagaOrchestrator {

    private final DistributedIdempotencyStore idempotencyStore;
    private final WalletService walletService;
    private final FraudCheckService fraudService;
    private final LedgerService ledgerService;

    public SagaResult execute(TransactionContext ctx) {
        String idempotencyKey = ctx.idempotencyKey();
        log.info("Starting payment saga for transaction: {}", ctx.transactionId());

        // Acquire idempotency lock
        if (!idempotencyStore.tryAcquire(idempotencyKey, 300)) {
            log.warn("Duplicate transaction detected: {}", idempotencyKey);
            return SagaResult.alreadyProcessed();
        }

        try {
            // Step 1: Fraud check
            log.info("Step 1: Running fraud check");
            if (!fraudService.check(ctx)) {
                throw new FraudDetectedException();
            }

            // Step 2: Reserve funds
            log.info("Step 2: Reserving funds");
            String reservationId = walletService.reserveFunds(ctx.userId(), ctx.amount());

            // Step 3: Debit wallet
            log.info("Step 3: Debiting wallet");
            walletService.debit(reservationId);

            // Step 4: Update ledger
            log.info("Step 4: Updating ledger");
            ledgerService.recordTransaction(ctx, "DEBIT");

            log.info("Payment saga completed successfully for: {}", ctx.transactionId());
            return SagaResult.success();

        } catch (FraudDetectedException e) {
            log.error("Fraud detected, saga failed", e);
            compensate(ctx);
            return SagaResult.failure(e.getMessage());

        } catch (Exception e) {
            log.error("Saga failed unexpectedly, compensating", e);
            compensate(ctx);
            return SagaResult.failure("Unexpected error: " + e.getMessage());

        } finally {
            idempotencyStore.release(idempotencyKey);
            log.info("Released idempotency lock for key: {}", idempotencyKey);
        }
    }

    private void compensate(TransactionContext ctx) {
        log.warn("Starting compensation for transaction: {}", ctx.transactionId());

        // Compensation actions in reverse order
        try {
            log.info("Compensation Step 1: Releasing wallet reservation");
            walletService.releaseReservation(ctx.userId());

            log.info("Compensation Step 2: Recording compensation in ledger");
            ledgerService.recordTransaction(ctx, "COMPENSATION");

            log.info("Compensation completed for transaction: {}", ctx.transactionId());
        } catch (Exception e) {
            log.error("Compensation failed for transaction: {}", ctx.transactionId(), e);
            // Manual intervention required
        }
    }
}