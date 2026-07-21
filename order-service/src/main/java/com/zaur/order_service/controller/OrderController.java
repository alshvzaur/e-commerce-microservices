package com.zaur.order_service.controller;

import com.zaur.order_service.dto.OrderRequest;
import com.zaur.order_service.dto.OrderResponse;
import com.zaur.order_service.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    OrderService orderService;

    @PostMapping()
    public OrderResponse createOrder(@RequestBody OrderRequest orderRequest) {
        return orderService .createOrder(orderRequest);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable UUID id) {
        return orderService.getOrder(id);
    }

    @GetMapping("/my")
    public List<OrderResponse> getMyOrder() {
        return orderService.getMyOrder();
    }
}
