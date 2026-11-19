package com.example.helpdesk.auth;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class PasswordReset {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, unique = true, length = 64)
  public String tokenHash;

  @ManyToOne(optional = false)
  public User user;

  @Column(nullable = false)
  public Instant expiresAt;
}
