package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.entity.*;
import com.ecommerce.ecommerce_backend.repository.DiscountCodeRepository;
import com.ecommerce.ecommerce_backend.repository.OrderRepository;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    @Transactional
    public Order placeOrder(Authentication authentication, String paymentIntentId, String discountCode) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartService.getCart(authentication);
        if(cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Stripe.apiKey = stripeSecretKey;
        PaymentIntent paymentIntent;
        try {
            paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to retrieve payment intent: " + e.getMessage());
        }
        if(!"succeed".equals(paymentIntent.getStatus())) {
            throw new RuntimeException("Payment not completed");
        }

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> {
                    Product product = cartItem.getProduct();
                    int quantity = cartItem.getQuantity();
                    if(product.getStock() < quantity) {
                        throw new RuntimeException("Insufficient stock for " + product.getName());
                    }
                    product.setStock(product.getStock() - quantity);
                    productRepository.save(product);
                    return new OrderItem(product, quantity);
                })
                .collect(Collectors.toList());

        Order order = new Order(user, orderItems, LocalDateTime.now());
        order.setPaymentIntentId(paymentIntentId);

        if (discountCode != null && !discountCode.isEmpty()) {
            DiscountCode code = discountCodeRepository.findByCode(discountCode)
                    .filter(DiscountCode::isActive)
                    .orElseThrow(() -> new RuntimeException("Invalid or inactive discount code"));
            double discount = order.getTotalPrice() * (code.getDiscountPercentage() / 100);
            order.setTotalPrice(order.getTotalPrice() - discount);
        }

        orderRepository.save(order);

        cartService.clearCart(authentication);

        emailService.sendEmail(
                user.getEmail(),
                "Order Confirmation",
                "Thank you for your order! Order ID: " + order.getId() + "\nTotal: $" + order.getTotalPrice() + "\nStatus: " + order.getOrderStatus()
        );
         return order;
    }

    public List<Order> getUserOrders(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUserId(user.getId());
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(!"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Only admins can update order status");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setOrderStatus(status);
        orderRepository.save(order);

        emailService.sendEmail(
                order.getUser().getEmail(),
                "Order Status Update",
                "Your Order #" + order.getId() + "is now " + status
        );
        return order;
    }

    @Transactional
    public Order cancelOrder(Long orderId, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("You can only cancel your own orders");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only PENDING orders can be cancelled");
        }

        Stripe.apiKey = stripeSecretKey;
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(order.getPaymentIntentId()); // Assume this field is added
            paymentIntent.cancel();
        } catch (Exception e) {
            throw new RuntimeException("Failed to refund payment: " + e.getMessage());
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        emailService.sendEmail(
                user.getEmail(),
                "Order Cancelled",
                "Your order #" + order.getId() + " has been cancelled."
        );

        return order;
    }

    public Order getOrderDetails(Long orderId, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("You can only view your own orders");
        }

        return order;
    }

    public String createPaymentIntent(double amount) throws Exception {
        Stripe.apiKey = stripeSecretKey;
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long) (amount * 100))
                .setCurrency("usd")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();
        PaymentIntent paymentIntent = PaymentIntent.create(params);
        return paymentIntent.getClientSecret();
    }
}

