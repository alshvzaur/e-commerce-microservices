package com.zaur.order_service.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class OrderItemResponse {

    String productName;
    BigDecimal price;
    Integer quantity;
    BigDecimal subtotal;
}
