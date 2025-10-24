package co.apexglobal.ordersworkerjava.mapper;

import co.apexglobal.ordersworkerjava.dto.OrderDto;
import co.apexglobal.ordersworkerjava.dto.ProductDto;
import co.apexglobal.ordersworkerjava.model.Order;
import co.apexglobal.ordersworkerjava.model.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public Order toOrder(OrderDto event) {
        if (event == null) return null;
        return Order.builder()
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .products(mapProducts(event.getProducts()))
                .status("RECEIVED")
                .retryCount(0)
                .build();
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
}
