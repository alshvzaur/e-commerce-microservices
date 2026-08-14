package com.zaur.order_service.service;

import com.zaur.order_service.client.ProductClient;
import com.zaur.order_service.dto.OrderItemRequest;
import com.zaur.order_service.dto.OrderRequest;
import com.zaur.order_service.dto.OrderResponse;
import com.zaur.order_service.dto.ProductResponse;
import com.zaur.order_service.exception.InsufficientStockException;
import com.zaur.order_service.mapper.OrderMapper;
import com.zaur.order_service.model.Order;
import com.zaur.order_service.model.OrderStatus;
import com.zaur.order_service.repo.OrderRepository;
import com.zaur.order_service.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderMapper orderMapper;

    @Mock
    ProductClient productClient;

    @InjectMocks
    OrderService orderService;

    UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(testUserId, "testUser", "USER");
        Authentication authentication = new UsernamePasswordAuthenticationToken(authenticatedUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void shouldGetOrder(){
        UUID orderId =  UUID.randomUUID();
        Order order = Order.builder()
                        .id(orderId).userId(testUserId).status(OrderStatus.NEW).orderItems(List.of()).build();

        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(orderId)
                .orderStatus(order.getStatus())
                .orderItemResponses(List.of())
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toOrderResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.getOrder(orderId);

        assertEquals(orderResponse, result);
    }

    @Test
    void shouldThrowAccessDeniedWhenUserIsNotOwnerOrAdmin(){
        UUID orderId =  UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Order order = Order.builder()
                .id(orderId).userId(userId).status(OrderStatus.NEW).orderItems(List.of()).build();

        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(orderId)
                .orderStatus(order.getStatus())
                .orderItemResponses(List.of())
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));


        assertThrows(AccessDeniedException.class, () -> orderService.getOrder(orderId));

    }

    @Test
    void shouldReturnMyOrder(){
        UUID order1Id =  UUID.randomUUID();
        UUID order2Id =  UUID.randomUUID();

        Order order1 = Order.builder()
                .id(order1Id).userId(testUserId).status(OrderStatus.NEW).orderItems(List.of()).build();
        Order order2 = Order.builder()
                .id(order2Id).userId(testUserId).status(OrderStatus.NEW).orderItems(List.of()).build();

        OrderResponse orderResponse1 = OrderResponse.builder()
                .orderId(order1Id).orderStatus(order1.getStatus()).orderItemResponses(List.of()).build();
        OrderResponse orderResponse2 = OrderResponse.builder()
                .orderId(order2Id).orderStatus(order2.getStatus()).orderItemResponses(List.of()).build();

        List<Order> orders = List.of(order1, order2);
        List<OrderResponse> orderResponses = List.of(orderResponse1, orderResponse2);

        when(orderRepository.findOrderByUserId(testUserId)).thenReturn(orders);
        when(orderMapper.toOrderResponse(order1)).thenReturn(orderResponse1);
        when(orderMapper.toOrderResponse(order2)).thenReturn(orderResponse2);

        List<OrderResponse> result = orderService.getMyOrder();
        assertEquals(orderResponse1, result.get(0));
        assertEquals(orderResponse2, result.get(1));
        assertEquals(2, result.size());
    }

    @Test
    void shouldCreateOrder(){
        UUID orderId =  UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Integer quantity = 3;

        Order savedOrder = Order.builder()
                .id(orderId).userId(userId).status(OrderStatus.NEW).orderItems(List.of()).build();

        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(orderId).orderStatus(savedOrder.getStatus()).orderItemResponses(List.of()).build();

        OrderItemRequest orderItemRequest = new OrderItemRequest(productId, quantity);

        OrderRequest orderRequest = new OrderRequest(List.of(orderItemRequest));

        ProductResponse testProductResponse = ProductResponse.builder()
                        .id(productId).name("test").description("test").price(new BigDecimal("200.00")).quantity(10)
                .build();

        ProductResponse someProductResponse = ProductResponse.builder()
                .id(productId).name("test").description("test").price(new BigDecimal("200.00")).quantity(7)
                .build();


        when(productClient.getProductById(productId)).thenReturn(testProductResponse);
        when(productClient.reduceStock(productId, quantity)).thenReturn(someProductResponse);

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toOrderResponse(savedOrder)).thenReturn(orderResponse);

        OrderResponse result = orderService.createOrder(orderRequest);

        assertEquals(orderResponse, result);
    }

    @Test
    void shouldThrowInsufficientStockWhenNotEnoughQuantity() {

        UUID productId = UUID.randomUUID();
        int quantity = 13;


        OrderItemRequest orderItemRequest = new OrderItemRequest(productId, quantity);

        OrderRequest orderRequest = new OrderRequest(List.of(orderItemRequest));

        ProductResponse testProductResponse = ProductResponse.builder()
                .id(productId).name("test").description("test").price(new BigDecimal("200.00")).quantity(10)
                .build();

        when(productClient.getProductById(productId)).thenReturn(testProductResponse);

        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(orderRequest));
    }
}
