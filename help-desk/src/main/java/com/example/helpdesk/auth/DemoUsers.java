package com.example.helpdesk.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("demo")
public class DemoUsers {
  @Bean
  @org.springframework.core.annotation.Order(1)
  CommandLineRunner seedUsers(
      UserRepository users,
      PasswordEncoder encoder,
      @Value("${DEMO_ADMIN_PASSWORD}") String adminPassword,
      @Value("${DEMO_AGENT_PASSWORD}") String agentPassword) {
    return args -> {
      add(users, encoder, "Demo Admin", "admin@example.test", adminPassword, Role.ADMIN);
      add(users, encoder, "Demo Agent", "agent@example.test", agentPassword, Role.AGENT);
    };
  }

  private void add(
      UserRepository users,
      PasswordEncoder encoder,
      String name,
      String email,
      String password,
      Role role) {
    if (password.length() < 8 || password.length() > 64)
      throw new IllegalArgumentException("Demo passwords must be 8-64 characters");
    if (users.findByEmail(email).isPresent()) return;
    User user = new User();
    user.name = name;
    user.email = email;
    user.password = encoder.encode(password);
    user.role = new RoleEntity(role);
    users.save(user);
  }
}
