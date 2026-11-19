package com.example.amazon.customer.cart;

import com.example.amazon.customer.auth.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "productId"}))
public class CartItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id")
  @JsonIgnore
  public User user;

  @Column(nullable = false)
  public Long productId;

  @Column(nullable = false)
  public int quantity;

  @Version public Long version;
}
