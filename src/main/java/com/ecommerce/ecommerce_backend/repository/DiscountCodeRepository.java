package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {
    Optional<DiscountCode> findByCode(String code);
}
