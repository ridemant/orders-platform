package co.apexglobal.ordersproducerjava.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {

    private String orderId;
    private String customerId;
    private List<ProductDto> products;
}