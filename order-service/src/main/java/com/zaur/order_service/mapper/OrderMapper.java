package com.zaur.order_service.mapper;

import com.zaur.order_service.client.ProductClient;
import com.zaur.order_service.dto.OrderItemResponse;
import com.zaur.order_service.dto.OrderResponse;
import com.zaur.order_service.dto.ProductResponse;
import com.zaur.order_service.model.Order;
import com.zaur.order_service.model.OrderItem;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderMapper {

    ProductClient productClient;

    public OrderResponse toOrderResponse(Order order) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        List<OrderItemResponse> orderItemResponseList = new ArrayList<>();

        for (OrderItem orderItem : order.getOrderItems()) {
            ProductResponse productResponse = productClient.getProductById(orderItem.getProductId());

            OrderItemResponse orderItemResponse = OrderItemResponse.builder()
                    .productName(productResponse.getName())
                    .price(productResponse.getPrice())
                    .quantity(orderItem.getQuantity())
                    .subtotal(productResponse.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                    .build();
            orderItemResponseList.add(orderItemResponse);
            totalPrice = totalPrice.add(orderItemResponse.getSubtotal());
        }


        return OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getStatus())
                .orderDate(order.getCreatedAt())
                .orderItemResponses(orderItemResponseList)
                .totalPrice(totalPrice)
                .build();
    }

}
