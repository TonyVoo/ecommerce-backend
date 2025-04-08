package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.entity.Cart;
import com.ecommerce.ecommerce_backend.entity.Order;
import com.ecommerce.ecommerce_backend.entity.OrderStatus;
import com.ecommerce.ecommerce_backend.service.CartService;
import com.ecommerce.ecommerce_backend.service.OrderService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody PaymentConfirmation paymentConfirmation, Authentication authentication) {
        Order order = orderService.placeOrder(authentication, paymentConfirmation.getPaymentIntentId(), paymentConfirmation.getDiscountCode());
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(Authentication authentication) {
        List<Order> orders = orderService.getUserOrders(authentication);
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long orderId, Authentication authentication) {
        Order order = orderService.cancelOrder(orderId, authentication);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long orderId, @RequestParam OrderStatus orderStatus, Authentication authentication) {
        Order order = orderService.updateOrderStatus(orderId, orderStatus, authentication);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderDetails(@PathVariable Long orderId, Authentication authentication) {
        Order order = orderService.getOrderDetails(orderId, authentication);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<String> createPaymentIntent(@RequestBody PaymentRequest paymentRequest, Authentication authentication) {
        Cart cart = cartService.getCart(authentication);
        double totalPrice = cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        try {
            String clientSecret = orderService.createPaymentIntent(totalPrice);
            return ResponseEntity.ok(clientSecret);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Payment error: " + e.getMessage());
        }
    }

    @Data
    static class PaymentRequest {
        private double amount;
    }

    @Data
    static class PaymentConfirmation {
        private String paymentIntentId;
        private String DiscountCode;
    }

}
