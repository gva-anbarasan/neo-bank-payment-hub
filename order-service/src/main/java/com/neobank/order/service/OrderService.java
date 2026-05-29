package com.neobank.order.service;

import com.neobank.order.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    @Transactional
    public Order createOrder(String userId, BigDecimal amount, String currency, String idempotencyKey) {
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setUserId(userId);
        order.setAmount(amount);
        order.setCurrency(currency);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        order.setIdempotencyKey(idempotencyKey);
        
        log.info("Order created: {}", order.getId());
        return order;
    }
}
