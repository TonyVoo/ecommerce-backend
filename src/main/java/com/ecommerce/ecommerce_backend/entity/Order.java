package com.ecommerce.ecommerce_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private LocalDateTime orderDate;

    private double totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    public Order(User user, List<OrderItem> items, LocalDateTime orderDate) {
        this.user = user;
        this.items = items;
        this.orderDate = orderDate;
        this.totalPrice = calculateTotalPrice();
    }

    private double calculateTotalPrice() {
        return items.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    private String paymentIntentId;
}
