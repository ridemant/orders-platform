package co.apexglobal.ordersworkerjava.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Customer {
    private String id;
    private String name;
    private String email;
    private Boolean active;
}
