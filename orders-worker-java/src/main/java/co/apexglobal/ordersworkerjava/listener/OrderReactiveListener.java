package co.apexglobal.ordersworkerjava.listener;

import co.apexglobal.ordersworkerjava.dto.OrderDto;
import co.apexglobal.ordersworkerjava.dto.ProductDto;
import co.apexglobal.ordersworkerjava.model.Order;
import co.apexglobal.ordersworkerjava.model.Product;
import co.apexglobal.ordersworkerjava.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReactiveListener {

    private final KafkaReceiver<String, String> kafkaReceiver;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        kafkaReceiver.receive()
                .flatMap(record -> processRecord(record)
                        .onErrorResume(err -> {
                            log.error("Error processing record: {}", err.getMessage());
                            try {
                                record.receiverOffset().acknowledge();
                            } catch (Exception e) {
                                log.warn("Error acknowledging offset after failure: {}", e.getMessage());
                            }
                            return Mono.empty();
                        }))
                .subscribe();
    }

    private Mono<Void> processRecord(ReceiverRecord<String, String> record) {
        String raw = record.value();
        return Mono.fromCallable(() -> objectMapper.readValue(raw, OrderDto.class))
                .flatMap(event -> {
                    if (!isValid(event)) {
                        log.warn("Invalid order event received: {}", event);
                        try { record.receiverOffset().acknowledge(); } catch (Exception e) { log.warn("Ack failed: {}", e.getMessage()); }
                        return Mono.empty();
                    }

                    String orderId = event.getOrderId() == null || event.getOrderId().isBlank()
                            ? UUID.randomUUID().toString()
                            : event.getOrderId();

                    Order order = Order.builder()
                            .orderId(orderId)
                            .customerId(event.getCustomerId())
                            .products(mapProducts(event.getProducts()))
                            .status("RECEIVED")
                            .retryCount(0)
                            .build();

                    return orderService.processOrder(order, raw)
                            .doOnSuccess(saved -> log.info("Successfully processed order: {}", saved.getOrderId()))
                            .doOnError(err -> log.error("Error processing order {}: {}", order.getOrderId(), err.getMessage()))
                            .then(Mono.fromRunnable(() -> {
                                try {
                                    record.receiverOffset().acknowledge();
                                } catch (Exception e) {
                                    log.warn("Failed to acknowledge offset for {}: {}", order.getOrderId(), e.getMessage());
                                }
                            }));
                })
                .onErrorResume(ex -> {
                    log.error("Failed to parse or process record: {}", ex.getMessage());
                    try { record.receiverOffset().acknowledge(); } catch (Exception e) { log.warn("Ack failed in onErrorResume: {}", e.getMessage()); }
                    return Mono.empty();
                }).then();
    }

    private List<Product> mapProducts(List<ProductDto> products) {
        if (products == null) return List.of();
        return products.stream()
                .map(p -> Product.builder()
                        .productId(p.getProductId())
                        .quantity(p.getQuantity() == null ? 1 : p.getQuantity())
                        .name(p.getName())
                        .price(p.getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    private boolean isValid(OrderDto event) {
        if (event == null) return false;
        if (event.getCustomerId() == null || event.getCustomerId().isBlank()) return false;
        if (event.getProducts() == null || event.getProducts().isEmpty()) return false;
        return event.getProducts().stream()
                .filter(Objects::nonNull)
                .allMatch(p -> p.getProductId() != null && !p.getProductId().isBlank());
    }
}
