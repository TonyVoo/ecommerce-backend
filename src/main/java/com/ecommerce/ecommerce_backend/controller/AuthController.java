package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import com.ecommerce.ecommerce_backend.service.EmailService;
import com.ecommerce.ecommerce_backend.service.UserService;
import com.ecommerce.ecommerce_backend.utils.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        userRepository.save(user);

        String subject = "Welcome to E-commerce Store";
        String text = "Hello " + user.getUsername() + ",\n\nThank you for registering with us!";
        emailService.sendEmail(user.getEmail(), subject, text);

        return ResponseEntity.ok("User registered successfully. Check your email!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) throws Exception {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        final UserDetails userDetails = userService.loadUserByUsername(authRequest.getUsername());
        User user = userRepository.findByUsername(authRequest.getUsername()).get();
        final String jwt = jwtUtil.generateToken(userDetails.getUsername(), user.getRole());
        return ResponseEntity.ok(new AuthResponse(jwt));
    }
}

@Data
class AuthRequest {
    private String username;
    private String password;
}

@Data
class AuthResponse {
    private final String jwt;
}

