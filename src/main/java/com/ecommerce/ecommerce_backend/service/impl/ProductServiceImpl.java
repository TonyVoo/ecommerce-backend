package com.ecommerce.ecommerce_backend.service.impl;

import com.ecommerce.ecommerce_backend.dto.ProductDto;
import com.ecommerce.ecommerce_backend.entity.Category;
import com.ecommerce.ecommerce_backend.entity.Product;
import com.ecommerce.ecommerce_backend.entity.Review;
import com.ecommerce.ecommerce_backend.repository.CategoryRepository;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import com.ecommerce.ecommerce_backend.repository.ReviewRepository;
import com.ecommerce.ecommerce_backend.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public List<ProductDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        products.forEach(this::setAverageRating);
        return products.stream().map(this::convertToDto).toList();
    }

    @Override
    public Optional<ProductDto> getProductById(Long id) {
        validateId(id);
        return productRepository.findById(id)
                        .map(product -> {
                            setAverageRating(product);
                            return convertToDto(product);
                        });
    }

    @Override
    public List<ProductDto> searchProducts(String query) {
        List<Product> products = (query == null || query.trim().isEmpty()) ?
                productRepository.findAll() :
                productRepository.findByNameContainingIgnoreCase(query);

        products.forEach(this::setAverageRating);
        return products.stream().map(this::convertToDto).toList();
    }

    @Override
    public List<ProductDto> getProductsByCategory(Long categoryId) {
        validateId(categoryId);
        List<Product> products = productRepository.findByCategoryId(categoryId);
        products.forEach(this::setAverageRating);
        return products.stream().map(this::convertToDto).toList();
    }

    @Override
    public ProductDto createProduct(Product product, Long categoryId) {
        validateProduct(product);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalStateException("Category with ID " + categoryId + " not found"));

        product.setCategory(category);
        Product saved = productRepository.save(product);
        setAverageRating(saved);
        return convertToDto(saved);
    }

    @Override
    public ProductDto updateProduct(Long id, Product product) {
        validateId(id);
        validateProduct(product);

        if (!productRepository.existsById(id)) {
            throw new IllegalStateException("Product with ID " + id + " not found");
        }
        product.setId(id);
        Product saved = productRepository.save(product);
        setAverageRating(saved);
        return convertToDto(saved);
    }

    @Override
    public void deleteProduct(Long id) {
        validateId(id);
        if (!productRepository.existsById(id)) {
            throw new IllegalStateException("Product with ID " + id + " not found");
        }
        productRepository.deleteById(id);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid ID: " + id);
        }
    }

    private void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
    }

    private void setAverageRating(Product product) {
        List<Review> reviews = reviewRepository.findByProductId(product.getId());
        double avgRating = reviews.isEmpty() ? 0 : reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0);
        product.setAverageRating(avgRating);
    }

    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        dto.setStock(product.getStock());
        dto.setAverageRating(product.getAverageRating());
        dto.setImageUrl(product.getImageUrl());
        dto.setCategoryName(product.getCategory().getName());
        return dto;
    }
}
