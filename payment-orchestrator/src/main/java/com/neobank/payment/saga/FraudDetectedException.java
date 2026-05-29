package com.neobank.payment.saga;

public class FraudDetectedException extends RuntimeException {

    public FraudDetectedException() {
        super("Fraud detected - transaction blocked");
    }

    public FraudDetectedException(String message) {
        super(message);
    }

    public FraudDetectedException(String message, Throwable cause) {
        super(message, cause);
    }
}