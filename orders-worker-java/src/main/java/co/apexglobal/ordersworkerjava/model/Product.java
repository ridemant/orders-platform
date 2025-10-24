package co.apexglobal.ordersworkerjava.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String productId;
    private String name;
    private String description;
    private Double price;
    private Integer quantity;
    private Boolean active;
}
