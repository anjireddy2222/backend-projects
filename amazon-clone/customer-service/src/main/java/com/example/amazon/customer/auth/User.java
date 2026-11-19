package com.example.amazon.customer.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "app_users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, length = 100)
  public String name;

  @Column(nullable = false, unique = true, length = 180)
  public String email;

  @JsonIgnore
  @Column(nullable = false)
  public String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public Role role = Role.CUSTOMER;
}
