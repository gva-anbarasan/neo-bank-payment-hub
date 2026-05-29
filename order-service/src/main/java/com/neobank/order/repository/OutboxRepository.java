package com.neobank.order.repository;

import com.neobank.order.entity.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxMessage, Long> {
    List<OutboxMessage> findByPublishedFalse();
}