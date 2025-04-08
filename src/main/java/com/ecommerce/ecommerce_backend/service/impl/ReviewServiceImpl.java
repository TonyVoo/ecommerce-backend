package com.ecommerce.ecommerce_backend.service.impl;

import com.ecommerce.ecommerce_backend.dto.ReviewDto;
import com.ecommerce.ecommerce_backend.entity.Product;
import com.ecommerce.ecommerce_backend.entity.Review;
import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.repository.OrderRepository;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import com.ecommerce.ecommerce_backend.repository.ReviewRepository;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import com.ecommerce.ecommerce_backend.service.ReviewService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    public ReviewDto addReview(Long productId, int rating, String comment, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        boolean hasOrdered = orderRepository.findByUserId(user.getId()).stream()
                .flatMap(order -> order.getItems().stream())
                .anyMatch(item -> item.getProduct().getId().equals(productId));
        if (!hasOrdered) {
            throw new RuntimeException("You can only review products you have purchased");
        }

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new RuntimeException("You have already reviewed this product");
        }

        Review review = new Review(product, user, rating, comment);
        return convertToDto(reviewRepository.save(review));
    }

    @Override
    public List<ReviewDto> getProductReviews(Long productId, Authentication authentication) {
        List<Review> reviews = reviewRepository.findByProductId(productId);

        final boolean isAdmin;
        if (authentication != null) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            isAdmin = user != null && "ADMIN".equals(user.getRole());
        } else {
            isAdmin = false;
        }

        return reviews.stream()
                .filter(r -> isAdmin || r.isApproved())
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewDto approveReview(Long reviewId, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Only admins can approve reviews");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setApproved(true);
        return convertToDto(reviewRepository.save(review));
    }

    @Override
    public void deleteReview(Long reviewId, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Only admins can delete reviews");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        reviewRepository.delete(review);
    }

    @Override
    public ReviewDto editReview(Long reviewId, int rating, String comment, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only edit your own reviews");
        }

        if (review.getCreatedAt().plusHours(24).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reviews can only be edited within 24 hours of submission");
        }

        review.setRating(rating);
        review.setComment(comment);
        return convertToDto(reviewRepository.save(review));
    }

    private ReviewDto convertToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setUsername(review.getUser().getUsername());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setApproved(review.isApproved());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
