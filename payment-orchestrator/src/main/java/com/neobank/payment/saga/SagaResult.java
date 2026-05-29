package com.neobank.payment.saga;

public record SagaResult(String status, String message) {

    public static SagaResult success() {
        return new SagaResult("SUCCESS", null);
    }

    public static SagaResult alreadyProcessed() {
        return new SagaResult("ALREADY_PROCESSED", "Transaction already processed");
    }

    public static SagaResult failure(String msg) {
        return new SagaResult("FAILURE", msg);
    }

    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }

    public boolean isAlreadyProcessed() {
        return "ALREADY_PROCESSED".equals(status);
    }

    public boolean isFailure() {
        return "FAILURE".equals(status);
    }
}