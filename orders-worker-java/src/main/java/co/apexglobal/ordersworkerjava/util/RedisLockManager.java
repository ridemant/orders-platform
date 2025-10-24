package co.apexglobal.ordersworkerjava.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisLockManager {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private static final String LOCK_PREFIX = "order:lock:";

    @Value("${redis.lock.ttl}")
    private long lockTtl;

    public Mono<Boolean> acquireLock(String orderId) {
        String lockKey = LOCK_PREFIX + orderId;
        return redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCKED", Duration.ofMillis(lockTtl));
    }

    public Mono<Boolean> releaseLock(String orderId) {
        String lockKey = LOCK_PREFIX + orderId;
        return redisTemplate.opsForValue()
                .delete(lockKey);
    }

    public Mono<Boolean> isLocked(String orderId) {
        String lockKey = LOCK_PREFIX + orderId;
        return redisTemplate.opsForValue()
                .get(lockKey)
                .map(value -> true)
                .defaultIfEmpty(false);
    }
}
