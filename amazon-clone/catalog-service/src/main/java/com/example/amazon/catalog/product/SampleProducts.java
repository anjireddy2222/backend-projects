package com.example.amazon.catalog.product;

import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;

@Configuration
@Profile("demo")
public class SampleProducts {
  @Bean
  CommandLineRunner seedProducts(ProductRepository products) {
    return args -> {
      if (products.count() != 0) return;
      add(
          products,
          "Wireless Mouse",
          "Comfortable USB wireless mouse",
          "Electronics",
          "599.00",
          25);
      add(products, "Java Basics", "A beginner-friendly programming book", "Books", "399.00", 50);
      add(products, "Travel Backpack", "Lightweight everyday backpack", "Bags", "999.00", 15);
    };
  }

  private void add(
      ProductRepository products,
      String name,
      String description,
      String category,
      String price,
      int stock) {
    Product p = new Product();
    p.name = name;
    p.description = description;
    p.category = category;
    p.price = new BigDecimal(price);
    p.stock = stock;
    products.save(p);
  }
}
