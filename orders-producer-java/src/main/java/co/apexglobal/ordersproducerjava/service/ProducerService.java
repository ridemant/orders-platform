package co.apexglobal.ordersproducerjava.service;

import co.apexglobal.ordersproducerjava.dto.OrderDto;
import reactor.core.publisher.Mono;

public interface ProducerService {
    Mono<Void> sendOrder(String topic, String key, OrderDto order);
}

