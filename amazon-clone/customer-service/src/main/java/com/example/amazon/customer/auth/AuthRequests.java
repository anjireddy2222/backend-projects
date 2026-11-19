package com.example.amazon.customer.auth;

import jakarta.validation.constraints.*;

public class AuthRequests {
  public record Register(
      @NotBlank @Size(max = 100) String name,
      @NotBlank @Email @Size(max = 180) String email,
      @NotBlank @Size(min = 8, max = 64) String password) {}

  public record Login(@NotBlank @Email String email, @NotBlank String password) {}

  public record Forgot(@NotBlank @Email String email) {}

  public record Reset(
      @NotBlank @Size(max = 100) String token,
      @NotBlank @Size(min = 8, max = 64) String password) {}

  public record Token(String accessToken, String tokenType, long expiresIn) {}
}
