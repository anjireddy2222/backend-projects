package com.example.amazon.catalog.product;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, length = 150)
  public String name;

  @Column(nullable = false, length = 2000)
  public String description;

  @Column(nullable = false, length = 80)
  public String category;

  @Column(nullable = false, precision = 12, scale = 2)
  public BigDecimal price;

  @Column(nullable = false)
  public int stock;
}
