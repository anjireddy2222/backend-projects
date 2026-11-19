package com.example.amazon.customer.cart;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<CartItem, Long> {
  List<CartItem> findByUserIdOrderById(Long userId);

  Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

  Optional<CartItem> findByIdAndUserId(Long id, Long userId);
}
