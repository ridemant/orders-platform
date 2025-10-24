package co.apexglobal.ordersworkerjava.service;

import co.apexglobal.ordersworkerjava.service.impl.FailedMessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class FailedMessageServiceImplTest {

    private ReactiveStringRedisTemplate redisTemplate;
    private ReactiveValueOperations<String, String> valueOps;
    private FailedMessageServiceImpl service;

    @BeforeEach
    public void setup() {
        redisTemplate = mock(ReactiveStringRedisTemplate.class);
        valueOps = mock(ReactiveValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        service = new FailedMessageServiceImpl(redisTemplate);
    }

    @Test
    public void whenFirstFailure_thenStorePayloadAndReturnOne() {
        String orderId = "order-123";
        String payload = "{\"foo\":\"bar\"}";

        when(valueOps.increment("order:attempts:" + orderId)).thenReturn(Mono.just(1L));
        when(valueOps.set(eq("order:failed:" + orderId), eq(payload), ArgumentMatchers.any(Duration.class)))
                .thenReturn(Mono.just(true));

        Mono<Long> result = service.recordFailure(orderId, payload);

        Long attempts = result.block();
        assertEquals(1L, attempts);

        verify(valueOps).increment("order:attempts:" + orderId);
        verify(valueOps).set(eq("order:failed:" + orderId), eq(payload), ArgumentMatchers.any(Duration.class));
    }
}
