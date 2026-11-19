package com.example.helpdesk.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class RoleSetup implements CommandLineRunner {
  private final RoleRepository roles;

  public RoleSetup(RoleRepository roles) {
    this.roles = roles;
  }

  @Override
  public void run(String... args) {
    for (Role role : Role.values()) {
      if (!roles.existsById(role)) roles.save(new RoleEntity(role));
    }
  }
}
