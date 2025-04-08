package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.ProductDto;
import com.ecommerce.ecommerce_backend.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<ProductDto> getAllProducts();
    Optional<ProductDto> getProductById(Long id);
    List<ProductDto> searchProducts(String query);
    List<ProductDto> getProductsByCategory(Long categoryId);
    ProductDto createProduct(Product product, Long categoryId);
    ProductDto updateProduct(Long id, Product product);
    void deleteProduct(Long id);
}
