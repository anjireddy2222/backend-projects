package com.example.amazon.catalog.product;

import com.example.amazon.catalog.common.ApiException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
  private final ProductRepository products;

  public ProductService(ProductRepository products) {
    this.products = products;
  }

  public Product get(Long id) {
    return products.findById(id).orElseThrow(() -> ApiException.notFound("Product not found"));
  }

  public Page<Product> search(String query, String category, int page, int size) {
    if (page < 0 || size < 1 || size > 100)
      throw ApiException.badRequest("page must be >= 0 and size between 1 and 100");
    return products.search(query.trim(), category, PageRequest.of(page, size, Sort.by("id")));
  }
}
