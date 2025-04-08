package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.entity.Product;

import java.util.List;

public class CategoryDto {
    private Long id;
    private String name;
    private String description;
    private List<Product> products;
}
