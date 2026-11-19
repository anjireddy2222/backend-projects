package com.example.amazon.catalog.product;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
  @Query(
      "select p from Product p where (lower(p.name) like lower(concat('%', :q, '%')) or"
          + " lower(p.description) like lower(concat('%', :q, '%'))) and (:category is null or"
          + " lower(p.category) = lower(:category))")
  Page<Product> search(
      @Param("q") String query, @Param("category") String category, Pageable pageable);
}
