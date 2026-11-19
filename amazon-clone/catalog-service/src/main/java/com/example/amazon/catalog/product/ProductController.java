package com.example.amazon.catalog.product;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
  private final ProductService service;

  public ProductController(ProductService service) {
    this.service = service;
  }

  @GetMapping
  public Page<Product> list(
      @RequestParam(defaultValue = "") String q,
      @RequestParam(required = false) String category,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return service.search(q, category, page, size);
  }

  @GetMapping("/{id}")
  public Product get(@PathVariable Long id) {
    return service.get(id);
  }
}
