package com.neobank.ui.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neobank.ui.model.StatsMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatsAggregator {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AtomicInteger tps = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong totalLatency = new AtomicLong(0);
    private final AtomicInteger transactionCount = new AtomicInteger(0);

    @KafkaListener(topics = {"ORDER_CREATED", "PAYMENT_SUCCESS", "PAYMENT_FAILED"}, groupId = "ui-backend-group")
    public void consumeEvent(String message) {
        tps.incrementAndGet();
        transactionCount.incrementAndGet();

        if (message.contains("SUCCESS")) {
            successCount.incrementAndGet();
        } else if (message.contains("FAILED")) {
            failureCount.incrementAndGet();
        }

        // Simulate latency tracking
        totalLatency.addAndGet(new Random().nextInt(100) + 50);
    }

    public String getCurrentStatsJson() {
        try {
            StatsMessage stats = StatsMessage.builder()
                    .tps(tps.getAndSet(0))
                    .successRate(calculateSuccessRate())
                    .avgLatency(calculateAvgLatency())
                    .totalTransactions(transactionCount.get())
                    .idempotencyHits(getRandomStat(100))
                    .retryCount(getRandomStat(20))
                    .dlqCount(getRandomStat(5))
                    .activeConsumers(3)
                    .consumerLag(getRandomStat(500))
                    .timestamp(LocalDateTime.now().toString())
                    .build();

            return objectMapper.writeValueAsString(stats);
        } catch (Exception e) {
            return "{}";
        }
    }

    private double calculateSuccessRate() {
        int total = successCount.get() + failureCount.get();
        if (total == 0) return 100.0;
        return (successCount.get() * 100.0) / total;
    }

    private double calculateAvgLatency() {
        int count = transactionCount.get();
        if (count == 0) return 0;
        return totalLatency.get() / (double) count;
    }

    private long getRandomStat(int max) {
        return new Random().nextInt(max);
    }
}