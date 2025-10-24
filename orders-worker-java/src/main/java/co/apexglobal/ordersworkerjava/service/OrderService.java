package co.apexglobal.ordersworkerjava.service;

import co.apexglobal.ordersworkerjava.model.Order;
import reactor.core.publisher.Mono;

public interface OrderService {
    Mono<Order> processOrder(Order order, String rawPayload);
}
