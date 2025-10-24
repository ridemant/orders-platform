package co.apexglobal.ordersworkerjava.service;

import reactor.core.publisher.Mono;

public interface FailedMessageService {
    Mono<Long> recordFailure(String orderId, String rawPayload);

    Mono<Long> getAttempts(String orderId);
}
