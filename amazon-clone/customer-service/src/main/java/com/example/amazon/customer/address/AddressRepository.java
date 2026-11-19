package com.example.amazon.customer.address;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
  List<Address> findByUserIdOrderById(Long userId);

  Optional<Address> findByIdAndUserId(Long id, Long userId);
}
