package co.apexglobal.ordersworkerjava.service.impl;

import co.apexglobal.ordersworkerjava.model.Customer;
import co.apexglobal.ordersworkerjava.model.Order;
import co.apexglobal.ordersworkerjava.model.Product;
import co.apexglobal.ordersworkerjava.repository.OrderRepository;
import co.apexglobal.ordersworkerjava.service.FailedMessageService;
import co.apexglobal.ordersworkerjava.service.OrderService;
import co.apexglobal.ordersworkerjava.util.RedisLockManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final RedisLockManager redisLockManager;
    private final WebClient webClient;
    private final FailedMessageService failedMessageService;

    @Value("${services.product.url}")
    private String productServiceUrl;

    @Value("${services.customer.url}")
    private String customerServiceUrl;

    @Value("${retry.maxAttempts:3}")
    private int maxRetryAttempts;

    @Value("${retry.initialInterval:1000}")
    private long initialIntervalMs;

    @Value("${retry.multiplier:2}")
    private double multiplier;

    private static final int CALL_MAX_RETRIES = 3;

    @Override
    public Mono<Order> processOrder(Order order, String rawPayload) {
        log.info("Iniciando procesamiento de orden: {}", order.getOrderId());
        return redisLockManager.acquireLock(order.getOrderId())
                .flatMap(locked -> {
                    if (!locked) {
                        log.warn("Orden {} está siendo procesada por otra instancia", order.getOrderId());
                        return Mono.error(new RuntimeException("Order is being processed by another instance"));
                    }

                    return enrichOrderData(order)
                            .flatMap(this::validateOrder)
                            .flatMap(orderRepository::save)
                            .flatMap(savedOrder ->
                                    redisLockManager.releaseLock(order.getOrderId()).thenReturn(savedOrder)
                            )
                            .onErrorResume(error -> handleProcessingError(order, rawPayload, error));
                });
    }

    private Mono<Order> handleProcessingError(Order order, String rawPayload, Throwable error) {
        return failedMessageService.recordFailure(order.getOrderId(), rawPayload)
                .flatMap(attempts -> {
                    if (attempts >= maxRetryAttempts) {
                        log.warn("Max retry attempts reached for order {} ({}). Marking as failed.", order.getOrderId(), attempts);
                        return redisLockManager.releaseLock(order.getOrderId())
                                .onErrorResume(e -> Mono.empty())
                                .then(Mono.error(new RuntimeException("Max attempts reached: " + error.getMessage())));
                    }
                    return redisLockManager.releaseLock(order.getOrderId())
                            .onErrorResume(e -> Mono.empty())
                            .then(Mono.error(error));
                });
    }

    private Mono<Order> enrichOrderData(Order order) {
        return Mono.just(order)
                .flatMap(this::enrichWithProductData)
                .flatMap(this::enrichWithCustomerData)
                .retryWhen(Retry.backoff(CALL_MAX_RETRIES, Duration.ofMillis(initialIntervalMs)).maxBackoff(Duration.ofSeconds(10)))
                .doOnError(err -> log.error("Error during enrichOrderData for {}: {}", order.getOrderId(), err.getMessage()));
    }

    private Mono<Order> enrichWithProductData(Order order) {
        if (order.getProducts() == null || order.getProducts().isEmpty()) {
            return Mono.just(order);
        }

        List<Mono<Product>> productEnrichments = order.getProducts().stream()
                .map(orig -> webClient.get()
                        .uri(productServiceUrl + "/products/" + orig.getProductId())
                        .retrieve()
                        .bodyToMono(Product.class)
                        .retryWhen(Retry.backoff(CALL_MAX_RETRIES, Duration.ofMillis(initialIntervalMs)))
                        .map(prodFromService -> {
                            if (prodFromService == null) {
                                throw new RuntimeException("Product not found: " + orig.getProductId());
                            }
                            if (!Boolean.TRUE.equals(prodFromService.getActive())) {
                                throw new RuntimeException("Product not active: " + orig.getProductId());
                            }
                            prodFromService.setQuantity(orig.getQuantity() == null ? 1 : orig.getQuantity());
                            return prodFromService;
                        })
                        .doOnError(err -> log.error("Error fetching product {}: {}", orig.getProductId(), err.getMessage()))
                )
                .collect(Collectors.toList());

        return Mono.zip(productEnrichments, objects -> Arrays.stream(objects).map(o -> (Product) o).collect(Collectors.toList()))
                .map(list -> {
                    @SuppressWarnings("unchecked")
                    List<Product> enriched = (List<Product>) list;
                    order.setProducts(enriched);
                    return order;
                });
    }

    private Mono<Order> enrichWithCustomerData(Order order) {
        return webClient.get()
                .uri(customerServiceUrl + "/customers/" + order.getCustomerId())
                .retrieve()
                .bodyToMono(Customer.class)
                .retryWhen(Retry.backoff(CALL_MAX_RETRIES, Duration.ofMillis(initialIntervalMs)))
                .flatMap(customer -> {
                    if (customer == null) {
                        return Mono.error(new RuntimeException("Customer not found: " + order.getCustomerId()));
                    }
                    if (!Boolean.TRUE.equals(customer.getActive())) {
                        return Mono.error(new RuntimeException("Customer not active: " + order.getCustomerId()));
                    }
                    return Mono.just(order);
                })
                .doOnError(err -> log.error("Error fetching customer {}: {}", order.getCustomerId(), err.getMessage()));
    }

    private Mono<Order> validateOrder(Order order) {
        if (order.getProducts() == null || order.getProducts().isEmpty()) {
            log.error("Orden {} no tiene productos", order.getOrderId());
            return Mono.error(new RuntimeException("Order must have at least one product"));
        }
        boolean anyMissing = order.getProducts().stream().anyMatch(p -> p.getName() == null || p.getPrice() == null);
        if (anyMissing) {
            log.error("Orden {} tiene productos sin datos completos después del enriquecimiento", order.getOrderId());
            return Mono.error(new RuntimeException("Some products lack details after enrichment"));
        }
        log.debug("Orden {} validada exitosamente", order.getOrderId());
        return Mono.just(order);
    }
}
