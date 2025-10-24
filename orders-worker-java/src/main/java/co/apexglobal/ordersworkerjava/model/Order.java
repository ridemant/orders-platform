package co.apexglobal.ordersworkerjava.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private String orderId;
    private String customerId;
    private List<Product> products;
    private String status;
    private Integer retryCount;
}
