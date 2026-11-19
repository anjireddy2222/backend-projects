package com.example.helpdesk.auth;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class RoleEntity {
  @Id
  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  public Role name;

  public RoleEntity() {}

  public RoleEntity(Role name) {
    this.name = name;
  }

  // Keep API responses simple: "CUSTOMER", rather than a nested role object.
  @JsonValue
  public Role value() {
    return name;
  }
}
