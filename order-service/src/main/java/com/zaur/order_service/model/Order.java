package com.zaur.order_service.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Table(name = "orders")
public class Order {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "user_id")
    UUID userId;

    // EnumType.STRING — в БД хранится "NEW"/"CONFIRMED"/"CANCELLED" текстом,
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    OrderStatus status;

    // mappedBy = "order" — говорит Hibernate: "связь уже описана в поле Order.order внутри OrderItem,
    // не создавай отдельную промежуточную таблицу, просто используй order_id из orders_items"
    // cascade = ALL — при сохранении/удалении Order автоматически сохраняются/удаляются его OrderItem
    // orphanRemoval = true — если OrderItem убрали из этого списка в коде, он удалится из БД
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrderItem> orderItems;

    @Column(name = "created_date")
    LocalDateTime createdAt;
}