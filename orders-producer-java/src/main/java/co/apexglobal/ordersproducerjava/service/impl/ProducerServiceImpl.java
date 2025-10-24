package co.apexglobal.ordersproducerjava.service.impl;

import co.apexglobal.ordersproducerjava.dto.OrderDto;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import co.apexglobal.ordersproducerjava.service.ProducerService;

@Primary
@Service
public class ProducerServiceImpl implements ProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProducerServiceImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Void> sendOrder(String topic, String key, OrderDto order) {
        return Mono.create(sink -> kafkaTemplate.send(topic, key, order).whenComplete((res, ex) -> {
            if (ex != null) sink.error(ex);
            else sink.success();
        }));
    }
}

