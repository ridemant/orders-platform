package co.apexglobal.ordersworkerjava.listener;

import co.apexglobal.ordersworkerjava.model.Order;
import co.apexglobal.ordersworkerjava.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;

import static org.mockito.Mockito.*;

public class OrderListenerTest {

    @Test
    public void whenKafkaRecord_thenProcessOrderCalled() throws Exception {
        KafkaReceiver<String, String> kafkaReceiver = mock(KafkaReceiver.class);
        OrderService orderService = mock(OrderService.class);
        ObjectMapper objectMapper = new ObjectMapper();

        @SuppressWarnings("unchecked")
        ReceiverRecord<String, String> record = mock(ReceiverRecord.class);
        ReceiverOffset offset = mock(ReceiverOffset.class);

        String json = "{\"orderId\":\"order-1\",\"customerId\":\"cust-1\",\"products\":[{\"productId\":\"prod-1\",\"quantity\":1}]}";

        when(record.value()).thenReturn(json);
        when(record.receiverOffset()).thenReturn(offset);
        when(kafkaReceiver.receive()).thenReturn(Flux.just(record));

        when(orderService.processOrder(any(Order.class), eq(json)))
                .thenReturn(Mono.just(Order.builder().orderId("order-1").build()));

        OrderReactiveListener listener = new OrderReactiveListener(kafkaReceiver, orderService, objectMapper);

        listener.start();

        verify(orderService, timeout(2000)).processOrder(any(Order.class), eq(json));

        verify(offset, timeout(2000)).acknowledge();
    }
}
