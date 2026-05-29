package com.neobank.ui.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatsMessage {
    private int tps;
    private double successRate;
    private double avgLatency;
    private int totalTransactions;
    private long idempotencyHits;
    private long retryCount;
    private long dlqCount;
    private int activeConsumers;
    private long consumerLag;
    private String timestamp;
}