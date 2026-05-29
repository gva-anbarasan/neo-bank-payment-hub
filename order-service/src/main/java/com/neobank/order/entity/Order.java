package com.neobank.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;  // ← ADD THIS IMPORT
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userId;
    private BigDecimal amount;  // Now works with import
    private String currency;
    private String status;
    private LocalDateTime createdAt;
    private String idempotencyKey;
}