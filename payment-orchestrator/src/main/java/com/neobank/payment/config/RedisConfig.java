package com.neobank.payment.config;

import com.neobank.common.idempotency.DistributedIdempotencyStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Bean
    public DistributedIdempotencyStore distributedIdempotencyStore() {
        String redisUrl = String.format("%s:%d", redisHost, redisPort);
        return new DistributedIdempotencyStore(redisUrl);
    }
}
