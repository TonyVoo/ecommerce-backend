package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.entity.Cart;
import com.ecommerce.ecommerce_backend.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@RequestParam Long productId, @RequestParam int quantity, Authentication authentication) {
        Cart cart = cartService.addToCart(productId, quantity, authentication);
        return ResponseEntity.ok(cart);
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(Authentication authentication) {
        Cart cart = cartService.getCart(authentication);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/update")
    public ResponseEntity<Cart> updateCartItem(@RequestParam Long cartItemId, @RequestParam int quantity, Authentication authentication) {
        Cart cart = cartService.updateCartItem(cartItemId, quantity, authentication);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Cart> removeCartItem(@RequestParam Long cartItemId, Authentication authentication) {
        Cart cart = cartService.removeCartItem(cartItemId, authentication);
        return ResponseEntity.ok(cart);
    }
}
