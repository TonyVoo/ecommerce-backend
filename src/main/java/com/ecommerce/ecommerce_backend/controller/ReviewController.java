package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.ReviewDto;
import com.ecommerce.ecommerce_backend.service.ReviewService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@AllArgsConstructor
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDto> addReview(
            @RequestParam Long productId,
            @RequestParam int rating,
            @RequestParam String comment,
            Authentication authentication) {
        return ResponseEntity.ok(reviewService.addReview(productId, rating, comment, authentication));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDto>> getProductReviews(@PathVariable Long productId, Authentication authentication) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, authentication));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> editReview(
            @PathVariable Long reviewId,
            @RequestParam int rating,
            @RequestParam String comment,
            Authentication authentication) {
        return ResponseEntity.ok(reviewService.editReview(reviewId, rating, comment, authentication));
    }

    @PutMapping("/{reviewId}/approve")
    public ResponseEntity<ReviewDto> approveReview(@PathVariable Long reviewId, Authentication authentication) {
        return ResponseEntity.ok(reviewService.approveReview(reviewId, authentication));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId, Authentication authentication) {
        reviewService.deleteReview(reviewId, authentication);
        return ResponseEntity.noContent().build();
    }
}
