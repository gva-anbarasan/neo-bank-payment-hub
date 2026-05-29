package com.neobank.common.retry;

import java.util.function.Supplier;

public class RetryUtils {
    public static <T> T retry(Supplier<T> action, int maxRetries, long initialBackoffMs) {
        int attempt = 0;
        while (true) {
            try {
                return action.get();
            } catch (Exception e) {
                if (++attempt > maxRetries) {
                    throw new RuntimeException("Retry exhausted after " + maxRetries + " attempts", e);
                }
                long backoff = initialBackoffMs * (long) Math.pow(2, attempt - 1);
                try {
                    Thread.sleep(Math.min(backoff, 30000));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
    }
}