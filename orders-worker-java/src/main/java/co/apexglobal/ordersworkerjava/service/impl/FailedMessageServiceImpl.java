package co.apexglobal.ordersworkerjava.service.impl;

import co.apexglobal.ordersworkerjava.service.FailedMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class FailedMessageServiceImpl implements FailedMessageService {

    private final ReactiveStringRedisTemplate redisTemplate;

    @Value("${failedMessages.ttlSeconds:604800}")
    private long ttlSeconds;

    private static final String ATTEMPTS_KEY_PREFIX = "order:attempts:";
    private static final String FAILED_KEY_PREFIX = "order:failed:";

    @Override
    public Mono<Long> recordFailure(String orderId, String rawfailed) {
        String attemptsKey = ATTEMPTS_KEY_PREFIX + orderId;
        String failedKey = FAILED_KEY_PREFIX + orderId;

        return redisTemplate.opsForValue().increment(attemptsKey)
                .flatMap(attempts -> {
                    if (attempts != null && attempts == 1L) {
                        return redisTemplate.opsForValue()
                                .set(failedKey, rawfailed, Duration.ofSeconds(ttlSeconds))
                                .thenReturn(attempts);
                    }
                    return Mono.just(attempts != null ? attempts : 0L);
                });
    }

    @Override
    public Mono<Long> getAttempts(String orderId) {
        String attemptsKey = ATTEMPTS_KEY_PREFIX + orderId;
        return redisTemplate.opsForValue().get(attemptsKey)
                .map(Long::valueOf)
                .defaultIfEmpty(0L);
    }
}
