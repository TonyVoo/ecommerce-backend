package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.ReviewDto;
import com.ecommerce.ecommerce_backend.entity.Review;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ReviewService {
    ReviewDto addReview(Long productId, int rating, String comment, Authentication authentication);
    List<ReviewDto> getProductReviews(Long productId, Authentication authentication);
    ReviewDto approveReview(Long reviewId, Authentication authentication);
    void deleteReview(Long reviewId, Authentication authentication);
    ReviewDto editReview(Long reviewId, int rating, String comment, Authentication authentication);
}
