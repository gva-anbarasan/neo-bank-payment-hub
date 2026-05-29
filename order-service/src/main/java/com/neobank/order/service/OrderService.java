package com.neobank.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neobank.order.entity.Order;
import com.neobank.order.entity.OutboxMessage;
import com.neobank.order.repository.OrderRepository;
import com.neobank.order.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;  // ← ADD THIS IMPORT
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public Order createOrder(String userId, BigDecimal amount, String currency, String idempotencyKey) {
        Order order = new Order();
        order.setUserId(userId);
        order.setAmount(amount);
        order.setCurrency(currency);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        order.setIdempotencyKey(idempotencyKey);
        orderRepository.save(order);

        OutboxMessage outbox = new OutboxMessage();
        outbox.setAggregateType("ORDER");
        outbox.setAggregateId(order.getId());
        outbox.setEventType("ORDER_CREATED");
        outbox.setPayload(String.format("{\"orderId\":\"%s\",\"userId\":\"%s\",\"amount\":%s}",
                order.getId(), userId, amount));
        outbox.setCreatedAt(LocalDateTime.now());
        outbox.setPublished(false);
        outboxRepository.save(outbox);

        log.info("Order created with outbox: {}", order.getId());
        return order;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishOutboxEvents() {
        List<OutboxMessage> pending = outboxRepository.findByPublishedFalse();
        for (OutboxMessage msg : pending) {
            try {
                kafkaTemplate.send(msg.getEventType(), msg.getAggregateId(), msg.getPayload())
                        .get(5, TimeUnit.SECONDS);
                msg.setPublished(true);
                outboxRepository.save(msg);
                log.info("Published outbox event: {}", msg.getId());
            } catch (Exception e) {
                log.error("Failed to publish outbox message: {}", msg.getId(), e);
            }
        }
    }
}