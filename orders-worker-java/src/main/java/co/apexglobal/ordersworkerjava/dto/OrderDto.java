package co.apexglobal.ordersworkerjava.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private String orderId;
    private String customerId;
    private List<ProductDto> products;
    private Instant createdAt;
}

