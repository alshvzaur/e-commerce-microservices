package com.zaur.order_service.service;

import com.zaur.order_service.client.ProductClient;
import com.zaur.order_service.dto.OrderItemRequest;
import com.zaur.order_service.dto.OrderRequest;
import com.zaur.order_service.dto.OrderResponse;
import com.zaur.order_service.dto.ProductResponse;
import com.zaur.order_service.exception.InsufficientStockException;
import com.zaur.order_service.exception.OrderNotFoundException;
import com.zaur.order_service.mapper.OrderMapper;
import com.zaur.order_service.model.Order;
import com.zaur.order_service.model.OrderItem;
import com.zaur.order_service.model.OrderStatus;
import com.zaur.order_service.repo.OrderRepository;
import com.zaur.order_service.security.AuthenticatedUser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
public class OrderService {

    OrderRepository orderRepository;
    OrderMapper orderMapper;
    ProductClient productClient;


    public OrderResponse createOrder(OrderRequest orderRequest) {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = user.getId();

        List<OrderItem> orderItems = new ArrayList<>();


        for(OrderItemRequest request: orderRequest.getItems()){
            ProductResponse product = productClient.getProductById(request.getProductId());

            if (product.getQuantity() >= request.getQuantity()) {
                OrderItem orderItem = OrderItem.builder()
                        .productId(product.getId())
                        .quantity(request.getQuantity())
                        .build();
                orderItems.add(orderItem);

                productClient.reduceStock(request.getProductId(), request.getQuantity());


            }else {
                throw new InsufficientStockException("Insufficient stock");
            }
        }

        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.NEW)
                .orderItems(orderItems)
                .createdAt(LocalDateTime.now())
                .build();

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
        }

        return orderMapper.toOrderResponse(orderRepository.save(order));

    }

    public OrderResponse getOrder(UUID id){

        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = user.getId();

        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException("Order not found" + id));

        if (order.getUserId() == null){
            throw new OrderNotFoundException("User of order not found" + id);
        }

        if (user.getId().equals(order.getUserId()) || user.getRole().equals("ADMIN")) {

            return orderMapper.toOrderResponse(order);
        }else {
            throw new AccessDeniedException("Access denied!");
        }

    }

    public List<OrderResponse> getMyOrder() {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = user.getId();

        List<Order> orders = orderRepository.findOrderByUserId(userId);
        List<OrderResponse>  orderResponses = new ArrayList<>();

        for (Order order: orders){
            orderResponses.add(orderMapper.toOrderResponse(order));
        }
        return orderResponses;
    }
}
