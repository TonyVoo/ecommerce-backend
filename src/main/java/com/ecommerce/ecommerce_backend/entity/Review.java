package com.ecommerce.ecommerce_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Product product;

    @ManyToOne
    private User user;

    private int rating; // 1 to 5 stars

    @Column(length = 500)
    private String comment;

    private LocalDateTime createdAt;

    private boolean approved = false;

    public Review(Product product, User user, int rating, String comment) {
        this.product = product;
        this.user = user;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
        this.approved = false;
    }
}
