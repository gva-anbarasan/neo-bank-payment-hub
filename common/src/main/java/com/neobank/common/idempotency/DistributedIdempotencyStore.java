package com.neobank.common.idempotency;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.UUID;

public class DistributedIdempotencyStore {
    private final JedisPool jedisPool;

    public DistributedIdempotencyStore(String redisUrl) {
        // Fix: Add redis:// protocol if not present
        String fixedUrl = redisUrl;
        if (!redisUrl.startsWith("redis://") && !redisUrl.startsWith("rediss://")) {
            fixedUrl = "redis://" + redisUrl;
        }
        this.jedisPool = new JedisPool(fixedUrl);
    }

    public boolean tryAcquire(String key, long ttlSeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            String existingValue = jedis.get(key);
            if (existingValue == null) {
                String newValue = UUID.randomUUID().toString();
                String result = jedis.setex(key, ttlSeconds, newValue);
                return "OK".equals(result);
            }
            return false;
        }
    }

    public void release(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }

    public String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }
}