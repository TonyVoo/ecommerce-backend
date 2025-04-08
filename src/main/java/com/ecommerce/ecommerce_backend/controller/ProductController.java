package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.ProductDto;
import com.ecommerce.ecommerce_backend.entity.Product;
import com.ecommerce.ecommerce_backend.service.CategoryService;
import com.ecommerce.ecommerce_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public List<ProductDto> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable("id") Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDto>> searchProducts(@RequestParam String query) {
        return ResponseEntity.ok(productService.searchProducts(query));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable("categoryId") Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    @PostMapping("/upload")
    public ResponseEntity<ProductDto> createProduct(@RequestBody Product product, @RequestParam Long categoryId) {
        return ResponseEntity.ok(productService.createProduct(product, categoryId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable("id") Long id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
