package com.example.helpdesk.auth;

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

  @ManyToOne(optional = false)
  @JoinColumn(name = "role_name")
  public RoleEntity role = new RoleEntity(Role.CUSTOMER);
}
