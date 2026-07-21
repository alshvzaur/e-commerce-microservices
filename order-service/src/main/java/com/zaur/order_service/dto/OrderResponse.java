package com.zaur.order_service.dto;


import com.zaur.order_service.model.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class OrderResponse {

    UUID orderId;
    OrderStatus orderStatus;
    LocalDateTime orderDate;
    List<OrderItemResponse> orderItemResponses;
    BigDecimal totalPrice;

}
