package co.apexglobal.ordersproducerjava.web;

import co.apexglobal.ordersproducerjava.dto.OrderDto;
import co.apexglobal.ordersproducerjava.service.ProducerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
@Validated
public class OrdersController {

    private final ProducerService producerService;
    private final String ordersTopic;

    // Explicit constructor: inject ProducerService and the topic property (with default)
    public OrdersController(ProducerService producerService,
                            @Value("${kafka.topic.orders:orders-topic}") String ordersTopic) {
        this.producerService = producerService;
        this.ordersTopic = ordersTopic;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> publishOrder(@Valid @RequestBody OrderDto order) {
        return producerService.sendOrder(ordersTopic, order.getOrderId(), order);
    }
}
