package com.ecommerce.ecommerce_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String imageUrl;

    @Transient
    private double averageRating;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
