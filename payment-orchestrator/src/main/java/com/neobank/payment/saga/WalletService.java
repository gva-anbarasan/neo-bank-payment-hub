package com.neobank.payment.saga;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@Slf4j
public class WalletService {

    public String reserveFunds(String userId, Double amount) {
        log.info("Reserving {} for user {}", amount, userId);
        String reservationId = "res_" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Reservation created: {}", reservationId);
        return reservationId;
    }

    public void debit(String reservationId) {
        log.info("Debiting reservation: {}", reservationId);
        // Simulate debit logic
    }

    public void releaseReservation(String userId) {
        log.info("Released reservation for user: {}", userId);
        // Simulate release logic
    }
}