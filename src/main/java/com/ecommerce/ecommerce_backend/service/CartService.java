package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.entity.Cart;
import com.ecommerce.ecommerce_backend.entity.CartItem;
import com.ecommerce.ecommerce_backend.entity.Product;
import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.repository.CartRepository;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Cart addToCart(Long productId, int quantity, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElse(new Cart(user));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if(product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if(existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem(product, quantity);
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    public Cart getCart(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cartRepository.findByUserId(user.getId())
                .orElse(new Cart(user));
    }

    @Transactional
    public Cart updateCartItem(Long cartItemId, int quantity, Authentication authentication) {
        Cart cart = getCart(authentication);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart Item not found"));

        if(quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            if(item.getProduct().getStock() < quantity) {
                throw new RuntimeException("Insufficient stock");
            }
            item.setQuantity(quantity);
        }
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeCartItem(Long cartItemId, Authentication authentication) {
        Cart cart = getCart(authentication);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart Item not found"));
        cart.getItems().remove(cart);
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Authentication authentication) {
        Cart cart = getCart(authentication);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
