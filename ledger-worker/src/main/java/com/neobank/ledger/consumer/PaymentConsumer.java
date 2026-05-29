package com.neobank.ledger.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class PaymentConsumer {

    @KafkaListener(topics = "ORDER_CREATED", groupId = "ledger-group",
            containerFactory = "batchManualAckFactory")
    public void consumeBatch(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        try {
            for (ConsumerRecord<String, String> record : records) {
                log.info("Processing: key={}, value={}", record.key(), record.value());
                processMessage(record.value());
            }
            ack.acknowledge();
            log.info("Committed {} records", records.size());
        } catch (Exception e) {
            log.error("Batch processing failed", e);
            // Don't acknowledge - will retry on rebalance
        }
    }

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2))
    @KafkaListener(topics = "ORDER_CREATED", groupId = "ledger-retry-group")
    public void consumeWithRetry(String message) {
        log.info("Processing with retry: {}", message);
        processMessage(message);
    }

    @DltHandler
    public void handleDLQ(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Moving to DLQ: {} from topic {}", message, topic);
        // Store in database for manual intervention
    }

    private void processMessage(String message) {
        // Simulate processing
        if (message.contains("error")) {
            throw new RuntimeException("Processing failed");
        }
    }
}