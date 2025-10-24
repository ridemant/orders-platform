package co.apexglobal.ordersworkerjava.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private String productId;
    private Integer quantity;
    private String name;
    private Double price;
}
