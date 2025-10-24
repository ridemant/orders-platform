package co.apexglobal.ordersproducerjava.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private String productId;
    private String name;
    private String description;
    private Double price;
    private Integer quantity;
}

