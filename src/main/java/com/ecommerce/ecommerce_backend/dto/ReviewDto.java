package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.entity.Product;
import com.ecommerce.ecommerce_backend.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDto {
    private Long id;
    private Long productId;
    private String username;
    private int rating;
    private String comment;
    private boolean approved;
    private LocalDateTime createdAt;
}
