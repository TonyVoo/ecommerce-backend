package com.ecommerce.ecommerce_backend.dto;

import lombok.Data;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String imageUrl;
    private double averageRating;
    private String categoryName;
}
