package com.example.amazon.customer.address;

import com.example.amazon.customer.auth.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class Address {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false)
  @JsonIgnore
  public User user;

  @Column(nullable = false, length = 100)
  public String recipient;

  @Column(nullable = false, length = 20)
  public String phone;

  @Column(nullable = false, length = 200)
  public String line1;

  @Column(nullable = false, length = 80)
  public String city;

  @Column(nullable = false, length = 80)
  public String state;

  @Column(nullable = false, length = 12)
  public String postalCode;

  @Column(nullable = false, length = 80)
  public String country;
}
