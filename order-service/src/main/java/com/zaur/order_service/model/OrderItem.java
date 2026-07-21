package com.zaur.order_service.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Table(name = "order_items")
public class OrderItem {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    // Владеющая сторона связи (owning side) — именно здесь физически хранится
    // колонка order_id в таблице order_items. Это поле "знает" про Order,
    // а Order.orderItems (mappedBy) — просто "зеркало" этой связи для удобства чтения из кода
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    Order order;

    @Column(name = "product_id")
    UUID productId;

    // Сколько единиц этого конкретного товара заказано в рамках этого заказа
    @Column(name = "quantity")
    int quantity;
}